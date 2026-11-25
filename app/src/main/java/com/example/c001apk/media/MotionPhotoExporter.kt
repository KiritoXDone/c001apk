package com.example.c001apk.media

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import java.io.StringWriter
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.xml.sax.InputSource

internal object MotionPhotoExporter {

    fun export(
        sourceImage: File,
        video: File?,
        destination: File,
        presentationTimestampUs: Long?,
    ): File {
        require(sourceImage.isFile) { "Motion photo source image is unavailable" }
        destination.parentFile?.mkdirs()
        val staging = File(destination.parentFile, "${destination.name}.part")
        if (staging.exists()) staging.delete()

        try {
            val sourceInfo = MotionPhotoParser.parse(sourceImage, expectMotionPhoto = true)
            sourceImage.copyTo(staging, overwrite = true)
            if (sourceInfo.embeddedVideoLength == null) {
                val motionVideo = requireNotNull(video?.takeIf { it.isFile && isMp4(it) }) {
                    "Motion photo video is unavailable"
                }
                updateMotionPhotoXmp(
                    image = staging,
                    videoLength = motionVideo.length(),
                    presentationTimestampUs = presentationTimestampUs ?: 0L,
                )
                FileOutputStream(staging, true).use { output ->
                    motionVideo.inputStream().use { input -> input.copyTo(output) }
                }
            }

            val exportedInfo = MotionPhotoParser.parse(staging, expectMotionPhoto = true)
            check(exportedInfo.embeddedVideoLength != null) {
                "Exported file does not contain a readable motion photo video"
            }
            if (destination.exists()) check(destination.delete())
            if (!staging.renameTo(destination)) {
                staging.copyTo(destination, overwrite = true)
                check(staging.delete())
            }
            return destination
        } catch (error: Throwable) {
            staging.delete()
            throw error
        }
    }

    private fun updateMotionPhotoXmp(
        image: File,
        videoLength: Long,
        presentationTimestampUs: Long,
    ) {
        val document = readStandardXmp(image)
            ?.takeIf(String::isNotBlank)
            ?.let(::parseDocument)
            ?: newDocument()
        val rdf = document.findOrCreateRdf()
        val description = rdf.findMotionDescription() ?: document.createElementNS(
            RDF_NAMESPACE,
            "rdf:Description",
        ).also(rdf::appendChild)

        description.declareNamespace("GCamera", GCAMERA_NAMESPACE)
        description.declareNamespace("Container", CONTAINER_NAMESPACE)
        description.declareNamespace("Item", ITEM_NAMESPACE)
        description.setAttributeNS(GCAMERA_NAMESPACE, "GCamera:MotionPhoto", "1")
        description.setAttributeNS(GCAMERA_NAMESPACE, "GCamera:MotionPhotoVersion", "1")
        description.setAttributeNS(
            GCAMERA_NAMESPACE,
            "GCamera:MotionPhotoPresentationTimestampUs",
            presentationTimestampUs.toString(),
        )
        description.setAttributeNS(GCAMERA_NAMESPACE, "GCamera:MicroVideo", "1")
        description.setAttributeNS(GCAMERA_NAMESPACE, "GCamera:MicroVideoVersion", "1")
        description.setAttributeNS(
            GCAMERA_NAMESPACE,
            "GCamera:MicroVideoOffset",
            videoLength.toString(),
        )

        val oldDirectories = document.getElementsByTagNameNS(CONTAINER_NAMESPACE, "Directory")
        for (index in oldDirectories.length - 1 downTo 0) {
            oldDirectories.item(index).let { it.parentNode.removeChild(it) }
        }
        description.appendChild(document.createMotionPhotoDirectory(videoLength))
        writeStandardXmp(image, serialize(document))
    }

    private fun readStandardXmp(image: File): String? =
        DataInputStream(BufferedInputStream(image.inputStream())).use { input ->
            require(input.readUnsignedShort() == JPEG_SOI) { "Source is not a JPEG image" }
            while (true) {
                val marker = readMarker(input)
                if (marker == JPEG_SOS || marker == JPEG_EOI) return@use null
                if (marker.isStandaloneJpegMarker()) continue
                val data = readSegment(input)
                if (marker == JPEG_APP1 && data.startsWith(XMP_HEADER)) {
                    return@use String(
                        data,
                        XMP_HEADER.size,
                        data.size - XMP_HEADER.size,
                        Charsets.UTF_8,
                    ).trimEnd('\u0000')
                }
            }
            null
        }

    private fun writeStandardXmp(image: File, xmp: String) {
        val xmpData = XMP_HEADER + xmp.toByteArray(Charsets.UTF_8)
        require(xmpData.size + 2 <= 0xffff) { "Motion photo XMP packet is too large" }
        val rewritten = File(image.parentFile, "${image.name}.xmp")
        if (rewritten.exists()) rewritten.delete()

        try {
            DataInputStream(BufferedInputStream(image.inputStream())).use { input ->
                DataOutputStream(BufferedOutputStream(rewritten.outputStream())).use { output ->
                    require(input.readUnsignedShort() == JPEG_SOI) { "Source is not a JPEG image" }
                    output.writeShort(JPEG_SOI)
                    var replaced = false
                    while (true) {
                        val marker = readMarker(input)
                        if (marker == JPEG_SOS) {
                            if (!replaced) writeXmpSegment(output, xmpData)
                            output.writeByte(JPEG_MARKER_PREFIX)
                            output.writeByte(marker)
                            val scanHeader = readSegment(input)
                            output.writeShort(scanHeader.size + 2)
                            output.write(scanHeader)
                            input.copyTo(output)
                            break
                        }
                        if (marker == JPEG_EOI) {
                            if (!replaced) writeXmpSegment(output, xmpData)
                            output.writeByte(JPEG_MARKER_PREFIX)
                            output.writeByte(marker)
                            input.copyTo(output)
                            break
                        }

                        output.writeByte(JPEG_MARKER_PREFIX)
                        output.writeByte(marker)
                        if (marker.isStandaloneJpegMarker()) continue
                        val data = readSegment(input)
                        if (marker == JPEG_APP1 && data.startsWith(XMP_HEADER) && !replaced) {
                            output.writeShort(xmpData.size + 2)
                            output.write(xmpData)
                            replaced = true
                        } else {
                            output.writeShort(data.size + 2)
                            output.write(data)
                        }
                    }
                }
            }
            check(image.delete())
            check(rewritten.renameTo(image))
        } catch (error: Throwable) {
            rewritten.delete()
            throw error
        }
    }

    private fun readMarker(input: DataInputStream): Int {
        require(input.readUnsignedByte() == JPEG_MARKER_PREFIX) { "Invalid JPEG marker" }
        var marker = input.readUnsignedByte()
        while (marker == JPEG_MARKER_PREFIX) marker = input.readUnsignedByte()
        require(marker != 0) { "Unexpected stuffed JPEG marker before image data" }
        return marker
    }

    private fun readSegment(input: DataInputStream): ByteArray {
        val length = input.readUnsignedShort()
        require(length >= 2) { "Invalid JPEG segment length" }
        return ByteArray(length - 2).also(input::readFully)
    }

    private fun writeXmpSegment(output: DataOutputStream, xmpData: ByteArray) {
        output.writeByte(JPEG_MARKER_PREFIX)
        output.writeByte(JPEG_APP1)
        output.writeShort(xmpData.size + 2)
        output.write(xmpData)
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

    private fun Int.isStandaloneJpegMarker(): Boolean =
        this == 0x01 || this in 0xd0..0xd8

    private fun parseDocument(xmp: String): Document {
        require(!xmp.contains("<!DOCTYPE", ignoreCase = true)) {
            "Motion photo XMP must not contain a document type declaration"
        }
        return documentBuilderFactory()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(xmp)))
    }

    private fun newDocument(): Document = documentBuilderFactory()
        .newDocumentBuilder()
        .newDocument()
        .apply {
            val xmpMeta = createElementNS(XMP_META_NAMESPACE, "x:xmpmeta").apply {
                declareNamespace("x", XMP_META_NAMESPACE)
            }
            appendChild(xmpMeta)
            xmpMeta.appendChild(createElementNS(RDF_NAMESPACE, "rdf:RDF").apply {
                declareNamespace("rdf", RDF_NAMESPACE)
            })
        }

    private fun documentBuilderFactory(): DocumentBuilderFactory =
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isExpandEntityReferences = false
            runCatching {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
        }

    private fun Document.findOrCreateRdf(): Element {
        val existing = getElementsByTagNameNS(RDF_NAMESPACE, "RDF")
            .item(0) as? Element
        if (existing != null) return existing
        val root = documentElement ?: createElementNS(XMP_META_NAMESPACE, "x:xmpmeta").also {
            it.declareNamespace("x", XMP_META_NAMESPACE)
            appendChild(it)
        }
        return createElementNS(RDF_NAMESPACE, "rdf:RDF").also {
            it.declareNamespace("rdf", RDF_NAMESPACE)
            root.appendChild(it)
        }
    }

    private fun Element.findMotionDescription(): Element? {
        val descriptions = getElementsByTagNameNS(RDF_NAMESPACE, "Description")
        for (index in 0 until descriptions.length) {
            val description = descriptions.item(index) as? Element ?: continue
            if (description.hasAttributeNS(GCAMERA_NAMESPACE, "MotionPhoto") ||
                description.hasAttributeNS(GCAMERA_NAMESPACE, "MicroVideo")
            ) {
                return description
            }
        }
        return descriptions.item(0) as? Element
    }

    private fun Document.createMotionPhotoDirectory(videoLength: Long): Element {
        val directory = createElementNS(CONTAINER_NAMESPACE, "Container:Directory")
        val sequence = createElementNS(RDF_NAMESPACE, "rdf:Seq")
        directory.appendChild(sequence)
        sequence.appendChild(createMotionPhotoItem("image/jpeg", "Primary", 0L))
        sequence.appendChild(createMotionPhotoItem("video/mp4", "MotionPhoto", videoLength))
        return directory
    }

    private fun Document.createMotionPhotoItem(
        mime: String,
        semantic: String,
        length: Long,
    ): Element = createElementNS(RDF_NAMESPACE, "rdf:li").apply {
        setAttributeNS(RDF_NAMESPACE, "rdf:parseType", "Resource")
        appendChild(createElementNS(CONTAINER_NAMESPACE, "Container:Item").apply {
            setAttributeNS(ITEM_NAMESPACE, "Item:Mime", mime)
            setAttributeNS(ITEM_NAMESPACE, "Item:Semantic", semantic)
            setAttributeNS(ITEM_NAMESPACE, "Item:Length", length.toString())
            setAttributeNS(ITEM_NAMESPACE, "Item:Padding", "0")
        })
    }

    private fun Element.declareNamespace(prefix: String, namespace: String) {
        setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:$prefix", namespace)
    }

    private fun serialize(document: Document): String {
        val writer = StringWriter()
        TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.INDENT, "no")
        }.transform(DOMSource(document), StreamResult(writer))
        return writer.toString()
    }

    private fun isMp4(file: File): Boolean = runCatching {
        file.inputStream().use { input ->
            val header = ByteArray(8)
            input.read(header) == header.size &&
                header.copyOfRange(4, 8).contentEquals("ftyp".toByteArray(Charsets.US_ASCII))
        }
    }.getOrDefault(false)

    private const val XMP_META_NAMESPACE = "adobe:ns:meta/"
    private const val RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    private const val GCAMERA_NAMESPACE = "http://ns.google.com/photos/1.0/camera/"
    private const val CONTAINER_NAMESPACE = "http://ns.google.com/photos/1.0/container/"
    private const val ITEM_NAMESPACE = "http://ns.google.com/photos/1.0/container/item/"
    private const val JPEG_MARKER_PREFIX = 0xff
    private const val JPEG_SOI = 0xffd8
    private const val JPEG_APP1 = 0xe1
    private const val JPEG_SOS = 0xda
    private const val JPEG_EOI = 0xd9
    private val XMP_HEADER = "http://ns.adobe.com/xap/1.0/\u0000".toByteArray(Charsets.US_ASCII)
}
