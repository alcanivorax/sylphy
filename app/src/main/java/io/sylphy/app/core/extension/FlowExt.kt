package io.sylphy.app.core.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber

fun <T> Flow<T>.catchAndLog(tag: String = "FlowExt"): Flow<T> =
    catch { e -> Timber.tag(tag).e(e) }

fun <T, R> Flow<List<T>>.mapList(transform: (T) -> R): Flow<List<R>> =
    map { list -> list.map(transform) }
