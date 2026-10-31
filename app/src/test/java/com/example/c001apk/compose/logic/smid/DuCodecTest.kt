package com.example.c001apk.compose.logic.smid

import org.junit.Assert.*
import org.junit.Test

class DuCodecTest {
    @Test fun referenceWireVectorsCoverEveryShuffle() {
        assertArrayEquals(byteArrayOf(3, 13), DuCodec.xor("{}".toByteArray()))
        assertEquals("dfceb", DuCodec.enc("abcde", 0))
        assertEquals("fbecd", DuCodec.enc("abcde", 100))
        assertEquals("fdbec", DuCodec.enc("abcde", 200))
        assertEquals("bdfce", DuCodec.enc("abcde", 300))
        assertEquals("", DuCodec.enc("", 200))
        assertEquals("b", DuCodec.enc("a", 200))
    }

    @Test fun identityTransformsMatchReferenceBoundaries() {
        assertEquals(456, DuCodec.param("1790000000.123000456"))
        assertEquals("012345689abcdef", DuCodec.dropChar("0123456789abcdef", 8))
        assertEquals("abc", DuCodec.dropChar("abcd", 8))
        assertEquals("CFCD208", DuCodec.checksum("a", 0))
        assertEquals("123456:DEF012:789ABC", DuCodec.fakeMac("0123456789ABCDEF0123456789ABCDEF0123456789"))
    }
}
