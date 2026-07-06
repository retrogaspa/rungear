package com.appsho.sneakers.util

import android.content.Intent
import android.net.Uri
import android.os.Build

object ShareIntentParser {

    fun parseImageUri(intent: Intent?): Uri? {
        if (intent == null) return null
        val type = intent.type ?: return null
        if (!type.startsWith("image/")) return null

        return when (intent.action) {
            Intent.ACTION_SEND -> intent.streamUriExtra()
            Intent.ACTION_SEND_MULTIPLE -> intent.streamUriListExtra()?.firstOrNull()
            else -> null
        }
    }

    private fun Intent.streamUriExtra(): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(Intent.EXTRA_STREAM)
        }

    private fun Intent.streamUriListExtra(): ArrayList<Uri>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }
}
