package com.hushnote.app.util

import android.content.Context
import android.net.Uri
import com.hushnote.app.model.ChronicleConstants
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ChronicleImageStore {
    fun imageDir(context: Context): File =
        File(context.filesDir, "chronicle_images").apply { mkdirs() }

    fun importFromUri(context: Context, uri: Uri): String? {
        val dir = imageDir(context)
        val dest = File(dir, "${UUID.randomUUID()}.jpg")
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            } ?: return null
            dest.absolutePath
        }.getOrNull()
    }

    fun deletePaths(paths: List<String>) {
        paths.forEach { path ->
            runCatching { File(path).delete() }
        }
    }

    fun trimToMax(paths: List<String>): List<String> =
        paths.take(ChronicleConstants.MAX_MILESTONE_IMAGES)
}
