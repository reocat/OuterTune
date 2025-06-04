package com.dd3boh.outertune.constants

import com.dd3boh.outertune.BuildConfig

// crash at first extractor scanner error. Currently not implemented
const val SCANNER_CRASH_AT_FIRST_ERROR = false

// true will not use multithreading for scanner
const val SYNC_SCANNER = false

// maximum parallel download jobs allowed
const val MAX_CONCURRENT_DOWNLOAD_JOBS = 3 // ytm defaults to 3

// maximum parallel scanner jobs allowed
const val MAX_CONCURRENT_JOBS = 4

// enable verbose debugging details for scanner
var SCANNER_DEBUG = BuildConfig.DEBUG

// enable verbose debugging details for extractor
var EXTRACTOR_DEBUG = BuildConfig.DEBUG

// enable printing of *ALL* data that extractor reads
var DEBUG_SAVE_OUTPUT = BuildConfig.DEBUG // ignored (will be false) when EXTRACTOR_DEBUG IS false

const val ENABLE_UPDATE_CHECKER = false

const val SCANNER_OWNER_DL = 32
const val SCANNER_OWNER_LM = 1