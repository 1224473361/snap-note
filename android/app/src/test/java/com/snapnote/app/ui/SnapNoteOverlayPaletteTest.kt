package com.snapnote.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapNoteOverlayPaletteTest {
    @Test
    fun overlayKeepsDarkParentWhenDialogSurfaceIsLight() {
        val inferredFromDialog = if (Color.White.luminance() < 0.45f) {
            darkSnapNoteColorPalette
        } else {
            lightSnapNoteColorPalette
        }
        val overlay = snapNoteOverlayPalette(
            parentPalette = darkSnapNoteColorPalette,
            dialogMaterialBackground = Color.White
        )

        assertEquals(lightSnapNoteColorPalette, inferredFromDialog)
        assertEquals(darkSnapNoteColorPalette, overlay)
        assertNotEquals(inferredFromDialog, overlay)
    }

    @Test
    fun overlayKeepsLightParentWhenDialogSurfaceIsDark() {
        val overlay = snapNoteOverlayPalette(
            parentPalette = lightSnapNoteColorPalette,
            dialogMaterialBackground = Color(0xFF121212)
        )
        assertEquals(lightSnapNoteColorPalette, overlay)
    }

    @Test
    fun darkAndLightPageBackgroundsAreOnOppositeSidesOfThreshold() {
        assertTrue(darkSnapNoteColorPalette.pageBackground.luminance() < 0.45f)
        assertTrue(lightSnapNoteColorPalette.pageBackground.luminance() > 0.45f)
        assertTrue(darkSnapNoteColorPalette.textPrimary.luminance() > 0.5f)
        assertTrue(lightSnapNoteColorPalette.textPrimary.luminance() < 0.5f)
    }
}
