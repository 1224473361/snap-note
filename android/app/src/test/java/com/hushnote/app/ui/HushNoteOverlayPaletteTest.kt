package com.hushnote.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HushNoteOverlayPaletteTest {
    @Test
    fun overlayKeepsDarkParentWhenDialogSurfaceIsLight() {
        val inferredFromDialog = if (Color.White.luminance() < 0.45f) {
            darkHushNoteColorPalette
        } else {
            lightHushNoteColorPalette
        }
        val overlay = hushNoteOverlayPalette(
            parentPalette = darkHushNoteColorPalette,
            dialogMaterialBackground = Color.White
        )

        assertEquals(lightHushNoteColorPalette, inferredFromDialog)
        assertEquals(darkHushNoteColorPalette, overlay)
        assertNotEquals(inferredFromDialog, overlay)
    }

    @Test
    fun overlayKeepsLightParentWhenDialogSurfaceIsDark() {
        val overlay = hushNoteOverlayPalette(
            parentPalette = lightHushNoteColorPalette,
            dialogMaterialBackground = Color(0xFF121212)
        )
        assertEquals(lightHushNoteColorPalette, overlay)
    }

    @Test
    fun darkAndLightPageBackgroundsAreOnOppositeSidesOfThreshold() {
        assertTrue(darkHushNoteColorPalette.pageBackground.luminance() < 0.45f)
        assertTrue(lightHushNoteColorPalette.pageBackground.luminance() > 0.45f)
        assertTrue(darkHushNoteColorPalette.textPrimary.luminance() > 0.5f)
        assertTrue(lightHushNoteColorPalette.textPrimary.luminance() < 0.5f)
    }
}
