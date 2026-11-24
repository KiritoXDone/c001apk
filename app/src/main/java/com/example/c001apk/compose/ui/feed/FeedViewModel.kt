package com.example.c001apk.compose.ui.feed

import com.example.c001apk.compose.logic.filter.loadFilteredPages
import com.example.c001apk.compose.logic.filter.FilterScope
import com.example.c001apk.compose.logic.filter.ContentFilterStore
import com.example.c001apk.compose.logic.filter.ContentFilterEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import androidx.lifecycle.viewModelScope
import com.example.c001apk.compose.constant.Constants.EMPTY_STRING
import com.example.c001apk.compose.logic.model.FeedArticleContentBean
import com.example.c001apk.compose.logic.model.FeedEntity
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.network.ApiFailures
import com.example.c001apk.compose.logic.repository.BlackListRepo
import com.example.c001apk.compose.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.compose.logic.repository.NetworkRepo
import com.example.c001apk.compose.logic.smid.hasRequestDeviceIdentity
import com.example.c001apk.compose.logic.state.FeedDeletionEvents
import com.example.c001apk.compose.logic.state.FooterState
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.base.BaseViewModel
import com.example.c001apk.compose.ui.base.LikeType
import com.example.c001apk.compose.ui.feed.reply.VoteCommentTarget
import com.example.c001apk.compose.ui.history.HistoryType
import com.example.c001apk.compose.util.CookieUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Created by bggRGjQaUbCoE on 2024/6/6 */
@HiltViewModel(assistedFactory = FeedViewModel.ViewModelFactory::class)
class FeedViewModel
@AssistedInject
constructor(
    @Assisted var id: String,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    contentFilters: ContentFilterStore,
    private val historyFavoriteRepo: HistoryFavoriteRepo,
    private val preferences: com.example.c001apk.compose.logic.repository.UserPreferencesRepository,
) : BaseViewModel(networkRepo, blackListRepo, contentFilters) {

    @AssistedFactory
    interface ViewModelFactory {
        fun create(id: String): FeedViewModel
    }

    var feedState by mutableStateOf<LoadingState<HomeFeedResponse.Data>>(LoadingState.Loading)
        private set

    var itemSize = 2

    lateinit var replyType: String
    private var discussMode: Int = 1
    var listType: String = "lastupdate_desc"
    var feedType: String = "feed"
    private var blockStatus = 0
    var fromFeedAuthor = 0

    private var topId: String? = null
    private var meId: String? = null

    var articleList: List<FeedArticleContentBean>? = null

    var replyCount by mutableStateOf("0")
    var isFav by mutableStateOf(false)
    var isBlocked by mutableStateOf(false)
    private var feedJob: kotlinx.coroutines.Job? = null

    var isAccountCollected by mutableStateOf(false)
        private set

    var deletingFeed by mutableStateOf(false)
        private set

    var deletedFeedId by mutableStateOf<String?>(null)
        private set

    var submittingVote by mutableStateOf(false)
        private set

    var voteError by mutableStateOf<String?>(null)
        private set

    private var deleteJob: kotlinx.coroutines.Job? = null
    private var voteJob: kotlinx.coroutines.Job? = null
    private var isVoteFeed = false
    private var localFavoriteBusy = false
    private val guestReplyGate = GuestReplyGate(
        viewModelScope,
        preferences.data.map { hasRequestDeviceIdentity(it.xAppDevice) },
        awaitPublishedIdentity = { preferences.synchronizeDeviceIdentity() },
    )

    fun dismissTradeLinkHint() {
        viewModelScope.launch { preferences.setHideTradeLinkHint(true) }
    }

    val collections =
        FeedCollections(
            viewModelScope,
            networkRepo,
            onSaved = { feedId, collected -> if (feedId == id) isAccountCollected = collected },
            notify = { toastText = it },
        )

    fun deleteOwnFeed() {
        val feed = (feedState as? LoadingState.Success)?.response ?: return
        if (deletingFeed || !CookieUtil.isLogin || feed.uid != CookieUtil.uid) return
        val requestedId = id
        val revision = CookieUtil.accountSession.revision
        deletingFeed = true
        deleteJob = viewModelScope.launch {
            try {
                networkRepo.postDelete("/v6/feed/deleteFeed", requestedId).collect { result ->
                    if (requestedId != id || revision != CookieUtil.accountSession.revision)
                        return@collect
                    val response = result.getOrNull()
                    val success =
                        response != null &&
                            (response.error == null || response.error == 0) &&
                            (response.data?.count == "删除成功" || response.message == "删除成功")
                    if (success) {
                        deletedFeedId = requestedId
                        FeedDeletionEvents.deleted(
                            requestedId,
                        )
                        toastText = AppLanguage.getString(R.string.feed_deleted)
                    } else {
                        toastText =
                            response?.message
                                ?: (result.exceptionOrNull() as? Exception)?.let {
                                    ApiFailures.safeMessage(it)
                                }
                                ?: AppLanguage.getString(R.string.error_delete)
                    }
                }
            } finally {
                deletingFeed = false
            }
        }
    }

    fun consumeFeedDeletion() {
        deletedFeedId = null
    }

    fun submitVote(optionIds: Set<String>, anonymous: Boolean) {
        val state = feedState as? LoadingState.Success ?: return
        val vote = state.response.vote ?: return
        val voteId = vote.id?.takeIf(String::isNotBlank) ?: return
        val account = CookieUtil.accountSession
        if (submittingVote || isRefreshing || state.isPublic || !account.isLogin) return
        val now = System.currentTimeMillis() / 1_000
        if (vote.isUpcoming(now) || vote.isClosed(now) || vote.selectedIds.isNotEmpty()) return
        val validChoices = vote.availableOptions.mapNotNull { it.id }.filter(optionIds::contains)
        if (validChoices.size != optionIds.size || validChoices.size !in vote.selectionRange) return

        val requestedId = id
        val submitAnonymously = anonymous && vote.anonymousStatus == 1
        voteError = null
        submittingVote = true
        voteJob = viewModelScope.launch {
            try {
                val result = networkRepo.postVote(voteId, validChoices, submitAnonymously).first()
                if (id != requestedId || account.revision != CookieUtil.accountSession.revision) return@launch
                val current = feedState as? LoadingState.Success ?: return@launch
                if (current.response.vote?.id != voteId) return@launch
                val response = result.getOrNull()
                val updatedVote = response?.data
                if (updatedVote == null || (response.error ?: 0) != 0) {
                    voteError = (result.exceptionOrNull() as? Exception)?.let(ApiFailures::safeMessage)
                        ?: response?.message?.takeIf(String::isNotBlank)
                        ?: AppLanguage.getString(R.string.error_vote)
                    return@launch
                }
                val confirmedVote = updatedVote.copy(
                    userSelectedOption = updatedVote.userSelectedOption
                        ?.takeIf(String::isNotBlank) ?: validChoices.joinToString(","),
                    userAnonymousStatus = updatedVote.userAnonymousStatus ?: if (submitAnonymously) 1 else 0,
                )
                feedState = current.copy(response = current.response.copy(vote = confirmedVote))
                replyCount = confirmedVote.totalCommentNum?.toString() ?: replyCount
            } finally {
                if (id == requestedId) submittingVote = false
            }
        }
    }

    internal fun onVoteCommentPublished(target: VoteCommentTarget) {
        if (id != target.feedId || !target.isCurrentAccount()) return
        val current = feedState as? LoadingState.Success ?: return
        val vote = current.response.vote?.takeIf { it.id == target.voteId } ?: return
        feedState = current.copy(response = current.response.copy(vote = vote.copy(userVoteCommentStatus = 1)))
        refreshVoteFeed()
    }

    init {
        fetchFeedData()
    }

    private suspend fun recordHistory(
        username: String,
        avatar: String,
        device: String,
        message: String,
        pubDate: String,
        type: HistoryType = HistoryType.HISTORY,
    ) {
        when (type) {
            HistoryType.FAV ->
                historyFavoriteRepo.insertFavorite(
                    FeedEntity(id, feedUid, username, avatar, device, message, pubDate),
                )

            HistoryType.HISTORY ->
                historyFavoriteRepo.insertHistory(
                    FeedEntity(id, feedUid, username, avatar, device, message, pubDate),
                )
        }
    }

    private fun fetchFeedData(verifyCaptcha: Boolean = false) {
        val requestedId = id
        val revision = CookieUtil.accountSession.revision
        feedJob?.cancel()
        feedJob =
            viewModelScope.launch(Dispatchers.IO) {
                networkRepo
                    .getFeedContent("/v6/feed/detail?id=$requestedId", verifyCaptcha)
                    .collect { state ->
                        if (requestedId != id || revision != CookieUtil.accountSession.revision) return@collect
                        if (state is LoadingState.Success) {
                            val response = state.response

                            feedUid = response.uid.orEmpty()
                            feedUsername = response.username.orEmpty()
                            isVoteFeed = response.isVoteFeed
                            replyCount = response.vote?.totalCommentNum?.toString() ?: response.replynum.orEmpty()
                            feedTypeName = response.feedTypeName.orEmpty()
                            feedType = response.feedType.orEmpty()

                            articleList =
                                runCatching {
                                    val type =
                                        object : TypeToken<List<FeedArticleContentBean>>() {}.type
                                    Gson()
                                        .fromJson<List<FeedArticleContentBean>>(
                                            response.messageRawOutput,
                                            type,
                                        )
                                        ?.filter { it.type in listOf("text", "image", "shareUrl") }
                                }
                                    .getOrNull()
                            itemSize = 2
                            if (!articleList.isNullOrEmpty()) {
                                if (!response.messageCover.isNullOrEmpty()) itemSize++
                                if (!response.title.isNullOrEmpty()) itemSize++
                                if (
                                    response.targetRow != null ||
                                        !response.relationRows.isNullOrEmpty()
                                )
                                    itemSize++
                                itemSize += articleList?.size ?: 0
                            }
                            if (!response.topReplyRows.isNullOrEmpty()) {
                                response.topReplyRows?.getOrNull(0)?.let { reply ->
                                    topId = reply.id
                                    val unameTag =
                                        when (reply.uid) {
                                            feedUid -> " [楼主]"
                                            else -> EMPTY_STRING
                                        }
                                    reply.username = "${reply.username}$unameTag [置顶]"
                                    if (!reply.replyRows.isNullOrEmpty()) {
                                        reply.replyRows =
                                            reply.replyRows?.map {
                                                it.copy(
                                                    message =
                                                        generateMess(
                                                            it,
                                                            feedUid,
                                                            reply.uid,
                                                        ),
                                                )
                                            }
                                    }
                                }
                            }
                            if (!response.replyMeRows.isNullOrEmpty()) {
                                response.replyMeRows?.getOrNull(0)?.let { reply ->
                                    meId = reply.id
                                    if (!reply.replyRows.isNullOrEmpty()) {
                                        reply.replyRows =
                                            reply.replyRows?.map {
                                                it.copy(
                                                    message =
                                                        generateMess(
                                                            it,
                                                            feedUid,
                                                            reply.uid,
                                                        ),
                                                )
                                            }
                                    }
                                }
                            }
                            guestReplyGate.start(
                                isGuest = !CookieUtil.isLogin,
                                isCurrent = { requestedId == id && revision == CookieUtil.accountSession.revision },
                                onWaiting = { loadingState = LoadingState.Error(AppLanguage.getString(R.string.error_device_not_ready)) },
                                onReady = ::fetchData,
                            )

                            if (CookieUtil.recordHistory && !historyFavoriteRepo.checkHistory(id)) {
                                recordHistory(
                                    username = response.username.orEmpty(),
                                    avatar = response.userAvatar.orEmpty(),
                                    device = response.deviceTitle.orEmpty(),
                                    message =
                                        with(response.message.orEmpty()) {
                                            if (this.length > 150) this.substring(0, 150) else this
                                        },
                                    pubDate = response.dateline.toString(),
                                )
                            }
                            isAccountCollected = response.userAction?.collect == 1
                            isFav = historyFavoriteRepo.checkFavorite(id)
                            isBlocked = blackListRepo.checkUid(feedUid)
                        }
                        feedState = state
                        isRefreshing = false
                    }
            }
    }

    lateinit var feedUid: String
    lateinit var feedUsername: String
    var feedTypeName by mutableStateOf(EMPTY_STRING)

    override fun loadMore() {
        guestReplyGate.cancel()
        super.loadMore()
    }

    override suspend fun customFetchData() =
        if (isVoteFeed) {
            networkRepo.getVoteComments(id, page, firstItem, lastItem)
        } else networkRepo.getFeedContentReply(
            id,
            listType,
            page,
            firstItem,
            lastItem,
            discussMode,
            feedType,
            blockStatus,
            fromFeedAuthor,
        )

    override fun handleResponse(
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data> {
        if (isVoteFeed) return response.distinctBy { it.entityId ?: it.id }
        response.forEach { item ->
            item.username = "${item.username}${if (item.uid == feedUid) " [楼主]" else EMPTY_STRING}"
            if (!item.replyRows.isNullOrEmpty()) {
                item.replyRows =
                    item.replyRows?.map {
                        it.copy(
                            message =
                                generateMess(
                                    it,
                                    feedUid,
                                    item.uid,
                                ),
                        )
                    }
            }
        }

        return response
            .distinctBy { it.entityId }
            .filterNot {
                if (listType == "lastupdate_desc") it.id in listOf(topId, meId) else false
            }
    }

    var isPull = false

    override fun refresh() {
        if (!isRefreshing && !isLoadMore) {
            if (feedState is LoadingState.Success) {
                if (isVoteFeed && submittingVote) {
                    isPull = false
                    return
                }
                if (isVoteFeed && isPull) {
                    refreshVoteFeed()
                    return
                }
                guestReplyGate.cancel()
                resetParams()
                isRefreshing = true
                fetchData()
            } else {
                if (isPull) {
                    isPull = false
                    viewModelScope.launch {
                        isRefreshing = true
                        delay(50)
                        isRefreshing = false
                    }
                }
                feedState = LoadingState.Loading
                fetchFeedData(verifyCaptcha = true)
            }
        }
    }

    private fun refreshVoteFeed() {
        guestReplyGate.cancel()
        cancelFetch()
        resetParams()
        isPull = false
        voteError = null
        isRefreshing = true
        fetchFeedData(verifyCaptcha = true)
    }

    private fun generateMess(
        reply: HomeFeedResponse.Data,
        feedUid: String?,
        uid: String?,
    ): String = run {
        val replyTag =
            when (reply.uid) {
                feedUid -> " [楼主] "
                uid -> " [层主] "
                else -> EMPTY_STRING
            }

        val rReplyTag =
            when (reply.ruid) {
                feedUid -> " [楼主] "
                uid -> " [层主] "
                else -> EMPTY_STRING
            }

        val rReplyUser =
            when (reply.ruid) {
                uid -> EMPTY_STRING
                else ->
                    """<a class="feed-link-uname" href="/u/${reply.ruid}">${reply.rusername}${rReplyTag}</a>"""
            }

        val replyPic =
            when (reply.pic) {
                EMPTY_STRING -> EMPTY_STRING
                else ->
                    """ <a class=\"feed-forward-pic\" href=${reply.pic}>${AppLanguage.getString(R.string.reply_view_images, reply.picArr?.size ?: 1)}</a>"""
            }

        """<a class="feed-link-uname" href="/u/${reply.uid}">${reply.username}${replyTag}</a>${AppLanguage.getString(R.string.reply_to_user, rReplyUser)}: ${reply.message}${replyPic}"""
    }

    var isSheet: Boolean = false
    var frid: String? = null
    lateinit var replyId: String
    lateinit var replyUid: String
    lateinit var replyName: String
    private var replyPage = 1
    private var replyLastItem: String? = null
    var isEndReply = false
    private var replyAutomaticPaused by mutableStateOf(false)
    val stopReplyAutomaticLoading get() = isEndReply || replyAutomaticPaused
    private var replyFetchJob: kotlinx.coroutines.Job? = null
    private var replyFetchGeneration = 0
    var replyRootId by mutableStateOf<String?>(null)
        private set

    private var isLoadMoreReply = false
    var replyLoadingState by
        mutableStateOf<LoadingState<List<HomeFeedResponse.Data>>>(LoadingState.Loading)
        private set

    var replyFooterState by mutableStateOf<FooterState>(FooterState.Success)
        private set

    private fun getReplyTop(): HomeFeedResponse.Data? {
        val replyTop =
            when (frid ?: replyId) {
                topId -> (feedState as LoadingState.Success).response.topReplyRows?.getOrNull(0)

                meId -> (feedState as LoadingState.Success).response.replyMeRows?.getOrNull(0)

                else ->
                    (loadingState as LoadingState.Success).response.find {
                        it.id == (frid ?: replyId)
                    }
            }
        return if (!frid.isNullOrEmpty()) replyTop?.replyRows?.find { it.id == replyId }
        else replyTop
    }

    fun fetchTotalReply() {
        val generation = ++replyFetchGeneration
        replyFetchJob?.cancel()
        val targetId = replyId
        val append = isLoadMoreReply
        replyAutomaticPaused = false
        replyFetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val filters = ContentFilterEngine(contentFilters.data.first())
                val paused = loadFilteredPages(
                    fetch = { networkRepo.getReply2Reply(targetId, replyPage, replyLastItem).first { it !is LoadingState.Loading } },
                    accept = { state, attempt ->
                        if (generation != replyFetchGeneration) throw kotlinx.coroutines.CancellationException()
                        when (state) {
                            LoadingState.Empty -> {
                                isEndReply = true
                                if (replyLoadingState !is LoadingState.Success) {
                                    replyLoadingState = getReplyTop()?.let {
                                        replyRootId = it.id
                                        LoadingState.Success(listOf(it))
                                    } ?: state
                                }
                                replyFooterState = FooterState.End
                                true
                            }
                            is LoadingState.Error -> {
                                replyAutomaticPaused = true
                                if (replyLoadingState is LoadingState.Success) replyFooterState = FooterState.Error(state.errMsg)
                                else replyLoadingState = state
                                true
                            }
                            is LoadingState.Success -> {
                                val raw = state.response
                                replyLastItem = raw.lastOrNull { !it.id.isNullOrBlank() }?.id ?: replyLastItem
                                isEndReply = raw.any { it.entityTemplate == "noMoreDataCard" }
                                var response = raw.filter { it.entityType == "feed_reply" }
                                    .map { it.copy(username = generateName(it, replyUid)) }
                                val hasVisible = response.any { filters.match(it, FilterScope.COMMENT) == null }
                                if (replyPage == 1) getReplyTop()?.let {
                                    replyRootId = it.id
                                    response = listOf(it) + response
                                }
                                replyPage++
                                if (append || attempt > 0) response = (replyLoadingState as? LoadingState.Success)?.response.orEmpty() + response
                                replyLoadingState = LoadingState.Success(response.distinctBy { it.entityId })
                                replyFooterState = if (isEndReply) FooterState.End else FooterState.Success
                                isEndReply || hasVisible
                            }
                            LoadingState.Loading -> true
                        }
                    },
                )
                if (paused) {
                    replyAutomaticPaused = true
                    replyFooterState = FooterState.Error(AppLanguage.getString(R.string.filter_batch_paused))
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (generation == replyFetchGeneration) {
                    replyAutomaticPaused = true
                    val message = ApiFailures.safeMessage(failure)
                    if (replyLoadingState is LoadingState.Success) replyFooterState = FooterState.Error(message)
                    else replyLoadingState = LoadingState.Error(message)
                }
            } finally {
                if (generation == replyFetchGeneration) isLoadMoreReply = false
            }
        }
    }

    private fun generateName(data: HomeFeedResponse.Data, uid: String): String = run {
        val replyTag =
            when (data.uid) {
                feedUid -> " [楼主] "
                uid -> " [层主] "
                else -> EMPTY_STRING
            }

        val rReplyTag =
            when (data.ruid) {
                feedUid -> " [楼主] "
                uid -> " [层主] "
                else -> EMPTY_STRING
            }

        if (data.ruid == "0")
            """<a class="feed-link-uname" href="/u/${data.uid}">${data.username}$replyTag</a>"""
        else {
            val target = """<a class="feed-link-uname" href="/u/${data.rusername}">${data.rusername}$rReplyTag</a>"""
            """<a class="feed-link-uname" href="/u/${data.uid}">${data.username}$replyTag</a>${AppLanguage.getString(R.string.reply_to_user, target)}"""
        }
    }

    fun resetReplyState() {
        replyFetchGeneration++
        replyFetchJob?.cancel()
        replyAutomaticPaused = false
        replyRootId = null
        replyPage = 1
        isLoadMoreReply = false
        isEndReply = false
        replyLastItem = null
        frid = null
        replyLoadingState = LoadingState.Loading
        replyFooterState = FooterState.Success
    }

    fun loadMoreReply() {
        if (!isLoadMoreReply && replyFetchJob?.isActive != true) {
            isEndReply = false
            isLoadMoreReply = true
            fetchTotalReply()
            if (replyLoadingState is LoadingState.Success) {
                replyFooterState = FooterState.Loading
            } else {
                replyLoadingState = LoadingState.Loading
            }
        }
    }

    override fun handleLikeResponse(id: String, like: Int, count: String?): Boolean? {
        return if (id in listOf(this.id, topId, meId)) {
            var response = (feedState as LoadingState.Success).response
            val isLike = if (like == 1) 0 else 1
            when (id) {
                this.id -> {
                    response =
                        response.copy(
                            likenum = count,
                            userAction = HomeFeedResponse.UserAction(isLike),
                        )
                }

                topId -> {
                    response.topReplyRows?.getOrNull(0)?.let {
                        response =
                            response.copy(
                                topReplyRows =
                                    listOf(
                                        it.copy(
                                            likenum = count,
                                            userAction = HomeFeedResponse.UserAction(isLike),
                                        ),
                                    ),
                            )
                    }
                }

                meId -> {
                    response.replyMeRows?.getOrNull(0)?.let {
                        response =
                            response.copy(
                                replyMeRows =
                                    listOf(
                                        it.copy(
                                            likenum = count,
                                            userAction = HomeFeedResponse.UserAction(isLike),
                                        ),
                                    ),
                            )
                    }
                }
            }
            feedState = LoadingState.Success(response)
            true
        } else null
    }

    fun blockUser() {
        viewModelScope.launch(Dispatchers.IO) {
            if (isBlocked) blackListRepo.deleteUid(feedUid) else blackListRepo.saveUid(feedUid)
            isBlocked = !isBlocked
        }
    }

    fun toggleLocalFavorite() {
        val response = (feedState as? LoadingState.Success)?.response ?: return
        if (localFavoriteBusy) return
        val requestedId = id
        val wasFavorite = isFav
        val entry =
            FeedEntity(
                requestedId,
                response.uid.orEmpty(),
                response.username.orEmpty(),
                response.userAvatar.orEmpty(),
                response.deviceTitle.orEmpty(),
                response.message.orEmpty().take(150),
                response.dateline.toString(),
            )
        localFavoriteBusy = true
        viewModelScope.launch {
            try {
                if (wasFavorite) historyFavoriteRepo.deleteFavorite(requestedId)
                else historyFavoriteRepo.insertFavorite(entry)
                if (requestedId == id) {
                    isFav = !wasFavorite
                    toastText = AppLanguage.getString(if (isFav) R.string.favorite_saved else R.string.favorite_removed)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                toastText = AppLanguage.getString(R.string.error_local_favorite)
            } finally {
                localFavoriteBusy = false
            }
        }
    }

    override fun handleLoadMore(
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

    override fun handleBlockUser(
        uid: String,
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data>? {
        var feedResponse = (feedState as LoadingState.Success).response
        feedResponse.topReplyRows?.getOrNull(0)?.let { reply ->
            feedResponse =
                if (reply.uid == uid) feedResponse.copy(topReplyRows = null)
                else
                    feedResponse.copy(
                        topReplyRows =
                            listOf(
                                reply.copy(
                                    replyRows = reply.replyRows?.filterNot { it.uid == uid },
                                ),
                            ),
                    )
        }
        feedResponse.replyMeRows?.getOrNull(0)?.let { reply ->
            feedResponse =
                if (reply.uid == uid) feedResponse.copy(replyMeRows = null)
                else
                    feedResponse.copy(
                        replyMeRows =
                            listOf(
                                reply.copy(
                                    replyRows = reply.replyRows?.filterNot { it.uid == uid },
                                ),
                            ),
                    )
        }
        feedState = LoadingState.Success(feedResponse)
        return if (frid.isNullOrEmpty()) null
        else
            response.map { item ->
                if (item.id == frid) {
                    item.copy(replyRows = item.replyRows?.filterNot { it.uid == uid })
                } else item
            }
    }

    fun onBlockReplyUser(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.saveUid(uid)

            if (replyLoadingState is LoadingState.Success) {
                val response =
                    (replyLoadingState as LoadingState.Success).response.filterNot { it.uid == uid }
                replyLoadingState = LoadingState.Success(response)
            }
        }
    }

    fun onDeleteRely(id: String, deleteType: LikeType) {
        val url = if (deleteType == LikeType.FEED) "/v6/feed/deleteFeed" else "/v6/feed/deleteReply"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id).collect { result ->
                val response = result.getOrNull()
                if (response != null) {
                    if (!response.message.isNullOrEmpty()) {
                        toastText = response.message
                    } else if (response.data?.count == "删除成功") {
                        toastText = response.data.count
                        val dataList =
                            (replyLoadingState as LoadingState.Success).response.filterNot {
                                it.id == id
                            }
                        replyLoadingState = LoadingState.Success(dataList)
                    }
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    fun onLikeReply(id: String, like: Int, likeType: LikeType) {
        val isLike =
            when (likeType) {
                LikeType.FEED -> if (like == 1) "unlike" else "like"
                LikeType.REPLY -> if (like == 1) "unLikeReply" else "likeReply"
            }
        val likeUrl = "/v6/feed/$isLike"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLike(likeUrl, id).collect { result ->
                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        if (!response.message.isNullOrEmpty()) {
                            toastText = response.message
                        } else if (handleLikeResponse(id, like, response.data.count) == null) {
                            val dataList =
                                (replyLoadingState as LoadingState.Success).response.map {
                                    if (it.id == id) {
                                        it.copy(
                                            likenum = response.data.count,
                                            userAction =
                                                it.userAction?.copy(like = if (like == 1) 0 else 1),
                                        )
                                    } else it
                                }
                            replyLoadingState = LoadingState.Success(dataList)
                        }
                    }
                } else {
                    toastText = ApiFailures.safeMessage(result.exceptionOrNull())
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    fun updateReply(data: HomeFeedResponse.Data) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isSheet) {
                isSheet = false
                val reply = data.copy(username = generateName(data, replyUid))
                var response = (replyLoadingState as? LoadingState.Success)?.response ?: emptyList()
                val position = response.map { it.id }.indexOf(replyId)
                response =
                    response.toMutableList().also {
                        it.add(position + 1, reply)
                    }
                replyLoadingState = LoadingState.Success(response)
            } else {
                val reply = data.copy(message = generateMess(data, feedUid, replyUid))
                if ((frid ?: replyId) in listOf(topId, meId)) {
                    var feedResponse = (feedState as LoadingState.Success).response
                    if ((frid ?: replyId) == topId)
                        feedResponse.topReplyRows?.getOrNull(0)?.let {
                            feedResponse =
                                feedResponse.copy(
                                    topReplyRows =
                                        listOf(
                                            it.copy(
                                                replyRows =
                                                    (it.replyRows ?: emptyList()) + listOf(reply),
                                            ),
                                        ),
                                )
                        }
                    else
                        feedResponse.replyMeRows?.getOrNull(0)?.let {
                            feedResponse =
                                feedResponse.copy(
                                    replyMeRows =
                                        listOf(
                                            it.copy(
                                                replyRows =
                                                    (it.replyRows ?: emptyList()) + listOf(reply),
                                            ),
                                        ),
                                )
                        }
                    feedState = LoadingState.Success(feedResponse)
                } else {
                    var response = (loadingState as? LoadingState.Success)?.response ?: emptyList()
                    response =
                        if (replyType == "feed") {
                            listOf(data) + response
                        } else {
                            response.map { item ->
                                if (item.id == (frid ?: replyId)) {
                                    item.copy(
                                        replyRows = (item.replyRows ?: emptyList()) + listOf(reply),
                                    )
                                } else item
                            }
                        }
                    loadingState = LoadingState.Success(response)
                }
            }
        }
    }

    override fun handleDeleteResponse(
        id: String,
        response: List<HomeFeedResponse.Data>,
    ): List<HomeFeedResponse.Data>? {
        return if (!frid.isNullOrEmpty()) {
            when (frid) {
                topId,
                meId -> {
                    var feedResponse = (feedState as LoadingState.Success).response
                    if (frid == topId)
                        feedResponse.topReplyRows?.getOrNull(0)?.let { reply ->
                            feedResponse =
                                feedResponse.copy(
                                    topReplyRows =
                                        listOf(
                                            reply.copy(
                                                replyRows =
                                                    reply.replyRows?.filterNot { it.id == id },
                                            ),
                                        ),
                                )
                        }
                    else
                        feedResponse.replyMeRows?.getOrNull(0)?.let { reply ->
                            feedResponse =
                                feedResponse.copy(
                                    replyMeRows =
                                        listOf(
                                            reply.copy(
                                                replyRows =
                                                    reply.replyRows?.filterNot { it.id == id },
                                            ),
                                        ),
                                )
                        }
                    feedState = LoadingState.Success(feedResponse)
                    null
                }

                else -> {
                    response.map { item ->
                        if (item.id == frid)
                            item.copy(replyRows = item.replyRows?.filterNot { it.id == id })
                        else item
                    }
                }
            }
        } else {
            if (id in listOf(topId, meId)) {
                var feedResponse = (feedState as LoadingState.Success).response
                feedResponse =
                    if (id == topId) feedResponse.copy(topReplyRows = null)
                    else feedResponse.copy(replyMeRows = null)
                feedState = LoadingState.Success(feedResponse)
                null
            } else super.handleDeleteResponse(id, response)
        }
    }

    fun refresh(id: String) {
        this.id = id
        resetParams()
        isRefreshing = false
        articleList = null
        itemSize = 2
        resetState()
        refresh()
    }

    private fun resetParams() {
        page = 1
        isEnd = false
        isLoadMore = false
        firstItem = null
        lastItem = null
    }

    fun resetState() {
        guestReplyGate.cancel()
        collections.reset()
        deleteJob?.cancel()
        voteJob?.cancel()
        submittingVote = false
        voteError = null
        isVoteFeed = false
        deletingFeed = false
        deletedFeedId = null
        isAccountCollected = false
        isFav = false
        feedJob?.cancel()
        cancelFetch()
        feedTypeName = EMPTY_STRING
        feedState = LoadingState.Loading
        loadingState = LoadingState.Loading
    }
}
