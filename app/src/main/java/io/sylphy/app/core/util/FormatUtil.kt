package io.sylphy.app.core.util

import java.security.MessageDigest

fun Long.toMmSs(): String {
    val total = this / 1000
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}

fun Long.toHhMm(): String {
    val total = this / 1000 / 60
    val h = total / 60
    val m = total % 60
    return "%d h %02d min".format(h, m)
}

fun String.sha1(): String {
    val bytes = MessageDigest.getInstance("SHA-1").digest(toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun Int.toTrackCountLabel(): String = if (this == 1) "1 track" else "$this tracks"
