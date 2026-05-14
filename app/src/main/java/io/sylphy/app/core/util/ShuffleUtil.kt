package io.sylphy.app.core.util

fun <T> List<T>.shuffledQueue(currentItem: T? = null): List<T> {
    if (isEmpty()) return this
    val mutable = toMutableList()
    if (currentItem != null && mutable.remove(currentItem)) {
        return listOf(currentItem) + mutable.shuffled()
    }
    return mutable.shuffled()
}
