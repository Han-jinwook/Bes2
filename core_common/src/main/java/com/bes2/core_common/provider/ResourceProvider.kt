package com.bes2.core_common.provider

import androidx.annotation.DrawableRes

/**
 * A provider interface to allow safe access to UI resources from other modules.
 */
interface ResourceProvider {
    @get:DrawableRes
    val notificationIcon: Int
}
