package com.example.c001apk.compose.ui

import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.logic.smid.DigitalUnionDebugEntryPoint
import com.example.c001apk.compose.logic.smid.DuClient
import com.example.c001apk.compose.logic.smid.DuCodec
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Explicit opt-in device smoke test; ordinary test runs do not contact the registration service. */
class DigitalUnionNetworkTest {
    @Test
    fun kotlinRegistrationGeneratesAndPersistsAnId() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("smidNetwork") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        // Keep the opt-in query visible so HyperOS does not freeze the registration worker.
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "am start -W -n ${context.packageName}/androidx.activity.ComponentActivity",
        ).use { descriptor ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
        val repository =
            EntryPointAccessors.fromApplication(context, DigitalUnionDebugEntryPoint::class.java)
                .digitalUnion()
        val id = repository.ensureId(force = true).getOrThrow()
        assertTrue(id.length >= 16)
        val preferences =
            EntryPointAccessors.fromApplication(context, DigitalUnionDebugEntryPoint::class.java)
                .preferences()
        assertTrue("Generated ID must be persisted", id == preferences.data.first().szlmId)
    }

    @Test
    fun fallbackEndpointGeneratesAnIdWithoutPrimary() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("smidFallbackNetwork") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext.applicationContext
        instrumentation.uiAutomation.executeShellCommand(
            "am start -W -n ${context.packageName}/androidx.activity.ComponentActivity",
        ).use { descriptor ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
        }
        // Invoke the reference's unchanged report method with the fallback host only.
        val id = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val payload = DuClient::class.java.getDeclaredMethod("buildPayload", android.content.Context::class.java, String::class.java)
                .apply { isAccessible = true }.invoke(DuClient, context, null) as org.json.JSONObject
            val compressed = DuClient::class.java.getDeclaredMethod("deflate", ByteArray::class.java)
                .apply { isAccessible = true }.invoke(DuClient, payload.toString().toByteArray()) as ByteArray
            val path = "/a/mdna/report?v=8.7&t=m&p=${DuCodec.md5Upper("com.coolapk.market.37be8a1ee106979a")}" +
                "&r=${java.util.UUID.randomUUID().toString().replace("-", "")}&n=0&l=2&h=0"
            DuClient::class.java.getDeclaredMethod("report", String::class.java, ByteArray::class.java)
                .apply { isAccessible = true }
                .invoke(DuClient, "https://global-auni.checktrustworthiness.com$path", DuCodec.xor(compressed)) as String?
        }
        assertTrue("Fallback must return a nonzero ID", !id.isNullOrEmpty())
    }
}
