package com.snapnote.app

import android.app.Application
import android.content.ClipData
import android.os.Build
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapnote.app.asr.AsrServiceFactory
import com.snapnote.app.audio.PlaybackService
import com.snapnote.app.audio.RecordingService
import com.snapnote.app.data.AsrLogRepository
import com.snapnote.app.data.BackupService
import com.snapnote.app.data.FeedingQuickPhraseRepository
import com.snapnote.app.data.ChronicleRepository
import com.snapnote.app.data.FeedingRepository
import com.snapnote.app.asr.IflytekConfig
import com.snapnote.app.data.FeishuConfigRepository
import com.snapnote.app.data.IflytekCredentialsRepository
import com.snapnote.app.data.FeishuSyncMetaRepository
import com.snapnote.app.data.MealRepository
import com.snapnote.app.feishu.FeishuApiClient
import com.snapnote.app.feishu.FeishuSyncOptions
import com.snapnote.app.feishu.FeishuSyncScope
import com.snapnote.app.update.GithubReleaseSummary
import com.snapnote.app.update.GithubReleaseUpdateService
import com.snapnote.app.feishu.FeishuSyncService
import com.snapnote.app.data.MemoRepository
import com.snapnote.app.domain.MemoProcessService
import com.snapnote.app.llm.IflytekSparkConfig
import com.snapnote.app.llm.IflytekSparkProvider
import com.snapnote.app.llm.SparkPromptBuilder
import com.snapnote.app.model.AppFontPreset
import com.snapnote.app.model.AppFontSize
import com.snapnote.app.model.AppLanguage
import com.snapnote.app.model.AppScreen
import com.snapnote.app.model.AsrLogEntry
import com.snapnote.app.model.DailyMilkPoint
import com.snapnote.app.model.DailyMilkSeriesBuilder
import com.snapnote.app.model.DeletedChronicleItem
import com.snapnote.app.model.DeletedFeedingItem
import com.snapnote.app.model.FeishuConfig
import com.snapnote.app.model.DeletedMealItem
import com.snapnote.app.model.DeletedMemoItem
import com.snapnote.app.model.ChronicleFilter
import com.snapnote.app.model.ChronicleGroup
import com.snapnote.app.model.ChronicleItem
import com.snapnote.app.model.ChronicleSeriesBuilder
import com.snapnote.app.model.ChronicleType
import com.snapnote.app.model.ChronicleValuePoint
import com.snapnote.app.model.FeedingGroup
import com.snapnote.app.model.IflytekCredentials
import com.snapnote.app.model.FeedingItem
import com.snapnote.app.model.HomeMode
import com.snapnote.app.model.MealGroup
import com.snapnote.app.model.MealItem
import com.snapnote.app.model.MealSource
import com.snapnote.app.model.MemoConstants
import com.snapnote.app.model.MemoGroup
import com.snapnote.app.model.MemoItem
import com.snapnote.app.model.StorageInfo
import com.snapnote.app.model.AppThemeMode
import com.snapnote.app.model.FeedingStatsCardStyle
import com.snapnote.app.util.AppHaptics
import com.snapnote.app.util.DateUtils
import com.snapnote.app.util.MealImageStore
import com.snapnote.app.util.ChronicleExportFormatter
import com.snapnote.app.util.ChronicleImageStore
import com.snapnote.app.util.FeedingExportFormatter
import com.snapnote.app.util.MealExportFormatter
import com.snapnote.app.util.MemoExportFormatter
import com.snapnote.app.util.ShortcutHelper
import com.snapnote.app.vision.IflytekVisionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

enum class MealAiPhase {
    Idle,
    Streaming,
    Ready,
    Error
}

data class MealConfirmState(
    val id: String,
    val imagePath: String,
    val thumbnailPath: String,
    val timestamp: Long,
    val source: MealSource,
    val dishes: List<String> = emptyList(),
    val note: String = "",
    val recognizing: Boolean = true,
    val recognizeFailed: Boolean = false,
    val errorMessage: String? = null
)

data class MainUiState(
    val screen: AppScreen = AppScreen.Home,
    val homeMode: HomeMode = HomeMode.FEEDING,
    val groups: List<MemoGroup> = emptyList(),
    val mealGroups: List<MealGroup> = emptyList(),
    val feedingGroups: List<FeedingGroup> = emptyList(),
    val chronicleGroups: List<ChronicleGroup> = emptyList(),
    val chronicleFilter: ChronicleFilter = ChronicleFilter.ALL,
    val chronicleWeightPoints: List<ChronicleValuePoint> = emptyList(),
    val chronicleHeightPoints: List<ChronicleValuePoint> = emptyList(),
    val chronicleMilestones: List<ChronicleItem> = emptyList(),
    val trashItems: List<DeletedMemoItem> = emptyList(),
    val mealTrashItems: List<DeletedMealItem> = emptyList(),
    val feedingTrashItems: List<DeletedFeedingItem> = emptyList(),
    val chronicleTrashItems: List<DeletedChronicleItem> = emptyList(),
    val asrLogs: List<AsrLogEntry> = emptyList(),
    val isRecording: Boolean = false,
    val recordingDuration: String = "0:00",
    val playingId: String = "",
    val pausedId: String = "",
    val exportDateKey: String = "",
    val exportMemos: List<MemoItem> = emptyList(),
    val exportMeals: List<MealItem> = emptyList(),
    val exportFeedings: List<FeedingItem> = emptyList(),
    val exportChronicles: List<ChronicleItem> = emptyList(),
    val searchDateKey: String? = null,
    val searchKeyword: String = "",
    val searchResults: List<MemoGroup> = emptyList(),
    val mealSearchResults: List<MealGroup> = emptyList(),
    val feedingSearchResults: List<FeedingGroup> = emptyList(),
    val chronicleSearchResults: List<ChronicleGroup> = emptyList(),
    val editingMemoId: String = "",
    val editingMealId: String = "",
    val editingFeedingId: String = "",
    val editReturnScreen: AppScreen = AppScreen.Home,
    val searchReturnScreen: AppScreen = AppScreen.Home,
    val mineReturnScreen: AppScreen = AppScreen.Mine,
    val mealConfirm: MealConfirmState? = null,
    val showMealSourceSheet: Boolean = false,
    val showFeedingQuickAdd: Boolean = false,
    val showChronicleSheet: Boolean = false,
    val editingChronicleId: String = "",
    val lastFeedingAmountMl: Int? = null,
    val storageInfo: StorageInfo = StorageInfo(0, 0, 0, 0, 0),
    val mealAiPhase: MealAiPhase = MealAiPhase.Idle,
    val mealAiPreview: String = "",
    val mealAiError: String = "",
    val toastMessage: String? = null,
    val feishuConfig: FeishuConfig = FeishuConfig(),
    val iflytekCredentials: IflytekCredentials = IflytekCredentials(),
    val feishuSyncing: Boolean = false,
    val feedingNoteRecording: Boolean = false,
    val feedingNoteTranscribing: Boolean = false,
    val feedingNoteRecordingDuration: String = "0:00",
    val feedingNoteVoiceResult: String? = null,
    val feedingQuickPhrases: List<String> = emptyList(),
    val feedingDailyMilkPoints: List<DailyMilkPoint> = emptyList(),
    val githubUpdateLoadingLatest: Boolean = false,
    val githubUpdateDownloading: Boolean = false,
    val githubUpdateLatestName: String? = null,
    val githubUpdateLatestDateLabel: String? = null,
    val githubUpdateConfirm: GithubReleaseSummary? = null,
    val githubUpdatePendingInstallPath: String? = null,
    val appFontSize: AppFontSize = AppFontSize.MEDIUM,
    val appFontPreset: AppFontPreset = AppFontPreset.APP,
    val reduceMotion: Boolean = false,
    val showSecondsInListTime: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val feedingStatsCardStyle: FeedingStatsCardStyle = FeedingStatsCardStyle.STANDARD,
    val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.CHINESE
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MemoRepository(application)
    private val mealRepository = MealRepository(application)
    private val feedingRepository = FeedingRepository(application)
    private val chronicleRepository = ChronicleRepository(application)
    private val feedingQuickPhraseRepository = FeedingQuickPhraseRepository(application)
    private val asrLogRepository = AsrLogRepository(application)
    private val backupService = BackupService(application, repository)
    private val feishuConfigRepository = FeishuConfigRepository(application)
    private val iflytekCredentialsRepository = IflytekCredentialsRepository(application)
    private val feishuSyncMetaRepository = FeishuSyncMetaRepository(application)
    private val feishuApiClient = FeishuApiClient()
    private val feishuSyncService = FeishuSyncService(
        feishuApiClient,
        feishuSyncMetaRepository,
        repository,
        mealRepository,
        feedingRepository,
        chronicleRepository
    )
    private val githubReleaseUpdateService = GithubReleaseUpdateService(application)
    private val feedingRecordingService = RecordingService(application, viewModelScope) { seconds ->
        if (_uiState.value.feedingNoteRecording) {
            _uiState.update {
                it.copy(feedingNoteRecordingDuration = DateUtils.formatDuration(seconds))
            }
        }
    }
    private val visionProvider = IflytekVisionProvider()
    private val sparkProvider = IflytekSparkProvider()
    private val playbackService = PlaybackService(
        onError = { message -> _uiState.update { it.copy(toastMessage = message) } },
        onStateChanged = { refreshPlaybackState() }
    )
    private val processService = MemoProcessService(repository, AsrServiceFactory(asrLogRepository)) {
        refreshList()
        refreshLogs()
    }
    private val recordingService = RecordingService(application, viewModelScope) { seconds ->
        _uiState.update { it.copy(recordingDuration = DateUtils.formatDuration(seconds)) }
    }

    private val prefs = application.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /** 语言切换后通知 Activity 重建；API 33+ 由系统自动重建，无需此处触发。 */
    private val _recreateEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val recreateEvent: SharedFlow<Unit> = _recreateEvent.asSharedFlow()

    private var pendingMemoId: String = ""
    private var pendingAudioFile: File? = null
    private var pendingFeedingVoiceId: String = ""
    private var mealAiJob: Job? = null
    init {
        IflytekConfig.bind(iflytekCredentialsRepository)
        repository.load()
        mealRepository.load()
        feedingRepository.load()
        chronicleRepository.load()
        asrLogRepository.load()
        refreshList()
        refreshMealList()
        refreshFeedingList()
        refreshChronicleList()
        refreshTrash()
        refreshLogs()
        _uiState.update {
            it.copy(
                homeMode = loadSavedHomeMode(),
                appFontSize = loadSavedAppFontSize(),
                appFontPreset = loadSavedAppFontPreset(),
                reduceMotion = prefs.getBoolean(MemoConstants.PREF_KEY_REDUCE_MOTION, false),
                showSecondsInListTime = prefs.getBoolean(MemoConstants.PREF_KEY_SHOW_SECONDS_IN_LIST, false),
                hapticFeedbackEnabled = prefs.getBoolean(MemoConstants.PREF_KEY_HAPTIC_FEEDBACK, true),
                feedingStatsCardStyle = FeedingStatsCardStyle.fromStored(
                    prefs.getString(MemoConstants.PREF_KEY_FEEDING_STATS_CARD_STYLE, null)
                ),
                appThemeMode = AppThemeMode.fromStored(
                    prefs.getString(MemoConstants.PREF_KEY_APP_THEME_MODE, null)
                ),
                appLanguage = AppLanguage.fromStored(
                    prefs.getString(MemoConstants.PREF_KEY_APP_LANGUAGE, null)
                ),
                feishuConfig = feishuConfigRepository.load(),
                iflytekCredentials = iflytekCredentialsRepository.load(),
                feedingQuickPhrases = feedingQuickPhraseRepository.load(),
            )
        }
        viewModelScope.launch {
            prefetchFeishuToken()
            runStartupFeishuSync()
        }
    }

    /** 进入首页时预取飞书 token，减轻保存/手动同步时的鉴权等待 */
    fun onHomeVisible() {
        prefetchFeishuToken()
    }

    private fun prefetchFeishuToken() {
        val config = _uiState.value.feishuConfig
        if (!config.enabled || !config.isConfigured) return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { feishuApiClient.warmUpTenantToken(config.appId, config.appSecret) }
        }
    }

    /** 冷启动时若已配置飞书，自动执行近 7 天同步；全新安装无同步元数据时先全量拉取 */
    private suspend fun runStartupFeishuSync() {
        val config = _uiState.value.feishuConfig
        if (!config.enabled || !config.isConfigured || _uiState.value.feishuSyncing) return
        val scope = if (config.lastSyncAt == 0L && feishuSyncMetaRepository.getAll().isEmpty()) {
            FeishuSyncScope.Full
        } else {
            recentDaysSyncScope(RecentSyncDays.WEEK)
        }
        runFeishuSync(
            config = config,
            scope = scope,
            modules = setOf(_uiState.value.homeMode),
            showSuccessToast = false
        )
    }

    private object RecentSyncDays {
        const val WEEK = 7
        const val MONTH = 30
    }

    /** 含今天在内的近 N 天同步范围 */
    private fun recentDaysSyncScope(days: Int): FeishuSyncScope {
        return FeishuSyncScope.DateKeys(DateUtils.recentDateKeys(days))
    }

    private fun dateKeysScope(vararg dateKeys: String): FeishuSyncScope {
        val keys = dateKeys.filter { it.isNotBlank() }.toSet()
        return if (keys.isEmpty()) recentDaysSyncScope(RecentSyncDays.WEEK) else FeishuSyncScope.DateKeys(keys)
    }

    private fun loadSavedHomeMode(): HomeMode {
        val saved = prefs.getString(MemoConstants.PREF_KEY_HOME_MODE, HomeMode.FEEDING.name)
        return runCatching { HomeMode.valueOf(saved!!) }.getOrDefault(HomeMode.FEEDING)
    }

    private fun persistHomeMode(mode: HomeMode) {
        prefs.edit().putString(MemoConstants.PREF_KEY_HOME_MODE, mode.name).apply()
    }

    private fun loadSavedAppFontSize(): AppFontSize {
        return AppFontSize.fromStored(prefs.getString(MemoConstants.PREF_KEY_APP_FONT_SIZE, null))
    }

    private fun persistAppFontSize(size: AppFontSize) {
        prefs.edit().putString(MemoConstants.PREF_KEY_APP_FONT_SIZE, size.name).apply()
    }

    fun setAppFontSize(size: AppFontSize) {
        if (_uiState.value.appFontSize == size) return
        _uiState.update { it.copy(appFontSize = size) }
        persistAppFontSize(size)
    }

    private fun loadSavedAppFontPreset(): AppFontPreset {
        return AppFontPreset.fromStored(prefs.getString(MemoConstants.PREF_KEY_APP_FONT_PRESET, null))
    }

    private fun persistAppFontPreset(preset: AppFontPreset) {
        prefs.edit().putString(MemoConstants.PREF_KEY_APP_FONT_PRESET, preset.name).apply()
    }

    fun setAppFontPreset(preset: AppFontPreset) {
        if (_uiState.value.appFontPreset == preset) return
        _uiState.update { it.copy(appFontPreset = preset) }
        persistAppFontPreset(preset)
    }

    fun setReduceMotion(enabled: Boolean) {
        if (_uiState.value.reduceMotion == enabled) return
        _uiState.update { it.copy(reduceMotion = enabled) }
        prefs.edit().putBoolean(MemoConstants.PREF_KEY_REDUCE_MOTION, enabled).apply()
    }

    fun setShowSecondsInListTime(enabled: Boolean) {
        if (_uiState.value.showSecondsInListTime == enabled) return
        _uiState.update { it.copy(showSecondsInListTime = enabled) }
        prefs.edit().putBoolean(MemoConstants.PREF_KEY_SHOW_SECONDS_IN_LIST, enabled).apply()
    }

    fun openSystemSettings() {
        _uiState.update { it.copy(screen = AppScreen.SystemSettings, mineReturnScreen = AppScreen.Mine) }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        if (_uiState.value.hapticFeedbackEnabled == enabled) return
        _uiState.update { it.copy(hapticFeedbackEnabled = enabled) }
        prefs.edit().putBoolean(MemoConstants.PREF_KEY_HAPTIC_FEEDBACK, enabled).apply()
        if (enabled) hapticSuccessIfEnabled()
    }

    fun setFeedingStatsCardStyle(style: FeedingStatsCardStyle) {
        if (_uiState.value.feedingStatsCardStyle == style) return
        _uiState.update { it.copy(feedingStatsCardStyle = style) }
        prefs.edit().putString(MemoConstants.PREF_KEY_FEEDING_STATS_CARD_STYLE, style.name).apply()
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        if (_uiState.value.appThemeMode == mode) return
        _uiState.update { it.copy(appThemeMode = mode) }
        prefs.edit().putString(MemoConstants.PREF_KEY_APP_THEME_MODE, mode.name).apply()
    }

    /** 切换应用语言：持久化 + AppCompat 全局 locale + 必要的 Activity 重建。 */
    fun setAppLanguage(language: AppLanguage) {
        if (_uiState.value.appLanguage == language) return
        _uiState.update { it.copy(appLanguage = language) }
        prefs.edit().putString(MemoConstants.PREF_KEY_APP_LANGUAGE, language.storedValue).apply()

        val locales = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.create(Locale.forLanguageTag(language.localeTag))
        }
        AppCompatDelegate.setApplicationLocales(locales)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            _recreateEvent.tryEmit(Unit)
        }
    }

    private fun hapticSuccessIfEnabled() {
        if (!_uiState.value.hapticFeedbackEnabled) return
        AppHaptics.performSuccess(getApplication())
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun showComingSoon() {
        _uiState.update { it.copy(toastMessage = "即将推出") }
    }

    fun setHomeMode(mode: HomeMode) {
        if (_uiState.value.homeMode == mode) return
        _uiState.update { state ->
            state.copy(
                homeMode = mode,
                showMealSourceSheet = false,
                showFeedingQuickAdd = false,
                showChronicleSheet = false,
                editingChronicleId = ""
            )
        }
        persistHomeMode(mode)
        if (_uiState.value.screen == AppScreen.RecordExport) {
            refreshExportData(_uiState.value.exportDateKey)
        }
    }

    fun toggleHomeMode() {
        val next = when (_uiState.value.homeMode) {
            HomeMode.NOTE -> HomeMode.MEAL
            HomeMode.MEAL -> HomeMode.FEEDING
            HomeMode.FEEDING -> HomeMode.CHRONICLE
            HomeMode.CHRONICLE -> HomeMode.NOTE
        }
        setHomeMode(next)
    }

    fun setChronicleFilter(filter: ChronicleFilter) {
        _uiState.update { it.copy(chronicleFilter = filter) }
    }

    fun openChronicleSheet() {
        _uiState.update { it.copy(showChronicleSheet = true, editingChronicleId = "") }
    }

    fun dismissChronicleSheet() {
        _uiState.update { it.copy(showChronicleSheet = false, editingChronicleId = "") }
    }

    fun openChronicleDetail(chronicleId: String, from: AppScreen) {
        if (chronicleRepository.getById(chronicleId) == null) return
        _uiState.update {
            it.copy(
                editingChronicleId = chronicleId,
                showChronicleSheet = true,
                editReturnScreen = from
            )
        }
    }

    fun getEditingChronicle(): ChronicleItem? =
        chronicleRepository.getById(_uiState.value.editingChronicleId)

    fun submitChronicleRecord(
        type: ChronicleType,
        value: Double?,
        title: String,
        body: String,
        imagePaths: List<String>,
        timestamp: Long
    ) {
        if (_uiState.value.editingChronicleId.isNotBlank()) {
            saveChronicleEdit(type, value, title, body, imagePaths, timestamp)
        } else {
            saveChronicle(type, value, title, body, imagePaths, timestamp)
        }
    }

    private fun saveChronicle(
        type: ChronicleType,
        value: Double?,
        title: String,
        body: String,
        imagePaths: List<String>,
        timestamp: Long
    ) {
        if (!validateChronicleInput(type, value, title, body, imagePaths)) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dateKey = DateUtils.toDateKey(timestamp)
            val item = ChronicleItem(
                id = now.toString(),
                type = type,
                timestamp = timestamp,
                dateKey = dateKey,
                value = value?.let { roundChronicleValue(it) },
                title = title,
                body = body,
                imagePaths = ChronicleImageStore.trimToMax(imagePaths),
                updatedAt = now
            )
            chronicleRepository.add(item)
            hapticSuccessIfEnabled()
            refreshChronicleList()
            refreshSearchResults()
            _uiState.update {
                it.copy(
                    showChronicleSheet = false,
                    editingChronicleId = "",
                    homeMode = HomeMode.CHRONICLE,
                    toastMessage = "已保存"
                )
            }
            persistHomeMode(HomeMode.CHRONICLE)
            syncFeishuAfterSave(HomeMode.CHRONICLE, "已保存", dateKey, localId = item.id)
        }
    }

    private fun saveChronicleEdit(
        type: ChronicleType,
        value: Double?,
        title: String,
        body: String,
        imagePaths: List<String>,
        timestamp: Long
    ) {
        val chronicleId = _uiState.value.editingChronicleId
        val existing = chronicleRepository.getById(chronicleId) ?: return
        if (type != existing.type) return
        if (!validateChronicleInput(type, value, title, body, imagePaths)) return
        viewModelScope.launch {
            val removedImages = existing.imagePaths.filter { it !in imagePaths }
            ChronicleImageStore.deletePaths(removedImages)
            val now = System.currentTimeMillis()
            val newDateKey = DateUtils.toDateKey(timestamp)
            chronicleRepository.update(
                existing.copy(
                    timestamp = timestamp,
                    dateKey = newDateKey,
                    value = value?.let { roundChronicleValue(it) },
                    title = title,
                    body = body,
                    imagePaths = ChronicleImageStore.trimToMax(imagePaths),
                    updatedAt = now
                )
            )
            hapticSuccessIfEnabled()
            refreshChronicleList()
            refreshSearchResults()
            _uiState.update {
                it.copy(
                    showChronicleSheet = false,
                    editingChronicleId = "",
                    toastMessage = "已保存"
                )
            }
            syncFeishuAfterSave(
                HomeMode.CHRONICLE,
                "已保存",
                existing.dateKey,
                newDateKey,
                localId = chronicleId
            )
        }
    }

    private fun validateChronicleInput(
        type: ChronicleType,
        value: Double?,
        title: String,
        body: String,
        imagePaths: List<String>
    ): Boolean {
        return when (type) {
            ChronicleType.WEIGHT, ChronicleType.HEIGHT -> {
                if (value == null || value <= 0.0) {
                    _uiState.update { it.copy(toastMessage = "请填写有效数值") }
                    false
                } else {
                    true
                }
            }
            ChronicleType.MILESTONE -> {
                if (title.isBlank() && body.isBlank() && imagePaths.isEmpty()) {
                    _uiState.update { it.copy(toastMessage = "请填写标题、正文或添加图片") }
                    false
                } else {
                    true
                }
            }
        }
    }

    private fun roundChronicleValue(value: Double): Double =
        com.snapnote.app.util.ChronicleFormat.parseDecimalInput(
            com.snapnote.app.util.ChronicleFormat.formatDecimal(value)
        ) ?: value

    fun deleteChronicle(id: String) {
        viewModelScope.launch {
            chronicleRepository.moveToTrash(id)
            refreshChronicleList()
            refreshTrash()
            refreshSearchResults()
            if (_uiState.value.editingChronicleId == id) {
                _uiState.update {
                    it.copy(
                        showChronicleSheet = false,
                        editingChronicleId = "",
                        toastMessage = "已移入回收站"
                    )
                }
            } else {
                _uiState.update { it.copy(toastMessage = "已移入回收站") }
            }
        }
    }

    fun openFeedingQuickAdd() {
        _uiState.update { it.copy(showFeedingQuickAdd = true, editingFeedingId = "") }
        viewModelScope.launch {
            val phrases = feedingQuickPhraseRepository.load()
            _uiState.update { state ->
                if (state.showFeedingQuickAdd) {
                    state.copy(feedingQuickPhrases = phrases)
                } else {
                    state
                }
            }
        }
    }

    fun dismissFeedingQuickAdd() {
        cancelFeedingNoteVoice()
        _uiState.update { it.copy(showFeedingQuickAdd = false, editingFeedingId = "") }
    }

    /**
     * 提交育儿记录。onComplete 在本地落盘后回调：true=成功（可关闭页面），false=失败（保留页面）。
     * 飞书同步在落盘后另行触发，不阻塞页面关闭。
     */
    fun submitFeedingRecord(
        amountMl: Int?,
        note: String,
        timestamp: Long,
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (_uiState.value.editingFeedingId.isNotBlank()) {
            saveFeedingEdit(amountMl, note, timestamp, onComplete)
        } else {
            saveFeeding(amountMl, note, timestamp, onComplete)
        }
    }

    private fun cancelFeedingNoteVoice() {
        if (feedingRecordingService.isRecording()) {
            feedingRecordingService.cancel()
        }
        _uiState.update {
            it.copy(
                feedingNoteRecording = false,
                feedingNoteTranscribing = false,
                feedingNoteRecordingDuration = "0:00"
            )
        }
    }

    fun openMealSourceSheet() {
        _uiState.update { it.copy(showMealSourceSheet = true) }
    }

    fun dismissMealSourceSheet() {
        _uiState.update { it.copy(showMealSourceSheet = false) }
    }

    fun openHomeLeftAction() {
        when (_uiState.value.homeMode) {
            HomeMode.CHRONICLE -> openChronicleStats()
            HomeMode.FEEDING -> openFeedingStats()
            else -> openSearch(returnScreen = AppScreen.Home)
        }
    }

    fun openSearchFromMine() {
        openSearch(returnScreen = AppScreen.Mine)
    }

    fun openSearch(returnScreen: AppScreen = AppScreen.Home) {
        val mode = _uiState.value.homeMode
        _uiState.update {
            it.copy(
                screen = AppScreen.Search,
                searchReturnScreen = returnScreen,
                searchDateKey = null,
                searchKeyword = "",
                searchResults = if (mode == HomeMode.NOTE) repository.search(null, "") else emptyList(),
                mealSearchResults = if (mode == HomeMode.MEAL) mealRepository.search(null, "") else emptyList(),
                feedingSearchResults = if (mode == HomeMode.FEEDING) feedingRepository.search(null, "") else emptyList(),
                chronicleSearchResults = if (mode == HomeMode.CHRONICLE) {
                    chronicleRepository.search(null, "")
                } else {
                    emptyList()
                }
            )
        }
        refreshPlaybackState()
    }

    fun openChronicleStats() {
        refreshChronicleList()
        _uiState.update { it.copy(screen = AppScreen.ChronicleStats) }
    }

    fun openFeedingStats() {
        refreshFeedingList()
        _uiState.update { it.copy(screen = AppScreen.FeedingStats) }
    }

    fun updateSearchDate(dateKey: String?) {
        _uiState.update { state ->
            when (state.homeMode) {
                HomeMode.MEAL -> state.copy(
                    searchDateKey = dateKey,
                    mealSearchResults = mealRepository.search(dateKey, state.searchKeyword)
                )
                HomeMode.FEEDING -> state.copy(
                    searchDateKey = dateKey,
                    feedingSearchResults = feedingRepository.search(dateKey, state.searchKeyword)
                )
                HomeMode.CHRONICLE -> state.copy(
                    searchDateKey = dateKey,
                    chronicleSearchResults = chronicleRepository.search(dateKey, state.searchKeyword)
                )
                HomeMode.NOTE -> state.copy(
                    searchDateKey = dateKey,
                    searchResults = repository.search(dateKey, state.searchKeyword)
                )
            }
        }
    }

    fun updateSearchKeyword(keyword: String) {
        _uiState.update { state ->
            when (state.homeMode) {
                HomeMode.MEAL -> state.copy(
                    searchKeyword = keyword,
                    mealSearchResults = mealRepository.search(state.searchDateKey, keyword)
                )
                HomeMode.FEEDING -> state.copy(
                    searchKeyword = keyword,
                    feedingSearchResults = feedingRepository.search(state.searchDateKey, keyword)
                )
                HomeMode.CHRONICLE -> state.copy(
                    searchKeyword = keyword,
                    chronicleSearchResults = chronicleRepository.search(state.searchDateKey, keyword)
                )
                HomeMode.NOTE -> state.copy(
                    searchKeyword = keyword,
                    searchResults = repository.search(state.searchDateKey, keyword)
                )
            }
        }
    }

    fun navigateBack() {
        val confirm = _uiState.value.mealConfirm
        if (_uiState.value.screen == AppScreen.MealConfirm && confirm != null) {
            cancelMealConfirm()
            return
        }
        if (_uiState.value.screen == AppScreen.MealDetail) {
            cancelMealAiAnalyze()
        }
        _uiState.update { state ->
            state.copy(
                screen = when (state.screen) {
                    AppScreen.LogList, AppScreen.RecycleBin, AppScreen.RecordExport,
                    AppScreen.StorageManage, AppScreen.FeishuSyncSettings,
                    AppScreen.GithubReleaseUpdate,
                    AppScreen.IflytekSettings,
                    AppScreen.FeedingQuickPhrases,
                    AppScreen.SystemSettings -> state.mineReturnScreen
                    AppScreen.MineFeishuHub, AppScreen.MineDataHub, AppScreen.MineIflytekHub ->
                        AppScreen.Mine
                    AppScreen.Search -> state.searchReturnScreen
                    AppScreen.ChronicleStats, AppScreen.FeedingStats -> AppScreen.Home
                    AppScreen.MemoEdit -> state.editReturnScreen
                    AppScreen.MealDetail -> state.editReturnScreen
                    AppScreen.MealConfirm -> AppScreen.Home
                    AppScreen.Mine -> AppScreen.Home
                    AppScreen.Home -> AppScreen.Home
                }
            )
        }
    }

    fun openMine() {
        refreshTrash()
        _uiState.update {
            it.copy(
                screen = AppScreen.Mine,
                mineReturnScreen = AppScreen.Mine,
                feedingQuickPhrases = feedingQuickPhraseRepository.load()
            )
        }
    }

    fun goHome() {
        _uiState.update { it.copy(screen = AppScreen.Home) }
    }

    fun openMineFeishuHub() {
        _uiState.update { it.copy(screen = AppScreen.MineFeishuHub) }
    }

    fun openMineDataHub() {
        refreshTrash()
        _uiState.update { it.copy(screen = AppScreen.MineDataHub) }
    }

    fun openMineIflytekHub() {
        _uiState.update { it.copy(screen = AppScreen.MineIflytekHub) }
    }

    fun openLogList(returnTo: AppScreen = AppScreen.MineIflytekHub) {
        refreshLogs()
        _uiState.update { it.copy(screen = AppScreen.LogList, mineReturnScreen = returnTo) }
    }

    fun openRecycleBin(returnTo: AppScreen = AppScreen.MineDataHub) {
        refreshTrash()
        _uiState.update { it.copy(screen = AppScreen.RecycleBin, mineReturnScreen = returnTo) }
    }

    fun openRecordExport(returnTo: AppScreen = AppScreen.MineDataHub) {
        val mode = _uiState.value.homeMode
        val defaultDate = defaultExportDateKey(mode)
        _uiState.update {
            it.copy(
                screen = AppScreen.RecordExport,
                exportDateKey = defaultDate,
                mineReturnScreen = returnTo
            )
        }
        refreshExportData(defaultDate)
    }

    fun openStorageManage(returnTo: AppScreen = AppScreen.MineDataHub) {
        refreshStorageInfo()
        _uiState.update { it.copy(screen = AppScreen.StorageManage, mineReturnScreen = returnTo) }
    }

    fun openFeishuSyncSettings(returnTo: AppScreen = AppScreen.MineFeishuHub) {
        _uiState.update {
            it.copy(
                screen = AppScreen.FeishuSyncSettings,
                mineReturnScreen = returnTo,
                feishuConfig = feishuConfigRepository.load()
            )
        }
    }

    fun openIflytekSettings(returnTo: AppScreen = AppScreen.MineIflytekHub) {
        _uiState.update {
            it.copy(
                screen = AppScreen.IflytekSettings,
                mineReturnScreen = returnTo,
                iflytekCredentials = iflytekCredentialsRepository.load()
            )
        }
    }

    fun openGithubReleaseUpdate(returnTo: AppScreen = AppScreen.Mine) {
        _uiState.update {
            it.copy(
                screen = AppScreen.GithubReleaseUpdate,
                mineReturnScreen = returnTo,
                githubUpdateConfirm = null
            )
        }
    }

    fun onGithubUpdateScreenVisible() {
        refreshGithubLatestRelease()
    }

    private var githubLatestSummary: GithubReleaseSummary? = null

    private fun refreshGithubLatestRelease() {
        viewModelScope.launch {
            _uiState.update { it.copy(githubUpdateLoadingLatest = true) }
            try {
                val summary = withContext(Dispatchers.IO) {
                    githubReleaseUpdateService.fetchLatest()
                }
                githubLatestSummary = summary
                _uiState.update {
                    it.copy(
                        githubUpdateLoadingLatest = false,
                        githubUpdateLatestName = summary.displayName,
                        githubUpdateLatestDateLabel = summary.dateLabel
                    )
                }
            } catch (e: Throwable) {
                githubLatestSummary = null
                val message = e.message?.take(200) ?: e.javaClass.simpleName
                _uiState.update {
                    it.copy(
                        githubUpdateLoadingLatest = false,
                        githubUpdateLatestName = null,
                        githubUpdateLatestDateLabel = null,
                        toastMessage = "获取发行版失败：$message"
                    )
                }
            }
        }
    }

    fun requestGithubDownload() {
        viewModelScope.launch {
            var summary = githubLatestSummary
            if (summary == null) {
                _uiState.update { it.copy(githubUpdateLoadingLatest = true) }
                try {
                    summary = withContext(Dispatchers.IO) {
                        githubReleaseUpdateService.fetchLatest()
                    }
                    githubLatestSummary = summary
                    _uiState.update {
                        it.copy(
                            githubUpdateLoadingLatest = false,
                            githubUpdateLatestName = summary.displayName,
                            githubUpdateLatestDateLabel = summary.dateLabel
                        )
                    }
                } catch (e: Throwable) {
                    val message = e.message?.take(200) ?: e.javaClass.simpleName
                    _uiState.update {
                        it.copy(
                            githubUpdateLoadingLatest = false,
                            toastMessage = "获取发行版失败：$message"
                        )
                    }
                    return@launch
                }
            }
            if (!summary.hasApk) {
                _uiState.update {
                    it.copy(toastMessage = "该发行版未附带 APK，请前往 GitHub Releases 页面手动下载")
                }
                return@launch
            }
            _uiState.update { it.copy(githubUpdateConfirm = summary) }
        }
    }

    fun dismissGithubDownloadConfirm() {
        _uiState.update { it.copy(githubUpdateConfirm = null) }
    }

    fun confirmGithubDownload() {
        val summary = _uiState.value.githubUpdateConfirm ?: githubLatestSummary
        if (summary == null) {
            _uiState.update { it.copy(toastMessage = "无法下载：缺少发行版信息") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(githubUpdateDownloading = true, githubUpdateConfirm = null) }
            try {
                val file = withContext(Dispatchers.IO) {
                    githubReleaseUpdateService.downloadLatestApk(summary)
                }
                _uiState.update {
                    it.copy(
                        githubUpdateDownloading = false,
                        githubUpdatePendingInstallPath = file.absolutePath,
                        toastMessage = "下载完成，正在打开安装"
                    )
                }
            } catch (e: Throwable) {
                val message = e.message?.take(200) ?: e.javaClass.simpleName
                _uiState.update {
                    it.copy(
                        githubUpdateDownloading = false,
                        toastMessage = "下载失败：$message"
                    )
                }
            }
        }
    }

    fun consumeGithubPendingInstall() {
        _uiState.update { it.copy(githubUpdatePendingInstallPath = null) }
    }

    fun saveIflytekCredentials(credentials: IflytekCredentials) {
        if (!credentials.isConfigured()) {
            _uiState.update { it.copy(toastMessage = "请填写完整的 APPID、APIKey 和 APISecret") }
            return
        }
        iflytekCredentialsRepository.save(credentials)
        _uiState.update {
            it.copy(
                iflytekCredentials = credentials,
                toastMessage = "讯飞密钥已保存"
            )
        }
    }

    fun openFeedingQuickPhrases(returnTo: AppScreen = AppScreen.Mine) {
        _uiState.update {
            it.copy(
                screen = AppScreen.FeedingQuickPhrases,
                mineReturnScreen = returnTo,
                feedingQuickPhrases = feedingQuickPhraseRepository.load()
            )
        }
    }

    fun addFeedingQuickPhrase(text: String) {
        val phrase = text.trim()
        if (phrase.isEmpty()) return
        val updated = _uiState.value.feedingQuickPhrases + phrase
        feedingQuickPhraseRepository.save(updated)
        _uiState.update { it.copy(feedingQuickPhrases = updated, toastMessage = "已添加") }
    }

    fun updateFeedingQuickPhrase(index: Int, text: String) {
        val phrase = text.trim()
        if (phrase.isEmpty()) return
        val current = _uiState.value.feedingQuickPhrases.toMutableList()
        if (index !in current.indices) return
        current[index] = phrase
        feedingQuickPhraseRepository.save(current)
        _uiState.update { it.copy(feedingQuickPhrases = current, toastMessage = "已保存") }
    }

    fun deleteFeedingQuickPhrase(index: Int) {
        val current = _uiState.value.feedingQuickPhrases.toMutableList()
        if (index !in current.indices) return
        current.removeAt(index)
        feedingQuickPhraseRepository.save(current)
        _uiState.update { it.copy(feedingQuickPhrases = current, toastMessage = "已删除") }
    }

    fun saveFeishuConfig(config: FeishuConfig) {
        if (config.enabled && !config.isConfigured) {
            _uiState.update { it.copy(toastMessage = "请先填写 App ID、App Secret 和 Base App Token") }
            return
        }
        if (config.enabled && config.configuredTableCount == 0) {
            _uiState.update { it.copy(toastMessage = "请至少填写一张数据表的 ID") }
            return
        }
        viewModelScope.launch {
            feishuConfigRepository.save(config)
            _uiState.update { it.copy(feishuConfig = config) }
            if (config.enabled && config.isConfigured) {
                runFeishuSync(
                    config = config,
                    scope = recentDaysSyncScope(RecentSyncDays.WEEK),
                    modules = setOf(_uiState.value.homeMode),
                    showSuccessToast = true,
                    savedToast = "飞书配置已保存"
                )
            } else {
                _uiState.update { it.copy(toastMessage = "飞书配置已保存") }
            }
        }
    }

    /** 近 7 天同步（首页下拉刷新、配置页「同步近7天」共用） */
    fun syncFeishuNow() {
        val config = _uiState.value.feishuConfig
        if (!config.enabled) {
            _uiState.update { it.copy(toastMessage = "请先启用飞书同步") }
            return
        }
        if (!config.isConfigured) {
            _uiState.update { it.copy(toastMessage = "请先保存完整的飞书配置") }
            return
        }
        if (_uiState.value.feishuSyncing) return
        val mode = _uiState.value.homeMode
        viewModelScope.launch {
            runFeishuSync(
                config = config,
                scope = recentDaysSyncScope(RecentSyncDays.WEEK),
                modules = setOf(mode),
                showSuccessToast = true,
                incrementalLabel = "${homeModeSyncLabel(mode)}·近7天"
            )
        }
    }

    /** 近 30 天同步（配置页「同步近30天」） */
    fun syncFeishuLast30Days() {
        val config = _uiState.value.feishuConfig
        if (!config.enabled) {
            _uiState.update { it.copy(toastMessage = "请先启用飞书同步") }
            return
        }
        if (!config.isConfigured) {
            _uiState.update { it.copy(toastMessage = "请先保存完整的飞书配置") }
            return
        }
        if (_uiState.value.feishuSyncing) return
        val mode = _uiState.value.homeMode
        viewModelScope.launch {
            runFeishuSync(
                config = config,
                scope = recentDaysSyncScope(RecentSyncDays.MONTH),
                modules = setOf(mode),
                showSuccessToast = true,
                incrementalLabel = "${homeModeSyncLabel(mode)}·近30天"
            )
        }
    }

    fun syncFeishuFull() {
        val config = _uiState.value.feishuConfig
        if (!config.enabled) {
            _uiState.update { it.copy(toastMessage = "请先启用飞书同步") }
            return
        }
        if (!config.isConfigured) {
            _uiState.update { it.copy(toastMessage = "请先保存完整的飞书配置") }
            return
        }
        if (_uiState.value.feishuSyncing) return
        val mode = _uiState.value.homeMode
        viewModelScope.launch {
            runFeishuSync(
                config = config,
                scope = FeishuSyncScope.Full,
                modules = setOf(mode),
                showSuccessToast = true,
                incrementalLabel = "${homeModeSyncLabel(mode)}·全量"
            )
        }
    }

    private fun syncFeishuIfEnabled() {
        val config = _uiState.value.feishuConfig
        if (!config.enabled || !config.isConfigured || _uiState.value.feishuSyncing) return
        viewModelScope.launch {
            runFeishuSync(
                config = config,
                scope = recentDaysSyncScope(RecentSyncDays.WEEK),
                modules = setOf(HomeMode.NOTE),
                showSuccessToast = false
            )
        }
    }

    private suspend fun syncFeishuAfterSave(
        mode: HomeMode,
        savedToast: String,
        vararg dateKeys: String,
        localId: String? = null
    ) {
        val config = _uiState.value.feishuConfig
        if (!config.enabled || !config.isConfigured) {
            hapticSuccessIfEnabled()
            _uiState.update { it.copy(toastMessage = savedToast) }
            return
        }
        val pushIds = localId?.let { setOf(it) }
        runFeishuSync(
            config = config,
            scope = dateKeysScope(*dateKeys),
            modules = setOf(mode),
            options = FeishuSyncOptions(pullRemote = false, pushOnlyLocalIds = pushIds),
            showSuccessToast = false,
            savedToast = savedToast,
            showSyncingIndicator = false,
            refreshLists = false
        )
    }

    private fun homeModeSyncLabel(mode: HomeMode): String = when (mode) {
        HomeMode.NOTE -> "语音笔记"
        HomeMode.MEAL -> "饭记"
        HomeMode.FEEDING -> "育儿记"
        HomeMode.CHRONICLE -> "大事记"
    }

    private suspend fun deleteFromFeishu(mode: HomeMode, localIds: List<String>) {
        if (localIds.isEmpty()) return
        val config = _uiState.value.feishuConfig
        if (!config.enabled || !config.isConfigured) return
        withContext(Dispatchers.IO) {
            feishuSyncService.deleteRemoteRecords(config, mode, localIds)
        }
    }

    private suspend fun runFeishuSync(
        config: FeishuConfig,
        scope: FeishuSyncScope,
        modules: Set<HomeMode>? = null,
        options: FeishuSyncOptions = FeishuSyncOptions(),
        showSuccessToast: Boolean,
        savedToast: String? = null,
        incrementalLabel: String? = null,
        showSyncingIndicator: Boolean = true,
        refreshLists: Boolean = true
    ) {
        if (showSyncingIndicator && _uiState.value.feishuSyncing) return
        if (showSyncingIndicator) {
            _uiState.update { it.copy(feishuSyncing = true) }
        }
        var toast: String? = null
        var updatedConfig = config
        val targetModules = modules ?: setOf(
            HomeMode.NOTE,
            HomeMode.MEAL,
            HomeMode.FEEDING,
            HomeMode.CHRONICLE
        )
        try {
            val result = withContext(Dispatchers.IO) {
                feishuSyncService.sync(config, scope, modules, options)
            }
            val now = System.currentTimeMillis()
            updatedConfig = config.copy(lastSyncAt = now)
            feishuConfigRepository.save(updatedConfig)
            if (refreshLists) {
                if (HomeMode.NOTE in targetModules) {
                    refreshList()
                }
                if (HomeMode.MEAL in targetModules) {
                    refreshMealList()
                }
                if (HomeMode.FEEDING in targetModules) {
                    refreshFeedingList()
                }
                if (HomeMode.CHRONICLE in targetModules) {
                    refreshChronicleList()
                }
                refreshSearchResults()
            }
            val scopeLabel = incrementalLabel ?: when (scope) {
                FeishuSyncScope.Full -> "全量"
                is FeishuSyncScope.DateKeys -> "按日"
            }
            val stats = "拉取 ${result.pulled} 条，上传 ${result.pushed} 条"
            toast = when {
                showSuccessToast && savedToast != null ->
                    "$savedToast，$scopeLabel 已同步（$stats）"
                showSuccessToast ->
                    "$scopeLabel 同步完成：$stats"
                savedToast != null -> savedToast
                else -> null
            }
        } catch (e: Throwable) {
            val message = e.message?.take(200) ?: e.javaClass.simpleName
            toast = when {
                savedToast != null -> "$savedToast（同步失败：$message）"
                else -> "同步失败：$message"
            }
        } finally {
            _uiState.update {
                it.copy(
                    feishuConfig = updatedConfig,
                    feishuSyncing = if (showSyncingIndicator) false else it.feishuSyncing,
                    toastMessage = toast
                )
            }
        }
    }

    fun toggleFeedingNoteVoice() {
        if (_uiState.value.feedingNoteTranscribing) return
        if (_uiState.value.isRecording) {
            _uiState.update { it.copy(toastMessage = "请先结束语音笔记录音") }
            return
        }
        if (_uiState.value.feedingNoteRecording) {
            stopFeedingNoteVoice()
        } else {
            startFeedingNoteVoice()
        }
    }

    fun clearFeedingNoteVoiceResult() {
        _uiState.update { it.copy(feedingNoteVoiceResult = null) }
    }

    private fun startFeedingNoteVoice() {
        if (!IflytekConfig.isConfigured()) {
            _uiState.update { it.copy(toastMessage = IflytekConfig.NOT_CONFIGURED_HINT) }
            return
        }
        if (feedingRecordingService.isRecording()) return
        pendingFeedingVoiceId = System.currentTimeMillis().toString()
        val cacheDir = File(getApplication<Application>().cacheDir, "feeding_asr").apply { mkdirs() }
        val wavFile = File(cacheDir, "$pendingFeedingVoiceId.wav")
        val m4aFile = File(cacheDir, "$pendingFeedingVoiceId.m4a")
        _uiState.update {
            it.copy(
                feedingNoteRecording = true,
                feedingNoteRecordingDuration = "0:00"
            )
        }
        if (!feedingRecordingService.start(wavFile, m4aFile)) {
            _uiState.update {
                it.copy(
                    feedingNoteRecording = false,
                    toastMessage = "无法启动录音，请检查麦克风权限"
                )
            }
        }
    }

    private fun stopFeedingNoteVoice() {
        if (!feedingRecordingService.isRecording()) {
            _uiState.update { it.copy(feedingNoteRecording = false) }
            return
        }
        _uiState.update { it.copy(feedingNoteRecording = false, feedingNoteTranscribing = true) }
        val result = feedingRecordingService.stop()
        viewModelScope.launch {
            try {
                if (result == null) {
                    _uiState.update {
                        it.copy(
                            feedingNoteTranscribing = false,
                            toastMessage = "录音太短，请再说一次"
                        )
                    }
                    return@launch
                }
                val text = withContext(Dispatchers.IO) {
                    AsrServiceFactory(asrLogRepository).create().transcribe(
                        result.asrWavPath?.takeIf { File(it).exists() } ?: result.path
                    )
                }
                result.asrWavPath?.let { sidecar ->
                    if (sidecar != result.path) File(sidecar).delete()
                }
                File(result.path).delete()
                if (text.isBlank()) {
                    _uiState.update {
                        it.copy(
                            feedingNoteTranscribing = false,
                            toastMessage = "未识别到文字"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            feedingNoteTranscribing = false,
                            feedingNoteVoiceResult = text.trim()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        feedingNoteTranscribing = false,
                        toastMessage = e.message ?: "转写失败"
                    )
                }
            }
        }
    }

    fun openMemoEdit(memoId: String, from: AppScreen) {
        _uiState.update {
            it.copy(
                screen = AppScreen.MemoEdit,
                editingMemoId = memoId,
                editReturnScreen = from
            )
        }
    }

    fun openMealDetail(mealId: String, from: AppScreen) {
        cancelMealAiAnalyze()
        _uiState.update {
            it.copy(
                screen = AppScreen.MealDetail,
                editingMealId = mealId,
                editReturnScreen = from
            )
        }
    }

    fun openFeedingDetail(feedingId: String, from: AppScreen) {
        if (feedingRepository.getById(feedingId) == null) return
        cancelFeedingNoteVoice()
        _uiState.update {
            it.copy(
                editingFeedingId = feedingId,
                showFeedingQuickAdd = true,
                editReturnScreen = from
            )
        }
        viewModelScope.launch {
            val phrases = feedingQuickPhraseRepository.load()
            _uiState.update { state ->
                if (state.showFeedingQuickAdd && state.editingFeedingId == feedingId) {
                    state.copy(feedingQuickPhrases = phrases)
                } else {
                    state
                }
            }
        }
    }

    fun getEditingMeal(): MealItem? = mealRepository.getMealById(_uiState.value.editingMealId)

    fun getEditingFeeding(): FeedingItem? = feedingRepository.getById(_uiState.value.editingFeedingId)

    fun saveFeeding(
        amountMl: Int?,
        note: String,
        timestamp: Long,
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (amountMl == null && note.isBlank()) {
            _uiState.update { it.copy(toastMessage = "请填写奶量或描述") }
            onComplete(false)
            return
        }
        viewModelScope.launch {
            val dateKey = DateUtils.toDateKey(timestamp)
            val item = FeedingItem(
                id = System.currentTimeMillis().toString(),
                timestamp = timestamp,
                dateKey = dateKey,
                amountMl = amountMl,
                note = note
            )
            val saved = runCatching { feedingRepository.add(item) }.isSuccess
            if (!saved) {
                _uiState.update { it.copy(toastMessage = "保存失败，数据未写入本地，请重试") }
                onComplete(false)
                return@launch
            }
            hapticSuccessIfEnabled()
            refreshFeedingList()
            refreshSearchResults()
            _uiState.update {
                it.copy(homeMode = HomeMode.FEEDING)
            }
            persistHomeMode(HomeMode.FEEDING)
            // 本地已落盘：先放行页面关闭，飞书同步独立进行，不阻塞用户
            onComplete(true)
            syncFeishuAfterSave(HomeMode.FEEDING, "已保存", dateKey, localId = item.id)
        }
    }

    fun saveFeedingEdit(
        amountMl: Int?,
        note: String,
        timestamp: Long,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val feedingId = _uiState.value.editingFeedingId
        val existing = feedingRepository.getById(feedingId)
        if (existing == null) {
            _uiState.update { it.copy(toastMessage = "记录不存在，无法保存") }
            onComplete(false)
            return
        }
        if (amountMl == null && note.isBlank()) {
            _uiState.update { it.copy(toastMessage = "请填写奶量或描述") }
            onComplete(false)
            return
        }
        viewModelScope.launch {
            val newDateKey = DateUtils.toDateKey(timestamp)
            val saved = runCatching {
                feedingRepository.update(
                    existing.copy(
                        amountMl = amountMl,
                        note = note,
                        timestamp = timestamp,
                        dateKey = newDateKey
                    )
                )
            }.isSuccess
            if (!saved) {
                _uiState.update { it.copy(toastMessage = "保存失败，数据未写入本地，请重试") }
                onComplete(false)
                return@launch
            }
            hapticSuccessIfEnabled()
            refreshFeedingList()
            refreshSearchResults()
            _uiState.update {
                it.copy(
                    showFeedingQuickAdd = false,
                    editingFeedingId = ""
                )
            }
            onComplete(true)
            syncFeishuAfterSave(
                HomeMode.FEEDING,
                "已保存",
                existing.dateKey,
                newDateKey,
                localId = feedingId
            )
        }
    }

    fun deleteFeeding(id: String) {
        viewModelScope.launch {
            feedingRepository.moveToTrash(id)
            refreshFeedingList()
            refreshTrash()
            refreshSearchResults()
            if (_uiState.value.editingFeedingId == id) {
                cancelFeedingNoteVoice()
                _uiState.update {
                    it.copy(
                        showFeedingQuickAdd = false,
                        editingFeedingId = "",
                        toastMessage = "已移入回收站"
                    )
                }
            } else {
                _uiState.update { it.copy(toastMessage = "已移入回收站") }
            }
        }
    }

    fun startMealAiAnalyze() {
        val meal = getEditingMeal() ?: return
        if (meal.dishes.none { it.isNotBlank() }) {
            _uiState.update { it.copy(toastMessage = "先添加菜品") }
            return
        }
        if (!IflytekSparkConfig.isConfigured()) {
            _uiState.update {
                it.copy(
                    mealAiPhase = MealAiPhase.Error,
                    mealAiPreview = "",
                    mealAiError = IflytekConfig.NOT_CONFIGURED_HINT
                )
            }
            return
        }
        mealAiJob?.cancel()
        _uiState.update {
            it.copy(
                mealAiPhase = MealAiPhase.Streaming,
                mealAiPreview = "",
                mealAiError = ""
            )
        }
        val system = SparkPromptBuilder.systemPrompt()
        val user = SparkPromptBuilder.userPrompt(
            dateTime = "${meal.dateKey} ${DateUtils.formatTime(meal.timestamp)}",
            dishes = meal.dishes,
            note = meal.note,
            previousAiComment = meal.aiComment
        )
        mealAiJob = viewModelScope.launch {
            try {
                val full = withContext(Dispatchers.IO) {
                    sparkProvider.complete(system, user) { accumulated ->
                        _uiState.update { state ->
                            if (state.mealAiPhase == MealAiPhase.Streaming) {
                                state.copy(mealAiPreview = accumulated)
                            } else {
                                state
                            }
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        mealAiPhase = MealAiPhase.Ready,
                        mealAiPreview = full.trim(),
                        mealAiError = ""
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mealAiPhase = MealAiPhase.Error,
                        mealAiPreview = "",
                        mealAiError = e.message ?: "生成失败"
                    )
                }
            }
        }
    }

    fun discardMealAiPreview() {
        cancelMealAiAnalyze()
    }

    fun adoptMealAiComment() {
        val state = _uiState.value
        if (state.mealAiPhase != MealAiPhase.Ready) return
        val preview = state.mealAiPreview.trim()
        if (preview.isEmpty()) return
        val mealId = state.editingMealId
        val existing = mealRepository.getMealById(mealId) ?: run {
            _uiState.update { it.copy(toastMessage = "饭记不存在") }
            cancelMealAiAnalyze()
            return
        }
        viewModelScope.launch {
            mealRepository.update(existing.copy(aiComment = preview))
            refreshMealList()
            refreshSearchResults()
            cancelMealAiAnalyze()
            syncFeishuAfterSave(HomeMode.MEAL, "已写入 AI 短评", existing.dateKey, localId = mealId)
        }
    }

    fun cancelMealAiAnalyze() {
        mealAiJob?.cancel()
        mealAiJob = null
        _uiState.update {
            it.copy(
                mealAiPhase = MealAiPhase.Idle,
                mealAiPreview = "",
                mealAiError = ""
            )
        }
    }

    fun saveMemoEdit(content: String) {
        val memoId = _uiState.value.editingMemoId
        if (memoId.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            val memo = repository.getMemoById(memoId)
            repository.updateContent(memoId, content)
            hapticSuccessIfEnabled()
            refreshList()
            refreshSearchResults()
            _uiState.update {
                it.copy(
                    screen = it.editReturnScreen,
                    editingMemoId = ""
                )
            }
            memo?.dateKey?.let { syncFeishuAfterSave(HomeMode.NOTE, "已保存", it, localId = memoId) }
                ?: syncFeishuAfterSave(HomeMode.NOTE, "已保存", localId = memoId)
        }
    }

    fun saveMealEdit(dishes: List<String>, note: String, timestamp: Long, aiComment: String) {
        val mealId = _uiState.value.editingMealId
        val existing = mealRepository.getMealById(mealId) ?: return
        val cleaned = dishes.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleaned.isEmpty()) {
            _uiState.update { it.copy(toastMessage = "请至少填写一道菜") }
            return
        }
        viewModelScope.launch {
            val newDateKey = DateUtils.toDateKey(timestamp)
            mealRepository.update(
                existing.copy(
                    dishes = cleaned,
                    note = note.trim(),
                    aiComment = aiComment.trim(),
                    timestamp = timestamp,
                    dateKey = newDateKey
                )
            )
            refreshMealList()
            refreshSearchResults()
            cancelMealAiAnalyze()
            _uiState.update {
                it.copy(
                    screen = it.editReturnScreen,
                    editingMealId = ""
                )
            }
            syncFeishuAfterSave(HomeMode.MEAL, "已保存", existing.dateKey, newDateKey, localId = mealId)
        }
    }

    fun startMealFromUri(uri: Uri, source: MealSource) {
        _uiState.update { it.copy(showMealSourceSheet = false) }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    MealImageStore.storeFromUri(
                        context = getApplication(),
                        uri = uri,
                        imageDir = mealRepository.imageDirectory(),
                        thumbDir = mealRepository.thumbDirectory()
                    )
                }
            }.onSuccess { stored ->
                beginRecognize(stored, source)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(toastMessage = "读取图片失败：${error.message ?: "未知错误"}")
                }
            }
        }
    }

    fun startMealFromCameraFile(cameraFile: File) {
        _uiState.update { it.copy(showMealSourceSheet = false) }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    MealImageStore.storeFromCameraFile(
                        cameraFile = cameraFile,
                        imageDir = mealRepository.imageDirectory(),
                        thumbDir = mealRepository.thumbDirectory()
                    )
                }
            }.onSuccess { stored ->
                beginRecognize(stored, MealSource.CAMERA)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(toastMessage = "保存拍照失败：${error.message ?: "未知错误"}")
                }
            }
        }
    }

    private fun beginRecognize(stored: MealImageStore.StoredImages, source: MealSource) {
        val confirm = MealConfirmState(
            id = stored.id,
            imagePath = stored.imageFile.absolutePath,
            thumbnailPath = stored.thumbFile.absolutePath,
            timestamp = stored.timestamp,
            source = source,
            recognizing = true
        )
        _uiState.update {
            it.copy(screen = AppScreen.MealConfirm, mealConfirm = confirm)
        }
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    visionProvider.recognizeDishes(stored.imageFile)
                }
            }
            _uiState.update { state ->
                val current = state.mealConfirm
                if (current == null || current.id != stored.id) state
                else result.fold(
                    onSuccess = { dishes ->
                        state.copy(
                            mealConfirm = current.copy(
                                dishes = dishes,
                                recognizing = false,
                                recognizeFailed = false,
                                errorMessage = null
                            )
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            mealConfirm = current.copy(
                                dishes = emptyList(),
                                recognizing = false,
                                recognizeFailed = true,
                                errorMessage = error.message ?: "识别失败"
                            ),
                            toastMessage = "识别失败，可手动填写菜品"
                        )
                    }
                )
            }
        }
    }

    fun updateMealConfirmDishes(dishes: List<String>) {
        _uiState.update { state ->
            val confirm = state.mealConfirm ?: return@update state
            state.copy(mealConfirm = confirm.copy(dishes = dishes))
        }
    }

    fun updateMealConfirmNote(note: String) {
        _uiState.update { state ->
            val confirm = state.mealConfirm ?: return@update state
            state.copy(mealConfirm = confirm.copy(note = note))
        }
    }

    fun confirmSaveMeal() {
        val confirm = _uiState.value.mealConfirm ?: return
        val dishes = confirm.dishes.map { it.trim() }.filter { it.isNotEmpty() }
        if (dishes.isEmpty()) {
            _uiState.update { it.copy(toastMessage = "请至少填写一道菜") }
            return
        }
        viewModelScope.launch {
            mealRepository.add(
                MealItem(
                    id = confirm.id,
                    timestamp = confirm.timestamp,
                    dateKey = DateUtils.toDateKey(confirm.timestamp),
                    dishes = dishes,
                    note = confirm.note.trim(),
                    imagePath = confirm.imagePath,
                    thumbnailPath = confirm.thumbnailPath,
                    source = confirm.source
                )
            )
            hapticSuccessIfEnabled()
            refreshMealList()
            _uiState.update {
                it.copy(
                    screen = AppScreen.Home,
                    homeMode = HomeMode.MEAL,
                    mealConfirm = null
                )
            }
            persistHomeMode(HomeMode.MEAL)
            syncFeishuAfterSave(
                HomeMode.MEAL,
                "饭记已保存",
                DateUtils.toDateKey(confirm.timestamp),
                localId = confirm.id
            )
        }
    }

    fun cancelMealConfirm() {
        val confirm = _uiState.value.mealConfirm
        if (confirm != null) {
            MealImageStore.deletePair(confirm.imagePath, confirm.thumbnailPath)
        }
        _uiState.update {
            it.copy(screen = AppScreen.Home, mealConfirm = null)
        }
    }

    fun exportBackup(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri)?.use { backupService.exportTo(it) }
                        ?: error("无法写入文件")
                }
            }.onSuccess {
                _uiState.update { it.copy(toastMessage = "备份已导出") }
            }.onFailure { error ->
                _uiState.update { it.copy(toastMessage = "导出失败：${error.message ?: "未知错误"}") }
            }
        }
    }

    fun importBackup(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use {
                        backupService.importFrom(it, replaceExisting = true)
                    } ?: error("无法读取文件")
                }
            }.onSuccess {
                refreshList()
                refreshTrash()
                refreshLogs()
                refreshStorageInfo()
                refreshSearchResults()
                _uiState.update { it.copy(toastMessage = "备份已恢复") }
            }.onFailure { error ->
                _uiState.update { it.copy(toastMessage = "恢复失败：${error.message ?: "未知错误"}") }
            }
        }
    }

    fun emptyTrashPermanently() {
        viewModelScope.launch {
            val mode = _uiState.value.homeMode
            val trashIds = when (mode) {
                HomeMode.MEAL -> mealRepository.getTrash().map { it.item.id }
                HomeMode.FEEDING -> feedingRepository.getTrash().map { it.item.id }
                HomeMode.CHRONICLE -> chronicleRepository.getTrash().map { it.item.id }
                HomeMode.NOTE -> repository.getTrash().map { it.item.id }
            }
            val dateKeys = trashDateKeys(mode, trashIds)
            val feishuError = runCatching { deleteFromFeishu(mode, trashIds) }.exceptionOrNull()?.message
            when (mode) {
                HomeMode.MEAL -> mealRepository.emptyTrashPermanently()
                HomeMode.FEEDING -> feedingRepository.emptyTrashPermanently()
                HomeMode.CHRONICLE -> chronicleRepository.emptyTrashPermanently()
                HomeMode.NOTE -> repository.emptyTrashPermanently()
            }
            refreshTrash()
            refreshStorageInfo()
            val toast = if (feishuError != null) "回收站已清空（云端删除失败：$feishuError）" else "回收站已清空"
            syncFeishuAfterSave(mode, toast, *dateKeys.toTypedArray())
        }
    }

    private fun trashDateKeys(mode: HomeMode, ids: List<String>): Set<String> {
        if (ids.isEmpty()) return emptySet()
        return when (mode) {
            HomeMode.NOTE -> repository.getTrash().filter { it.item.id in ids }.map { it.item.dateKey }
            HomeMode.MEAL -> mealRepository.getTrash().filter { it.item.id in ids }.map { it.item.dateKey }
            HomeMode.FEEDING -> feedingRepository.getTrash().filter { it.item.id in ids }.map { it.item.dateKey }
            HomeMode.CHRONICLE -> chronicleRepository.getTrash().filter { it.item.id in ids }.map { it.item.dateKey }
        }.toSet()
    }

    fun clearAsrLogs() {
        asrLogRepository.clearAll()
        refreshLogs()
        refreshStorageInfo()
        _uiState.update { it.copy(toastMessage = "日志已清空") }
    }

    fun pinRecordShortcut() {
        ShortcutHelper.pinRecordShortcut(getApplication())
    }

    private fun refreshStorageInfo() {
        _uiState.update {
            it.copy(storageInfo = repository.getStorageInfo(asrLogRepository.getAll().size))
        }
    }

    private fun refreshSearchResults() {
        _uiState.update { state ->
            if (state.screen != AppScreen.Search && state.editReturnScreen != AppScreen.Search) {
                return@update state
            }
            when (state.homeMode) {
                HomeMode.MEAL -> state.copy(
                    mealSearchResults = mealRepository.search(state.searchDateKey, state.searchKeyword)
                )
                HomeMode.FEEDING -> state.copy(
                    feedingSearchResults = feedingRepository.search(state.searchDateKey, state.searchKeyword)
                )
                HomeMode.CHRONICLE -> state.copy(
                    chronicleSearchResults = chronicleRepository.search(state.searchDateKey, state.searchKeyword)
                )
                HomeMode.NOTE -> state.copy(
                    searchResults = repository.search(state.searchDateKey, state.searchKeyword)
                )
            }
        }
    }

    fun selectExportDate(dateKey: String) {
        _uiState.update { it.copy(exportDateKey = dateKey) }
        refreshExportData(dateKey)
    }

    fun copyExportToClipboard() {
        val state = _uiState.value
        val text = when (state.homeMode) {
            HomeMode.NOTE -> MemoExportFormatter.format(state.exportDateKey, state.exportMemos)
            HomeMode.MEAL -> MealExportFormatter.format(state.exportDateKey, state.exportMeals)
            HomeMode.FEEDING -> FeedingExportFormatter.format(state.exportDateKey, state.exportFeedings)
            HomeMode.CHRONICLE -> ChronicleExportFormatter.format(state.exportDateKey, state.exportChronicles)
        }
        if (text == state.exportDateKey) return
        val clipboard = getApplication<Application>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("snap-note export", text))
        _uiState.update { it.copy(toastMessage = "已复制到剪贴板") }
    }

    private fun defaultExportDateKey(mode: HomeMode): String {
        val keys = when (mode) {
            HomeMode.NOTE -> repository.getAvailableDateKeys()
            HomeMode.MEAL -> mealRepository.getAvailableDateKeys()
            HomeMode.FEEDING -> feedingRepository.getAvailableDateKeys()
            HomeMode.CHRONICLE -> chronicleRepository.getAvailableDateKeys()
        }
        return keys.firstOrNull() ?: DateUtils.toDateKey(System.currentTimeMillis())
    }

    private fun refreshExportData(dateKey: String) {
        _uiState.update { state ->
            when (state.homeMode) {
                HomeMode.NOTE -> state.copy(
                    exportMemos = repository.getMemosByDate(dateKey),
                    exportMeals = emptyList(),
                    exportFeedings = emptyList(),
                    exportChronicles = emptyList()
                )
                HomeMode.MEAL -> state.copy(
                    exportMemos = emptyList(),
                    exportMeals = mealRepository.getMealsByDate(dateKey),
                    exportFeedings = emptyList(),
                    exportChronicles = emptyList()
                )
                HomeMode.FEEDING -> state.copy(
                    exportMemos = emptyList(),
                    exportMeals = emptyList(),
                    exportFeedings = feedingRepository.getFeedingsByDate(dateKey),
                    exportChronicles = emptyList()
                )
                HomeMode.CHRONICLE -> state.copy(
                    exportMemos = emptyList(),
                    exportMeals = emptyList(),
                    exportFeedings = emptyList(),
                    exportChronicles = chronicleRepository.getByDateKey(dateKey)
                )
            }
        }
    }

    fun refreshList() {
        _uiState.update { it.copy(groups = repository.getGrouped()) }
        refreshPlaybackState()
    }

    fun refreshMealList() {
        _uiState.update { it.copy(mealGroups = mealRepository.getGrouped()) }
    }

    fun refreshFeedingList() {
        val all = feedingRepository.getAll()
        val groups = feedingRepository.getGrouped()
        val lastAmount = all.firstOrNull { it.amountMl != null }?.amountMl
        val milkSeries = DailyMilkSeriesBuilder.build(all, 14)
        _uiState.update {
            it.copy(
                feedingGroups = groups,
                lastFeedingAmountMl = lastAmount,
                feedingDailyMilkPoints = milkSeries
            )
        }
    }

    fun refreshChronicleList() {
        val all = chronicleRepository.getAll()
        _uiState.update {
            it.copy(
                chronicleGroups = chronicleRepository.getGrouped(),
                chronicleWeightPoints = ChronicleSeriesBuilder.weightPoints(all),
                chronicleHeightPoints = ChronicleSeriesBuilder.heightPoints(all),
                chronicleMilestones = ChronicleSeriesBuilder.milestones(all)
            )
        }
    }

    private fun refreshPlaybackState() {
        _uiState.update {
            val current = playbackService.getCurrentId()
            it.copy(
                playingId = if (playbackService.isPlaying(current)) current else "",
                pausedId = if (playbackService.isPausedOn(current)) current else ""
            )
        }
    }

    private fun refreshTrash() {
        _uiState.update {
            it.copy(
                trashItems = repository.getTrash(),
                mealTrashItems = mealRepository.getTrash(),
                feedingTrashItems = feedingRepository.getTrash(),
                chronicleTrashItems = chronicleRepository.getTrash()
            )
        }
    }

    private fun refreshLogs() {
        _uiState.update { it.copy(asrLogs = asrLogRepository.getAll()) }
    }

    fun onRecordStart(): Boolean {
        if (_uiState.value.feedingNoteRecording) {
            _uiState.update { it.copy(toastMessage = "请先结束育儿描述录音") }
            return false
        }
        if (recordingService.isRecording()) return false
        pendingMemoId = System.currentTimeMillis().toString()
        val m4aFile = repository.buildAudioPath(pendingMemoId)
        val wavFile = repository.buildWavPath(pendingMemoId)
        pendingAudioFile = m4aFile
        _uiState.update { it.copy(isRecording = true, recordingDuration = "0:00") }
        if (!recordingService.start(wavFile, m4aFile)) {
            pendingAudioFile = null
            _uiState.update {
                it.copy(isRecording = false, toastMessage = "无法启动录音，请检查麦克风权限")
            }
            return false
        }
        return true
    }

    fun onRecordEnd(cancelled: Boolean) {
        if (!recordingService.isRecording()) {
            _uiState.update { it.copy(isRecording = false) }
            return
        }
        _uiState.update { it.copy(isRecording = false) }
        if (cancelled) {
            recordingService.cancel()
            pendingAudioFile = null
            return
        }
        val memoId = pendingMemoId.ifBlank { System.currentTimeMillis().toString() }
        val result = recordingService.stop()
        pendingAudioFile = null
        if (result == null) {
            _uiState.update {
                it.copy(
                    toastMessage = "录音太短，请按住至少 ${MemoConstants.MIN_RECORDING_DURATION_SECONDS} 秒"
                )
            }
            return
        }
        viewModelScope.launch {
            val error = processService.processNewRecording(
                memoId,
                result.path,
                result.durationSeconds,
                result.asrWavPath
            )
            if (error != null) {
                _uiState.update { it.copy(toastMessage = error) }
            }
            syncFeishuIfEnabled()
        }
    }

    fun togglePlay(id: String, audioPath: String) {
        playbackService.toggle(id, audioPath)
        refreshPlaybackState()
    }

    fun delete(id: String) {
        if (_uiState.value.playingId == id || _uiState.value.pausedId == id) {
            playbackService.stop()
        }
        viewModelScope.launch {
            repository.moveToTrash(id)
            refreshList()
            refreshTrash()
            refreshSearchResults()
            _uiState.update { it.copy(toastMessage = "已移入回收站") }
        }
    }

    fun deleteMeal(id: String) {
        viewModelScope.launch {
            mealRepository.moveToTrash(id)
            refreshMealList()
            refreshTrash()
            refreshSearchResults()
            if (_uiState.value.editingMealId == id) {
                cancelMealAiAnalyze()
                _uiState.update {
                    it.copy(
                        screen = it.editReturnScreen,
                        editingMealId = "",
                        toastMessage = "已移入回收站"
                    )
                }
            } else {
                _uiState.update { it.copy(toastMessage = "已移入回收站") }
            }
        }
    }

    fun getEditingMemo(): MemoItem? = repository.getMemoById(_uiState.value.editingMemoId)

    fun restoreFromTrash(id: String) {
        viewModelScope.launch {
            when (_uiState.value.homeMode) {
                HomeMode.MEAL -> {
                    mealRepository.restoreFromTrash(id)
                    refreshMealList()
                }
                HomeMode.FEEDING -> {
                    feedingRepository.restoreFromTrash(id)
                    refreshFeedingList()
                }
                HomeMode.CHRONICLE -> {
                    chronicleRepository.restoreFromTrash(id)
                    refreshChronicleList()
                }
                HomeMode.NOTE -> {
                    repository.restoreFromTrash(id)
                    refreshList()
                }
            }
            refreshTrash()
            _uiState.update { it.copy(toastMessage = "已恢复") }
        }
    }

    fun deletePermanently(id: String) {
        viewModelScope.launch {
            val mode = _uiState.value.homeMode
            val dateKeys = trashDateKeys(mode, listOf(id))
            val feishuError = runCatching { deleteFromFeishu(mode, listOf(id)) }.exceptionOrNull()?.message
            when (mode) {
                HomeMode.MEAL -> mealRepository.deletePermanently(id)
                HomeMode.FEEDING -> feedingRepository.deletePermanently(id)
                HomeMode.CHRONICLE -> chronicleRepository.deletePermanently(id)
                HomeMode.NOTE -> repository.deletePermanently(id)
            }
            refreshTrash()
            val toast = if (feishuError != null) "已彻底删除（云端删除失败：$feishuError）" else "已彻底删除"
            syncFeishuAfterSave(mode, toast, *dateKeys.toTypedArray())
        }
    }

    fun currentTrashCount(): Int {
        val state = _uiState.value
        return when (state.homeMode) {
            HomeMode.MEAL -> state.mealTrashItems.size
            HomeMode.FEEDING -> state.feedingTrashItems.size
            HomeMode.CHRONICLE -> state.chronicleTrashItems.size
            HomeMode.NOTE -> state.trashItems.size
        }
    }

    override fun onCleared() {
        cancelMealAiAnalyze()
        playbackService.stop()
        recordingService.cancel()
        super.onCleared()
    }
}
