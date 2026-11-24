package com.example.c001apk.imageviewer

import org.junit.Assert.*
import org.junit.Test

class ImageLoadStateTest {
    @Test
    fun missingThumbnailCanFallBackToOriginalWithoutPreviewSuccess() {
        val state = ImageLoadState(hasDistinctTarget = true)
        assertTrue(state.beginTarget())
        assertFalse(state.beginTarget())
        state.targetFailed()
        assertTrue(state.beginTarget())
        state.acceptTarget()
        assertTrue(state.targetLoaded)
        assertFalse(state.acceptPreview())
        assertFalse(state.showTargetAction)
    }

    @Test
    fun coldPreviewAndMissingCachedTargetLeaveOriginalActionAvailable() {
        val state = ImageLoadState(hasDistinctTarget = true)
        assertTrue(state.beginTarget())
        state.targetFailed()
        assertTrue(state.acceptPreview())
        assertTrue(state.showTargetAction)
        assertTrue(state.beginTarget())
        state.acceptTarget()
        assertFalse(state.showTargetAction)
    }

    @Test
    fun lateThumbnailCannotReplaceAnAlreadyLoadedOriginal() {
        val state = ImageLoadState(hasDistinctTarget = true)
        assertTrue(state.beginTarget())
        state.acceptTarget()
        assertFalse(state.acceptPreview())
        assertTrue(state.targetLoaded)
    }

    @Test
    fun animationAndPreviewCompletionShareOneTargetRequest() {
        val state = ImageLoadState(hasDistinctTarget = true)
        state.acceptPreview()
        assertTrue(state.beginTarget())
        assertFalse(state.beginTarget())
        state.targetFailed()
        assertTrue(state.showTargetAction)
        assertTrue(state.beginTarget())
        state.acceptTarget()
        assertFalse(state.beginTarget())
    }

    @Test
    fun completeLongImageIsNotDownloadedAgainAsItsOwnTarget() {
        val state = ImageLoadState(hasDistinctTarget = false)
        assertTrue(state.acceptPreview())
        assertTrue(state.targetLoaded)
        assertFalse(state.showTargetAction)
        assertFalse(state.beginTarget())
    }
}
