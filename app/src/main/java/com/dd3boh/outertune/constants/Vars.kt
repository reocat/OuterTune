package com.dd3boh.outertune.constants

// crash at first extractor scanner error. Currently not implemented
const val SCANNER_CRASH_AT_FIRST_ERROR = false

// true will not use multithreading for scanner
const val SYNC_SCANNER = false

// maximum parallel scanner jobs allowed
const val MAX_CONCURRENT_JOBS = 4

// enable verbose debugging details for scanner
const val SCANNER_DEBUG = false

// enable verbose debugging details for extractor
const val EXTRACTOR_DEBUG = false

// enable printing of *ALL* data that extractor reads
const val DEBUG_SAVE_OUTPUT = false // ignored (will be false) when EXTRACTOR_DEBUG IS false