package com.snapnote.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.snapnote.app.ui.AppScreenRoot
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.snapnote.app.model.AppScreen
import com.snapnote.app.model.AppThemeMode
import com.snapnote.app.model.HomeMode
import com.snapnote.app.model.MealSource
import com.snapnote.app.model.MemoConstants
import com.snapnote.app.ui.applyAppNightMode
import com.snapnote.app.ui.LogListScreen
import com.snapnote.app.ui.ChronicleRecordSheet
import com.snapnote.app.ui.ChronicleStatsScreen
import com.snapnote.app.ui.FeedingStatsScreen
import com.snapnote.app.ui.FeedingRecordSheet
import com.snapnote.app.ui.MealConfirmScreen
import com.snapnote.app.ui.MealDetailScreen
import com.snapnote.app.ui.MemoEditScreen
import com.snapnote.app.ui.MineHubMenuItem
import com.snapnote.app.ui.MineMenuHubScreen
import com.snapnote.app.ui.MineScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Storage
import com.snapnote.app.ui.RecordExportScreen
import com.snapnote.app.ui.RecycleBinScreen
import com.snapnote.app.ui.SearchScreen
import com.snapnote.app.ui.SnapNoteScreen
import com.snapnote.app.ui.AppTheme
import com.snapnote.app.ui.SystemSettingsScreen
import com.snapnote.app.ui.appScreenTransition
import com.snapnote.app.ui.FeedingQuickPhraseScreen
import com.snapnote.app.ui.FeishuSyncSettingsScreen
import com.snapnote.app.ui.GithubReleaseUpdateScreen
import com.snapnote.app.ui.IflytekSettingsScreen
import com.snapnote.app.ui.StorageManageScreen
import com.snapnote.app.util.DateUtils
import com.snapnote.app.util.LocaleManager
import com.snapnote.app.util.MealImageStore
import java.io.File

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_QUICK_RECORD = "extra_quick_record"
    }

    private val viewModel: MainViewModel by viewModels()
    private var micGranted = false
    private var cameraGranted = false
    private var pendingCameraFile: File? = null

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
        if (!granted) {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraGranted = granted
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(this, getString(R.string.toast_camera_permission), Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        pendingCameraFile = null
        if (success && file != null && file.exists() && file.length() > 0) {
            viewModel.startMealFromCameraFile(file)
        } else {
            file?.delete()
            Toast.makeText(this, getString(R.string.toast_no_photo), Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.startMealFromUri(uri, MealSource.GALLERY)
        }
    }

    private val exportBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let { viewModel.exportBackup(contentResolver, it) }
    }

    private val importBackupLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(contentResolver, it) }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleManager.wrapContext(it) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val storedTheme = getSharedPreferences(MemoConstants.PREF_NAME, MODE_PRIVATE)
            .getString(MemoConstants.PREF_KEY_APP_THEME_MODE, null)
        applyAppNightMode(AppThemeMode.fromStored(storedTheme))
        super.onCreate(savedInstanceState)
        micGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        cameraGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        handleQuickRecordIntent(intent)

        setContent {
            val state by viewModel.uiState.collectAsState()
            AppTheme(
                fontSizeScale = state.appFontSize.scale,
                fontPreset = state.appFontPreset,
                themeMode = state.appThemeMode,
                reduceMotion = state.reduceMotion,
                showSecondsInListTime = state.showSecondsInListTime,
                hapticFeedbackEnabled = state.hapticFeedbackEnabled,
                feedingStatsCardStyle = state.feedingStatsCardStyle
            ) {

            LaunchedEffect(Unit) {
                viewModel.recreateEvent.collect { recreate() }
            }

            LaunchedEffect(state.toastMessage) {
                state.toastMessage?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            LaunchedEffect(state.githubUpdatePendingInstallPath) {
                val path = state.githubUpdatePendingInstallPath ?: return@LaunchedEffect
                installDownloadedApk(path)
                viewModel.consumeGithubPendingInstall()
            }

            if (state.showFeedingQuickAdd) {
                BackHandler { viewModel.dismissFeedingQuickAdd() }
            } else if (state.showChronicleSheet) {
                BackHandler { viewModel.dismissChronicleSheet() }
            } else if (state.screen != AppScreen.Home) {
                BackHandler { viewModel.navigateBack() }
            }

            Box(Modifier.fillMaxSize()) {
            AppScreenRoot {
            AnimatedContent(
                modifier = Modifier.fillMaxSize(),
                targetState = state.screen,
                transitionSpec = { appScreenTransition() },
                label = "appScreen"
            ) { screen ->
            AppScreenRoot {
            when (screen) {
                AppScreen.Home -> SnapNoteScreen(
                    state = state,
                    onHomeVisible = { viewModel.onHomeVisible() },
                    onSearch = { viewModel.openHomeLeftAction() },
                    onMine = { viewModel.openMine() },
                    onFeedingPullRefresh = { viewModel.syncFeishuNow() },
                    onRecordStart = {
                        if (!ensureMicPermission()) return@SnapNoteScreen false
                        viewModel.onRecordStart()
                    },
                    onRecordEnd = { cancelled -> viewModel.onRecordEnd(cancelled) },
                    onMealFabClick = { viewModel.openMealSourceSheet() },
                    onFeedingFabClick = { viewModel.openFeedingQuickAdd() },
                    onChronicleFabClick = { viewModel.openChronicleSheet() },
                    onChronicleFilterSelected = { viewModel.setChronicleFilter(it) },
                    onHomeModeSelected = { viewModel.setHomeMode(it) },
                    onPlayToggle = { id, path -> viewModel.togglePlay(id, path) },
                    onDelete = { id, _ -> viewModel.delete(id) },
                    onEdit = { viewModel.openMemoEdit(it.id, AppScreen.Home) },
                    onMealClick = { viewModel.openMealDetail(it.id, AppScreen.Home) },
                    onFeedingClick = { viewModel.openFeedingDetail(it.id, AppScreen.Home) },
                    onDeleteFeeding = { viewModel.deleteFeeding(it) },
                    onChronicleClick = { viewModel.openChronicleDetail(it.id, AppScreen.Home) },
                    onDeleteChronicle = { viewModel.deleteChronicle(it) },
                    onDismissMealSheet = { viewModel.dismissMealSourceSheet() },
                    onTakePhoto = { ensureCameraAndCapture() },
                    onPickGallery = {
                        viewModel.dismissMealSourceSheet()
                        pickImageLauncher.launch("image/*")
                    }
                )

                AppScreen.ChronicleStats -> ChronicleStatsScreen(
                    weightPoints = state.chronicleWeightPoints,
                    heightPoints = state.chronicleHeightPoints,
                    milestones = state.chronicleMilestones,
                    onBack = { viewModel.navigateBack() }
                )

                AppScreen.FeedingStats -> FeedingStatsScreen(
                    dailyMilkPoints = state.feedingDailyMilkPoints,
                    onBack = { viewModel.navigateBack() }
                )

                AppScreen.Search -> SearchScreen(
                    homeMode = state.homeMode,
                    selectedDateKey = state.searchDateKey,
                    keyword = state.searchKeyword,
                    results = state.searchResults,
                    mealResults = state.mealSearchResults,
                    feedingResults = state.feedingSearchResults,
                    chronicleResults = state.chronicleSearchResults,
                    playingId = state.playingId,
                    pausedId = state.pausedId,
                    onBack = { viewModel.navigateBack() },
                    onDateSelected = { viewModel.updateSearchDate(it) },
                    onKeywordChange = { viewModel.updateSearchKeyword(it) },
                    onPlayToggle = { id, path -> viewModel.togglePlay(id, path) },
                    onDelete = { id, _ -> viewModel.delete(id) },
                    onEdit = { viewModel.openMemoEdit(it.id, AppScreen.Search) },
                    onMealClick = { viewModel.openMealDetail(it.id, AppScreen.Search) },
                    onFeedingClick = { viewModel.openFeedingDetail(it.id, AppScreen.Search) },
                    onChronicleClick = { viewModel.openChronicleDetail(it.id, AppScreen.Search) }
                )

                AppScreen.Mine -> MineScreen(
                    homeMode = state.homeMode,
                    onBack = { viewModel.navigateBack() },
                    onHomeModeSelected = { viewModel.setHomeMode(it) },
                    onOpenFeishuHub = { viewModel.openMineFeishuHub() },
                    onOpenDataHub = { viewModel.openMineDataHub() },
                    onOpenIflytekHub = { viewModel.openMineIflytekHub() },
                    onOpenGithubReleaseUpdate = { viewModel.openGithubReleaseUpdate() },
                    onOpenFeedingConfig = { viewModel.openFeedingQuickPhrases() },
                    onOpenSearch = { viewModel.openSearchFromMine() },
                    onOpenSystemSettings = { viewModel.openSystemSettings() },
                    feishuSyncEnabled = state.feishuConfig.enabled,
                    iflytekConfigured = state.iflytekCredentials.isConfigured(),
                    feedingPhraseCount = state.feedingQuickPhrases.size,
                    appFontPreset = state.appFontPreset,
                    appFontSize = state.appFontSize
                )

                AppScreen.SystemSettings -> SystemSettingsScreen(
                    fontPreset = state.appFontPreset,
                    fontSize = state.appFontSize,
                    themeMode = state.appThemeMode,
                    appLanguage = state.appLanguage,
                    reduceMotion = state.reduceMotion,
                    showSecondsInListTime = state.showSecondsInListTime,
                    onBack = { viewModel.navigateBack() },
                    onFontPresetSelected = { viewModel.setAppFontPreset(it) },
                    onFontSizeSelected = { viewModel.setAppFontSize(it) },
                    onThemeModeSelected = { viewModel.setAppThemeMode(it) },
                    onLanguageSelected = { viewModel.setAppLanguage(it) },
                    onReduceMotionChange = { viewModel.setReduceMotion(it) },
                    onShowSecondsInListTimeChange = { viewModel.setShowSecondsInListTime(it) },
                    hapticFeedbackEnabled = state.hapticFeedbackEnabled,
                    feedingStatsCardStyle = state.feedingStatsCardStyle,
                    onHapticFeedbackChange = { viewModel.setHapticFeedbackEnabled(it) },
                    onFeedingStatsCardStyleSelected = { viewModel.setFeedingStatsCardStyle(it) }
                )

                AppScreen.MineFeishuHub -> {
                    val feishuSubtitle = if (state.feishuConfig.enabled) {
                        stringResource(R.string.hub_feishu_subtitle_enabled)
                    } else {
                        stringResource(R.string.hub_feishu_subtitle_disabled)
                    }
                    MineMenuHubScreen(
                        title = stringResource(R.string.hub_feishu_title),
                        onBack = { viewModel.navigateBack() },
                        items = listOf(
                            MineHubMenuItem(
                                icon = Icons.Outlined.CloudSync,
                                title = stringResource(R.string.hub_feishu_sync),
                                subtitle = feishuSubtitle,
                                onClick = { viewModel.openFeishuSyncSettings() }
                            )
                        )
                    )
                }

                AppScreen.MineDataHub -> {
                    val trashCount = when (state.homeMode) {
                        HomeMode.NOTE -> state.trashItems.size
                        HomeMode.MEAL -> state.mealTrashItems.size
                        HomeMode.FEEDING -> state.feedingTrashItems.size
                        HomeMode.CHRONICLE -> state.chronicleTrashItems.size
                    }
                    val recycleSubtitle = if (trashCount > 0) {
                        stringResource(R.string.hub_trash_deleted, trashCount)
                    } else {
                        stringResource(R.string.hub_trash_empty)
                    }
                    MineMenuHubScreen(
                        title = stringResource(R.string.hub_data_title),
                        onBack = { viewModel.navigateBack() },
                        items = listOf(
                            MineHubMenuItem(
                                icon = Icons.Outlined.Storage,
                                title = stringResource(R.string.hub_data_storage),
                                subtitle = stringResource(R.string.hub_data_storage_subtitle),
                                onClick = { viewModel.openStorageManage() }
                            ),
                            MineHubMenuItem(
                                icon = Icons.Outlined.FileDownload,
                                title = stringResource(R.string.hub_data_export),
                                subtitle = stringResource(R.string.hub_data_export_subtitle),
                                onClick = { viewModel.openRecordExport() }
                            ),
                            MineHubMenuItem(
                                icon = Icons.Default.DeleteOutline,
                                title = stringResource(R.string.hub_trash),
                                subtitle = recycleSubtitle,
                                onClick = { viewModel.openRecycleBin() }
                            )
                        )
                    )
                }

                AppScreen.MineIflytekHub -> {
                    val iflytekSubtitle = if (state.iflytekCredentials.isConfigured()) {
                        stringResource(R.string.hub_iflytek_configured)
                    } else {
                        stringResource(R.string.hub_iflytek_unconfigured)
                    }
                    MineMenuHubScreen(
                        title = stringResource(R.string.hub_iflytek_title),
                        onBack = { viewModel.navigateBack() },
                        items = listOf(
                            MineHubMenuItem(
                                icon = Icons.Default.Mic,
                                title = stringResource(R.string.hub_iflytek_title),
                                subtitle = iflytekSubtitle,
                                onClick = { viewModel.openIflytekSettings() }
                            ),
                            MineHubMenuItem(
                                icon = Icons.AutoMirrored.Filled.ListAlt,
                                title = stringResource(R.string.hub_iflytek_logs),
                                subtitle = stringResource(R.string.hub_iflytek_logs_subtitle),
                                onClick = { viewModel.openLogList() }
                            )
                        )
                    )
                }

                AppScreen.FeishuSyncSettings -> FeishuSyncSettingsScreen(
                    config = state.feishuConfig,
                    syncing = state.feishuSyncing,
                    onBack = { viewModel.navigateBack() },
                    onSave = { viewModel.saveFeishuConfig(it) },
                    onSyncLast7Days = { viewModel.syncFeishuNow() },
                    onSyncLast30Days = { viewModel.syncFeishuLast30Days() },
                    onSyncFull = { viewModel.syncFeishuFull() }
                )

                AppScreen.GithubReleaseUpdate -> GithubReleaseUpdateScreen(
                    latestName = state.githubUpdateLatestName,
                    latestDateLabel = state.githubUpdateLatestDateLabel,
                    loadingLatest = state.githubUpdateLoadingLatest,
                    downloading = state.githubUpdateDownloading,
                    confirmRelease = state.githubUpdateConfirm,
                    onBack = { viewModel.navigateBack() },
                    onVisible = { viewModel.onGithubUpdateScreenVisible() },
                    onRequestDownload = { viewModel.requestGithubDownload() },
                    onConfirmDownload = { viewModel.confirmGithubDownload() },
                    onDismissConfirm = { viewModel.dismissGithubDownloadConfirm() }
                )

                AppScreen.IflytekSettings -> IflytekSettingsScreen(
                    credentials = state.iflytekCredentials,
                    onBack = { viewModel.navigateBack() },
                    onSave = { viewModel.saveIflytekCredentials(it) }
                )

                AppScreen.FeedingQuickPhrases -> FeedingQuickPhraseScreen(
                    phrases = state.feedingQuickPhrases,
                    onBack = { viewModel.navigateBack() },
                    onAdd = { viewModel.addFeedingQuickPhrase(it) },
                    onUpdate = { index, text -> viewModel.updateFeedingQuickPhrase(index, text) },
                    onDelete = { viewModel.deleteFeedingQuickPhrase(it) }
                )

                AppScreen.StorageManage -> StorageManageScreen(
                    info = state.storageInfo,
                    onBack = { viewModel.navigateBack() },
                    onExportBackup = {
                        val name = "snap-note-backup-${DateUtils.toDateKey(System.currentTimeMillis())}.zip"
                        exportBackupLauncher.launch(name)
                    },
                    onRestoreBackup = { importBackupLauncher.launch(arrayOf("application/zip")) },
                    onEmptyTrash = { viewModel.emptyTrashPermanently() },
                    onClearAsrLogs = { viewModel.clearAsrLogs() },
                    onAddShortcut = { viewModel.pinRecordShortcut() }
                )

                AppScreen.RecordExport -> RecordExportScreen(
                    homeMode = state.homeMode,
                    selectedDateKey = state.exportDateKey,
                    memos = state.exportMemos,
                    meals = state.exportMeals,
                    feedings = state.exportFeedings,
                    chronicles = state.exportChronicles,
                    onBack = { viewModel.navigateBack() },
                    onToggleMode = { viewModel.toggleHomeMode() },
                    onDateSelected = { viewModel.selectExportDate(it) },
                    onCopy = { viewModel.copyExportToClipboard() }
                )

                AppScreen.MemoEdit -> {
                    val memo = viewModel.getEditingMemo()
                    if (memo != null) {
                        MemoEditScreen(
                            item = memo,
                            onBack = { viewModel.navigateBack() },
                            onSave = { viewModel.saveMemoEdit(it) }
                        )
                    }
                }

                AppScreen.MealConfirm -> {
                    val confirm = state.mealConfirm
                    if (confirm != null) {
                        MealConfirmScreen(
                            state = confirm,
                            onBack = { viewModel.cancelMealConfirm() },
                            onDishesChange = { viewModel.updateMealConfirmDishes(it) },
                            onNoteChange = { viewModel.updateMealConfirmNote(it) },
                            onSave = { viewModel.confirmSaveMeal() }
                        )
                    }
                }

                AppScreen.MealDetail -> {
                    val meal = viewModel.getEditingMeal()
                    if (meal != null) {
                        MealDetailScreen(
                            item = meal,
                            aiPhase = state.mealAiPhase,
                            aiPreview = state.mealAiPreview,
                            aiError = state.mealAiError,
                            onBack = { viewModel.navigateBack() },
                            onSave = { dishes, note, timestamp, aiComment ->
                                viewModel.saveMealEdit(dishes, note, timestamp, aiComment)
                            },
                            onDelete = { viewModel.deleteMeal(meal.id) },
                            onAnalyze = { viewModel.startMealAiAnalyze() },
                            onDiscardPreview = { viewModel.discardMealAiPreview() },
                            onAdopt = { viewModel.adoptMealAiComment() },
                            onCancelAnalyze = { viewModel.cancelMealAiAnalyze() }
                        )
                    }
                }

                AppScreen.LogList -> LogListScreen(
                    logs = state.asrLogs,
                    onBack = { viewModel.navigateBack() }
                )

                AppScreen.RecycleBin -> RecycleBinScreen(
                    homeMode = state.homeMode,
                    items = state.trashItems,
                    mealItems = state.mealTrashItems,
                    feedingItems = state.feedingTrashItems,
                    chronicleItems = state.chronicleTrashItems,
                    onBack = { viewModel.navigateBack() },
                    onRestore = { viewModel.restoreFromTrash(it) },
                    onDeletePermanently = { viewModel.deletePermanently(it) }
                )
            }
            }
            }
            }

            if (state.showChronicleSheet) {
                ChronicleRecordSheet(
                    editItem = viewModel.getEditingChronicle(),
                    onDismiss = { viewModel.dismissChronicleSheet() },
                    onSave = { type, value, title, body, images, timestamp ->
                        viewModel.submitChronicleRecord(type, value, title, body, images, timestamp)
                    }
                )
            }

            if (state.showFeedingQuickAdd) {
                FeedingRecordSheet(
                    editItem = viewModel.getEditingFeeding(),
                    quickPhrases = state.feedingQuickPhrases,
                    lastAmountMl = state.lastFeedingAmountMl,
                    feedingNoteRecording = state.feedingNoteRecording,
                    feedingNoteTranscribing = state.feedingNoteTranscribing,
                    feedingNoteRecordingDuration = state.feedingNoteRecordingDuration,
                    feedingNoteVoiceResult = state.feedingNoteVoiceResult,
                    onDismiss = { viewModel.dismissFeedingQuickAdd() },
                    onSave = { amount, note, timestamp, onComplete ->
                        viewModel.submitFeedingRecord(amount, note, timestamp, onComplete)
                    },
                    onFeedingNoteVoiceToggle = {
                        if (!ensureMicPermission()) return@FeedingRecordSheet
                        viewModel.toggleFeedingNoteVoice()
                    },
                    onFeedingNoteVoiceResultConsumed = { viewModel.clearFeedingNoteVoiceResult() }
                )
            }
            }
        }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleQuickRecordIntent(intent)
    }

    private fun handleQuickRecordIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_QUICK_RECORD, false) == true) {
            viewModel.goHome()
            Toast.makeText(this, getString(R.string.toast_quick_record_hint), Toast.LENGTH_SHORT).show()
        }
    }

    private fun ensureMicPermission(): Boolean {
        if (micGranted) return true
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureCameraAndCapture() {
        viewModel.dismissMealSourceSheet()
        if (cameraGranted || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraGranted = true
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val file = MealImageStore.createTempCameraFile(cacheDir)
        pendingCameraFile = file
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        takePictureLauncher.launch(uri)
    }

    private fun installDownloadedApk(absolutePath: String) {
        val file = File(absolutePath)
        if (!file.exists()) {
            Toast.makeText(this, getString(R.string.toast_install_file_missing), Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                Toast.makeText(this, getString(R.string.toast_allow_unknown_sources), Toast.LENGTH_LONG).show()
                val settingsIntent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName")
                )
                startActivity(settingsIntent)
                return
            }
        }
        startActivity(intent)
    }
}
