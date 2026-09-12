package de.niklasschmitt.locker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Bare-minimum accessibility service: it exists only so [LockActivity] can call
 * performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN), which a plain app isn't allowed to do.
 * No overlay, no touch handling, no reaction to events - Quick Tap already does the
 * gesture detection in hardware, this just performs the actual lock.
 *
 * Unlike DevicePolicyManager.lockNow() (device admin), this does not mark the lock as
 * "administrator locked", so fingerprint/face unlock keep working normally afterwards.
 */
@Suppress("AccessibilityPolicy") // legitimate self-service use, see AndroidManifest.xml
class LockAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) = Unit

    override fun onInterrupt() = Unit

    companion object {
        private var instance: LockAccessibilityService? = null

        /** Returns true if the lock action actually fired (i.e. the service is enabled). */
        fun lock(): Boolean = instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) ?: false
    }
}
