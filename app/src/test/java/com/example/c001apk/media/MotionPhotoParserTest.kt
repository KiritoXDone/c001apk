package com.example.c001apk.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets

class MotionPhotoParserTest {

    @Test
    fun externalMotionPhotoKeepsDeclaredVideoMetadata() {
        val file = temporaryFile(
            """
            <rdf:Description GCamera:MotionPhoto="1"
                GCamera:MotionPhotoVersion="1"
                GCamera:MotionPhotoPresentationTimestampUs="1433276" />
            <Container:Item Item:Mime="video/mp4" Item:Semantic="MotionPhoto"
                Item:Length="6606384" />
            """.trimIndent().toByteArray(StandardCharsets.ISO_8859_1),
        )

        val result = MotionPhotoParser.parse(file)

        assertTrue(result.declaresMotionPhoto)
        assertTrue(result.hasMotionPhoto)
        assertEquals(6_606_384L, result.declaredVideoLength)
        assertNull(result.embeddedVideoLength)
        assertEquals(1_433_276L, result.presentationTimestampUs)
        file.delete()
    }

    @Test
    fun embeddedMotionPhotoRequiresAnMp4AtTheDeclaredTail() {
        val video = byteArrayOf(
            0, 0, 0, 16,
            'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(),
            'm'.code.toByte(), 'p'.code.toByte(), '4'.code.toByte(), '2'.code.toByte(),
            0, 0, 0, 0,
        )
        val xmp = """
            <rdf:Description GCamera:MotionPhoto="1" />
            <Container:Item Item:Mime="video/mp4" Item:Semantic="MotionPhoto"
                Item:Length="${video.size}" />
        """.trimIndent().toByteArray(StandardCharsets.ISO_8859_1)
        val file = temporaryFile(xmp + video)

        val result = MotionPhotoParser.parse(file)

        assertEquals(video.size.toLong(), result.declaredVideoLength)
        assertEquals(video.size.toLong(), result.embeddedVideoLength)
        file.delete()
    }

    @Test
    fun ultraHdrDoesNotImplyMotionPhoto() {
        val file = temporaryFile(
            "<rdf:Description hdrgm:Version=\"1.0\" />padding"
                .toByteArray(StandardCharsets.ISO_8859_1),
        )

        val result = MotionPhotoParser.parse(file)

        assertTrue(result.hasUltraHdr)
        assertFalse(result.hasMotionPhoto)
        file.delete()
    }

    @Test
    fun livePhotoUrlHintSupportsExternalVideoWithoutXmp() {
        val file = temporaryFile(ByteArray(32) { it.toByte() })

        val result = MotionPhotoParser.parse(file, expectMotionPhoto = true)

        assertTrue(result.declaresMotionPhoto)
        assertTrue(result.hasMotionPhoto)
        assertNull(result.declaredVideoLength)
        assertNull(result.embeddedVideoLength)
        file.delete()
    }

    @Test
    fun imageWithoutGainMapMetadataIsNotUltraHdr() {
        val file = temporaryFile(ByteArray(32) { it.toByte() })

        val result = MotionPhotoParser.parse(file)

        assertFalse(result.hasUltraHdr)
        assertFalse(result.hasMotionPhoto)
        file.delete()
    }

    @Test
    fun urlHintPrefersLivePhotoWhenUrlAlsoContainsHdrMarker() {
        assertEquals(
            RichMediaHint.LIVE_PHOTO,
            "https://example.test/image-livepic-uhdr.jpg".coolApkRichMediaHint(),
        )
        assertEquals(
            RichMediaHint.ULTRA_HDR,
            "https://example.test/image-xhdr.jpg".coolApkRichMediaHint(),
        )
        assertEquals(
            RichMediaHint.NONE,
            "https://example.test/image.jpg".coolApkRichMediaHint(),
        )
    }

    private fun temporaryFile(bytes: ByteArray): File =
        File.createTempFile("motion-photo-parser-", ".jpg").apply { writeBytes(bytes) }
}
