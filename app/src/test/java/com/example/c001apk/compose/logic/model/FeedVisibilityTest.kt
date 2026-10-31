package com.example.c001apk.compose.logic.model

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class FeedVisibilityTest {
    @Test fun readsBothNumericAndStringPrivateMarkers() {
        for (value in listOf("1", "\"1\"")) {
            val post = Gson().fromJson("""{"publish_status":$value}""", HomeFeedResponse.Data::class.java)
            assertEquals(1, post.publishStatus)
        }
    }

    @Test fun publicMissingAndOtherStatusesAreNotPrivate() {
        for (json in listOf("{}", """{"publish_status":0}""", """{"publish_status":"0"}""",
            """{"publish_status":null}""", """{"publish_status":2}""", """{"status":-1}""")) {
            assertNotEquals(1, Gson().fromJson(json, HomeFeedResponse.Data::class.java).publishStatus)
        }
    }
}
