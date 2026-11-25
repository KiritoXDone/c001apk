package com.example.c001apk.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Base64

class MotionPhotoExporterTest {

    @Test
    fun externalVideoIsExportedAsReadableMotionPhoto() {
        val image = temporaryFile(".jpg", Base64.getDecoder().decode(ONE_PIXEL_JPEG))
        val video = temporaryFile(
            ".mp4",
            byteArrayOf(
                0, 0, 0, 24,
                'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(),
                'm'.code.toByte(), 'p'.code.toByte(), '4'.code.toByte(), '2'.code.toByte(),
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            ),
        )
        val output = File.createTempFile("motion-photo-export-", ".jpg").apply { delete() }

        MotionPhotoExporter.export(image, video, output, 1_433_276L)

        val parsed = MotionPhotoParser.parse(output)
        val exportedBytes = output.readBytes().toString(Charsets.ISO_8859_1)
        assertEquals(video.length(), parsed.embeddedVideoLength)
        assertEquals(1_433_276L, parsed.presentationTimestampUs)
        assertTrue(exportedBytes.contains("GCamera:MotionPhoto=\"1\""))
        assertTrue(exportedBytes.contains("Item:Length=\"${video.length()}\""))

        image.delete()
        video.delete()
        output.delete()
    }

    @Test
    fun existingHdrXmpIsPreservedWhenMotionMetadataIsAdded() {
        val jpeg = Base64.getDecoder().decode(ONE_PIXEL_JPEG)
        val image = temporaryFile(".jpg", jpeg.withXmp(HDR_XMP))
        val video = temporaryFile(
            ".mp4",
            byteArrayOf(
                0, 0, 0, 16,
                'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte(),
                'm'.code.toByte(), 'p'.code.toByte(), '4'.code.toByte(), '2'.code.toByte(),
                0, 0, 0, 0,
            ),
        )
        val output = File.createTempFile("motion-photo-hdr-export-", ".jpg").apply { delete() }

        MotionPhotoExporter.export(image, video, output, 0L)

        val exportedBytes = output.readBytes().toString(Charsets.ISO_8859_1)
        val parsed = MotionPhotoParser.parse(output)
        assertTrue(exportedBytes.contains("hdrgm:Version=\"1.0\""))
        assertTrue(parsed.hasUltraHdr)
        assertEquals(video.length(), parsed.embeddedVideoLength)

        image.delete()
        video.delete()
        output.delete()
    }

    private fun temporaryFile(suffix: String, bytes: ByteArray): File =
        File.createTempFile("motion-photo-exporter-", suffix).apply { writeBytes(bytes) }

    private fun ByteArray.withXmp(xmp: String): ByteArray {
        val scanStart = indices.first { index ->
            index + 1 < size && this[index] == 0xff.toByte() && this[index + 1] == 0xda.toByte()
        }
        val data = "http://ns.adobe.com/xap/1.0/\u0000".toByteArray(Charsets.US_ASCII) +
            xmp.toByteArray(Charsets.UTF_8)
        val segment = byteArrayOf(
            0xff.toByte(),
            0xe1.toByte(),
            ((data.size + 2) ushr 8).toByte(),
            (data.size + 2).toByte(),
        ) + data
        return copyOfRange(0, scanStart) + segment + copyOfRange(scanStart, size)
    }

    private companion object {
        const val ONE_PIXEL_JPEG =
            "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8U" +
                "HRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwL" +
                "DBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIy" +
                "MjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAA" +
                "AAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEG" +
                "E1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RF" +
                "RkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKj" +
                "pKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP0" +
                "9fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgEC" +
                "BAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLR" +
                "ChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0" +
                "dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbH" +
                "yMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD5/ooo" +
                "oA//2Q=="
        const val HDR_XMP =
            "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF " +
                "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                "<rdf:Description xmlns:hdrgm=\"http://ns.adobe.com/hdr-gain-map/1.0/\" " +
                "hdrgm:Version=\"1.0\" /></rdf:RDF></x:xmpmeta>"
    }
}
