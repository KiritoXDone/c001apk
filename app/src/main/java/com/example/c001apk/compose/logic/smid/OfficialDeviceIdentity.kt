package com.example.c001apk.compose.logic.smid

enum class DeviceIdentityInputError { TOO_LONG, MISSING_ID, INVALID_ID }

class DeviceIdentityInputException(val error: DeviceIdentityInputError) : IllegalArgumentException(error.name)

/** TG-Twilight's explicit ShuzlmID import format. Other copied identifiers are ignored. */
object OfficialDeviceIdentity {
    fun parse(text: String): String {
        if (text.length > 32768) throw DeviceIdentityInputException(DeviceIdentityInputError.TOO_LONG)
        val matches =
            Regex(
                    "^\\s*ShuzlmID\\s*[:：]\\s*([^\\r\\n]*)$",
                    setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE),
                )
                .findAll(text.replace("\uFEFF", ""))
                .toList()
        if (matches.size != 1) throw DeviceIdentityInputException(DeviceIdentityInputError.MISSING_ID)
        return matches.single().groupValues[1].trim().also {
            if (!valid(it)) throw DeviceIdentityInputException(DeviceIdentityInputError.INVALID_ID)
        }
    }

    fun valid(id: String) =
        !id.equals("null", true) &&
            !id.equals("undefined", true) &&
            Regex("^[A-Za-z0-9_+/.=-]{8,4096}$").matches(id)
}
