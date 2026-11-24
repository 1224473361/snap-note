package com.snapnote.app.data

import android.content.Context
import com.google.gson.Gson
import com.snapnote.app.model.DeletedMemoItem
import com.snapnote.app.model.MemoItem
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupService(
    private val context: Context,
    private val repository: MemoRepository
) {
    private val gson = Gson()

    fun exportTo(output: OutputStream) {
        val payload = BackupPayload(
            memos = repository.getAllMemos(),
            trash = repository.getAllTrash()
        )
        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
            zip.write(gson.toJson(payload).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            val audioFiles = (payload.memos.map { it.audioPath } + payload.trash.map { it.item.audioPath })
                .map { File(it) }
                .filter { it.exists() }
                .distinctBy { it.name }

            audioFiles.forEach { file ->
                zip.putNextEntry(ZipEntry("$AUDIO_DIR/${file.name}"))
                file.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    suspend fun importFrom(input: InputStream, replaceExisting: Boolean = true) {
        val tempDir = File(context.cacheDir, "backup_import_${System.currentTimeMillis()}").apply { mkdirs() }
        var payload: BackupPayload? = null

        ZipInputStream(BufferedInputStream(input)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                when {
                    entry.name == MANIFEST_ENTRY -> {
                        payload = gson.fromJson(zip.reader(Charsets.UTF_8).readText(), BackupPayload::class.java)
                    }
                    entry.name.startsWith("$AUDIO_DIR/") -> {
                        val outFile = File(tempDir, entry.name.substringAfterLast('/'))
                        outFile.outputStream().use { zip.copyTo(it) }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val data = payload ?: throw IllegalStateException("备份文件无效")
        val audioDir = repository.audioDirectory()
        tempDir.listFiles()?.forEach { tempFile ->
            val target = File(audioDir, tempFile.name)
            tempFile.copyTo(target, overwrite = true)
        }
        tempDir.deleteRecursively()

        val remappedMemos = data.memos.map { memo ->
            memo.copy(audioPath = remapAudioPath(memo.audioPath, audioDir))
        }
        val remappedTrash = data.trash.map { deleted ->
            deleted.copy(item = deleted.item.copy(audioPath = remapAudioPath(deleted.item.audioPath, audioDir)))
        }

        if (replaceExisting) {
            repository.replaceAll(remappedMemos, remappedTrash)
        } else {
            repository.mergeAll(remappedMemos, remappedTrash)
        }
    }

    private fun remapAudioPath(originalPath: String, audioDir: File): String {
        val name = File(originalPath).name
        return File(audioDir, name).absolutePath
    }

    companion object {
        private const val MANIFEST_ENTRY = "manifest.json"
        private const val AUDIO_DIR = "audio"
    }
}
