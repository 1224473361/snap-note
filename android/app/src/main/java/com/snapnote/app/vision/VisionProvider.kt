package com.snapnote.app.vision

import java.io.File

interface VisionProvider {
    suspend fun recognizeDishes(imageFile: File): List<String>
}
