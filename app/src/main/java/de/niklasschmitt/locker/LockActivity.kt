package de.niklasschmitt.locker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

/**
 * No UI: locks the screen immediately, then closes itself. Meant to be launched as the
 * target of Pixel's "Quick Tap" (double-tap the back of the phone) -> "Open app" action,
 * not opened by hand.
 */
class LockActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!LockAccessibilityService.lock()) {
            // Service not enabled (yet) - send the user to turn it on once.
            Toast.makeText(this, R.string.accessibility_required, Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        finish()
    }
}
