package com.dd3boh.outertune.extensions

fun <T> List<T>.reversed(reversed: Boolean) = if (reversed) asReversed() else this

fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int): MutableList<T> {
    // First validate the initial indices
    if (fromIndex !in indices || toIndex !in 0..size) {
        throw IndexOutOfBoundsException("Invalid indices: fromIndex=$fromIndex, toIndex=$toIndex, size=$size")
    }
    
    val element = removeAt(fromIndex)
    
    // After removing the element, we need to adjust the toIndex if it was after fromIndex
    val adjustedToIndex = when {
        toIndex > fromIndex -> toIndex - 1
        else -> toIndex
    }
    
    add(adjustedToIndex, element)
    return this
}
