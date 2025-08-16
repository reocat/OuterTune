package com.dd3boh.outertune.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.room.Room
import com.dd3boh.outertune.db.MusicDatabase.Companion.MUSIC_DATABASE_VERSION
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Attempts to open Room normally. If opening fails due to integrity/hash mismatch or migration issues,
 * it will:
 * 1) Move the existing DB to a timestamped backup
 * 2) Create a fresh DB with the current schema
 * 3) Best-effort copy of table data from the backup into the new DB (column-intersection copy)
 * 4) Show toasts indicating the outcome
 */
object DatabasePatcher {
    private const val TAG = "DatabasePatcher"

    fun buildPatched(context: Context): MusicDatabase {
        // 1) Try normal open first
        return try {
            Timber.tag(TAG).i("Opening Room database normally (v$MUSIC_DATABASE_VERSION)")
            val db = buildRoom(context)
            // Force open to trigger integrity/migration validation now
            db.openHelper.writableDatabase
            db
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Room open failed, evaluating for patching...")
            if (!isRoomIntegrityOrMigrationError(e)) {
                // Unknown failure: rethrow
                throw e
            }

            showToast(context, "Database issue detected. Attempting repair...")

            // 2) Move existing DB to backup
            val dbFile = context.getDatabasePath(InternalDatabase.DB_NAME)
            val backup = tryBackup(dbFile)

            // 3) Create new fresh DB file
            val fresh = try {
                val f = buildRoom(context)
                // Force open
                f.openHelper.writableDatabase
                f
            } catch (freshErr: Throwable) {
                Timber.tag(TAG).e(freshErr, "Failed to build fresh DB even after backup")
                // As a last resort, delete and try once more
                dbFile.safeDelete()
                val f2 = buildRoom(context)
                f2.openHelper.writableDatabase
                f2
            }

            // 4) Best-effort data copy from backup
            val copied = backup?.let { bak ->
                try {
                    copyDataBestEffort(from = bak, to = dbFile)
                } catch (copyErr: Throwable) {
                    Timber.tag(TAG).e(copyErr, "Copy from backup failed")
                    false
                }
            } ?: false

            // Validate schema sanity for common pitfalls (e.g., playlist columns)
            val schemaOk = try {
                val sdb = fresh.openHelper.writableDatabase
                sdb.query("SELECT description, privacyStatus FROM playlist LIMIT 0").use { }
                true
            } catch (t: Throwable) {
                Timber.tag(TAG).w(t, "Schema validation after copy failed; performing full reset")
                false
            }

            if (!schemaOk) {
                // Full reset without copy
                dbFile.safeDelete()
                val wal = File(dbFile.path + "-wal"); val shm = File(dbFile.path + "-shm")
                wal.safeDelete(); shm.safeDelete()
                val reset = buildRoom(context).also { it.openHelper.writableDatabase }
                showToast(context, "Database was reset due to incompatible schema.")
                return reset
            } else {
                if (copied) {
                    showToast(context, "Database repaired. Your data was migrated.")
                } else {
                    showToast(context, "Database was reset due to integrity issues.")
                }
            }

            fresh
        }
    }

    private fun buildRoom(context: Context): MusicDatabase {
        return MusicDatabase(
            delegate = Room.databaseBuilder(context, InternalDatabase::class.java, InternalDatabase.DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_14_15)
                .addMigrations(MIGRATION_15_16)
                .addMigrations(MIGRATION_16_17)
                .addMigrations(MIGRATION_18_19)
                .addMigrations(MIGRATION_20_21)
                .build()
        )
    }

    private fun isRoomIntegrityOrMigrationError(e: Throwable): Boolean {
        // Common Room integrity/migration error messages
        val msg = buildString {
            var cur: Throwable? = e
            while (cur != null) {
                append(cur.message ?: "")
                append('\n')
                cur = cur.cause
            }
        }.lowercase()
        return e is IllegalStateException || e is SQLiteException || e is SQLException ||
            msg.contains("cannot verify data integrity") ||
            msg.contains("room cannot verify data integrity") ||
            msg.contains("migration didn't properly handle") ||
            msg.contains("expected:") ||
            msg.contains("found:") ||
            msg.contains("has changed but the version") ||
            msg.contains("requires a migration") ||
            msg.contains("migration")
    }

    private fun tryBackup(dbFile: File): File? {
        return try {
            if (!dbFile.exists()) return null

            val backupName = "${dbFile.name}.bak.${System.currentTimeMillis()}"
            val backupFile = File(dbFile.parentFile, backupName)

            // Also handle -wal and -shm if present
            val wal = File(dbFile.path + "-wal")
            val shm = File(dbFile.path + "-shm")

            dbFile.copyTo(backupFile, overwrite = true)
            if (wal.exists()) wal.copyTo(File(backupFile.path + "-wal"), overwrite = true)
            if (shm.exists()) shm.copyTo(File(backupFile.path + "-shm"), overwrite = true)

            // Remove original to allow fresh creation
            dbFile.safeDelete(); wal.safeDelete(); shm.safeDelete()

            Timber.tag(TAG).i("Backed up DB to: ${backupFile.path}")
            backupFile
        } catch (io: IOException) {
            Timber.tag(TAG).e(io, "Failed to backup DB file")
            null
        }
    }

    private fun copyDataBestEffort(from: File, to: File): Boolean {
        if (!from.exists() || !to.exists()) return false

        var success = true
        val oldDb = SQLiteDatabase.openDatabase(from.path, null, SQLiteDatabase.OPEN_READONLY)
        val newDb = SQLiteDatabase.openDatabase(to.path, null, SQLiteDatabase.OPEN_READWRITE)
        try {
            newDb.execSQL("PRAGMA foreign_keys = OFF")
            newDb.beginTransaction()

            val oldTables = oldDb.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT IN ('room_master_table','android_metadata')",
                null
            )
            val tables = mutableListOf<String>()
            oldTables.use {
                while (it.moveToNext()) tables.add(it.getString(0))
            }

            for (table in tables) {
                try {
                    copyTable(oldDb, newDb, table)
                } catch (t: Throwable) {
                    Timber.tag(TAG).w(t, "Failed copying table %s", table)
                    success = false
                }
            }

            if (success) newDb.setTransactionSuccessful()
        } finally {
            try { newDb.endTransaction() } catch (_: Throwable) {}
            try { newDb.execSQL("PRAGMA foreign_keys = ON") } catch (_: Throwable) {}
            oldDb.close(); newDb.close()
        }
        return success
    }

    private fun copyTable(oldDb: SQLiteDatabase, newDb: SQLiteDatabase, table: String) {
        // Compute intersection of columns
        val oldCols = tableColumns(oldDb, table)
        val newCols = tableColumns(newDb, table)
        if (oldCols.isEmpty() || newCols.isEmpty()) return
        val cols = oldCols.intersect(newCols.toSet()).toList()
        if (cols.isEmpty()) return

        // Try fast-path: INSERT INTO new SELECT columns FROM old via ATTACH
        val attached = try {
            newDb.execSQL("ATTACH DATABASE ? AS olddb", arrayOf(oldDb.path))
            true
        } catch (_: Throwable) { false }

        if (attached) {
            try {
                val colList = cols.joinToString(",") { "`$it`" }
                newDb.execSQL("INSERT OR IGNORE INTO `$table`($colList) SELECT $colList FROM olddb.`$table`")
                newDb.execSQL("DETACH DATABASE olddb")
                return
            } catch (_: Throwable) {
                try { newDb.execSQL("DETACH DATABASE olddb") } catch (_: Throwable) {}
                // fall through to row-by-row
            }
        }

        // Row-by-row copy fallback
        oldDb.rawQuery("SELECT ${cols.joinToString(",") { "`$it`" }} FROM `$table`", null).use { cursor ->
            val cv = ContentValues()
            while (cursor.moveToNext()) {
                cv.clear()
                for (i in cols.indices) {
                    val name = cols[i]
                    when (cursor.getType(i)) {
                        Cursor.FIELD_TYPE_NULL -> cv.putNull(name)
                        Cursor.FIELD_TYPE_INTEGER -> cv.put(name, cursor.getLong(i))
                        Cursor.FIELD_TYPE_FLOAT -> cv.put(name, cursor.getDouble(i))
                        Cursor.FIELD_TYPE_STRING -> cv.put(name, cursor.getString(i))
                        Cursor.FIELD_TYPE_BLOB -> cv.put(name, cursor.getBlob(i))
                        else -> cv.putNull(name)
                    }
                }
                try {
                    newDb.insertWithOnConflict(table, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                } catch (t: Throwable) {
                    Timber.tag(TAG).w(t, "Insert failed for table %s", table)
                }
            }
        }
    }

    private fun tableColumns(db: SQLiteDatabase, table: String): List<String> {
        return try {
            db.rawQuery("PRAGMA table_info(`$table`)", null).use { c ->
                val out = mutableListOf<String>()
                val nameIdx = c.getColumnIndex("name")
                while (c.moveToNext()) out.add(c.getString(nameIdx))
                out
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}

private fun File.safeDelete() {
    try { delete() } catch (_: Throwable) {}
}

private fun showToast(context: Context, message: String) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    } else {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
