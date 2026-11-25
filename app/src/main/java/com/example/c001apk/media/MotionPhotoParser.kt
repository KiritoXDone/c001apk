package com.example.c001apk.media

import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets

internal data class EmbeddedMediaInfo(
    val hasUltraHdr: Boolean,
    val declaresMotionPhoto: Boolean,
    val embeddedVideoLength: Long?,
    val declaredVideoLength: Long?,
    val presentationTimestampUs: Long?,
) {
    val hasMotionPhoto: Boolean
        get() = declaresMotionPhoto || embeddedVideoLength != null
}

internal object MotionPhotoParser {

    private const val MAX_XMP_SCAN_BYTES = 512 * 1024

    private val itemTagRegex = Regex("""<[^>]+Item:Mime=\"video/mp4\"[^>]*>""")
    private val lengthRegex = Regex("""Item:Length=\"(\d+)\"""")
    private val microVideoOffsetRegex = Regex("""GCamera:MicroVideoOffset=\"(\d+)\"""")
    private val onePlusVideoLengthRegex = Regex("""OpCamera:VideoLength=\"(\d+)\"""")
    private val presentationTimestampRegex =
        Regex("""(?:GCamera:)?MotionPhotoPresentationTimestampUs=\"(\d+)\"""")

    fun parse(
        file: File,
        expectMotionPhoto: Boolean = false,
    ): EmbeddedMediaInfo {
        if (!file.isFile || file.length() < 16L) {
            return EmbeddedMediaInfo(false, false, null, null, null)
        }

        val xmp = readPrefix(file)
        val hasUltraHdr = xmp.contains("hdrgm:Version", ignoreCase = true)
        val declaresMotionPhoto = expectMotionPhoto ||
            xmp.contains("GCamera:MotionPhoto=\"1\"") ||
            xmp.contains("GCamera:MicroVideo=\"1\"") ||
            xmp.contains("OpCamera:OLivePhotoVersion=\"")
        val declaredVideoLength = findContainerVideoLength(xmp)
            ?: microVideoOffsetRegex.find(xmp)?.groupValues?.get(1)?.toLongOrNull()
            ?: onePlusVideoLengthRegex.find(xmp)?.groupValues?.get(1)?.toLongOrNull()
        val embeddedVideoLength = declaredVideoLength
            ?.takeIf {
                (declaresMotionPhoto || expectMotionPhoto) &&
                    it in 12 until file.length() && hasFtypAtTail(file, it)
            }
            ?: if (declaresMotionPhoto || expectMotionPhoto) findMp4LengthFromTail(file) else null
        val presentationTimestampUs = presentationTimestampRegex.find(xmp)
            ?.groupValues
            ?.get(1)
            ?.toLongOrNull()

        return EmbeddedMediaInfo(
            hasUltraHdr = hasUltraHdr,
            declaresMotionPhoto = declaresMotionPhoto,
            embeddedVideoLength = embeddedVideoLength,
            declaredVideoLength = declaredVideoLength,
            presentationTimestampUs = presentationTimestampUs,
        )
    }

    private fun readPrefix(file: File): String {
        val length = minOf(file.length(), MAX_XMP_SCAN_BYTES.toLong()).toInt()
        val bytes = ByteArray(length)
        RandomAccessFile(file, "r").use { input ->
            input.readFully(bytes)
        }
        return String(bytes, StandardCharsets.ISO_8859_1)
    }

    private fun findContainerVideoLength(xmp: String): Long? {
        val tag = itemTagRegex.find(xmp)?.value ?: return null
        return lengthRegex.find(tag)?.groupValues?.get(1)?.toLongOrNull()
    }

    private fun hasFtypAtTail(file: File, videoLength: Long): Boolean {
        val offset = file.length() - videoLength
        return runCatching {
            RandomAccessFile(file, "r").use { input ->
                input.seek(offset + 4L)
                val type = ByteArray(4)
                input.readFully(type)
                String(type, StandardCharsets.US_ASCII) == "ftyp"
            }
        }.getOrDefault(false)
    }

    private fun findMp4LengthFromTail(file: File): Long? = runCatching {
        val scanLength = minOf(file.length(), 64L * 1024 * 1024).toInt()
        val scanOffset = file.length() - scanLength
        val bytes = ByteArray(scanLength)
        RandomAccessFile(file, "r").use { input ->
            input.seek(scanOffset)
            input.readFully(bytes)
        }
        for (index in 4 until bytes.size - 4) {
            if (bytes[index] == 'f'.code.toByte() &&
                bytes[index + 1] == 't'.code.toByte() &&
                bytes[index + 2] == 'y'.code.toByte() &&
                bytes[index + 3] == 'p'.code.toByte()
            ) {
                val boxSize = ((bytes[index - 4].toInt() and 0xff) shl 24) or
                    ((bytes[index - 3].toInt() and 0xff) shl 16) or
                    ((bytes[index - 2].toInt() and 0xff) shl 8) or
                    (bytes[index - 1].toInt() and 0xff)
                if (boxSize in 8..4096) {
                    val mp4Offset = scanOffset + index - 4L
                    return@runCatching file.length() - mp4Offset
                }
            }
        }
        null
    }.getOrNull()
}
