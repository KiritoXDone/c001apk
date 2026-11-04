package com.example.c001apk.compose.ui.main

// 保持访问 ID 不变，只更新本次搜索及其直接来源页面的输入。
internal fun AppNavigator.updateSearchInput(sourceEntryId: String, query: String) {
    val stack = controller.backStack
    val sourceIndex = stack.indexOfFirst { (it as? AppNavEntry)?.id == sourceEntryId }
    if (sourceIndex < 0) return
    val source = (stack[sourceIndex] as AppNavEntry).destination
    val targetIndex = when (source) {
        is AppDestination.Search -> sourceIndex
        is AppDestination.SearchResult -> sourceIndex - 1
        else -> return
    }
    val target = stack.getOrNull(targetIndex) as? AppNavEntry ?: return
    val search = target.destination as? AppDestination.Search ?: return
    if (source is AppDestination.SearchResult &&
        (source.title != search.title || source.pageType != search.pageType || source.pageParam != search.pageParam)
    ) return
    if (search.query != query) stack[targetIndex] = target.copy(destination = search.copy(query = query))
}
