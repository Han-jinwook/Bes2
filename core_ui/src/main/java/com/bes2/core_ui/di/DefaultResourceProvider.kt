package com.bes2.core_ui.di

import com.bes2.core_common.provider.ResourceProvider
import com.bes2.core_ui.R
import javax.inject.Inject

class DefaultResourceProvider @Inject constructor() : ResourceProvider {
    override val notificationIcon: Int
        get() = R.drawable.ic_notification
}
