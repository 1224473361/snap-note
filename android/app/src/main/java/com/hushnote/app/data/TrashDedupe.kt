package com.hushnote.app.data

/**
 * 同一条记录可能进回收站多次（飞书仍保留远端，同步拉回后再删）。
 * LazyColumn 用 id 当 key 时，重复 id 会在进入页面时直接崩溃。
 * 保留列表中先出现的那条（调用方按“最新删除在前”存放）。
 */
fun <T> MutableList<T>.collapseDuplicateIds(idOf: (T) -> String): Boolean {
    val deduped = distinctBy(idOf)
    if (deduped.size == size) return false
    clear()
    addAll(deduped)
    return true
}
