package com.gec.emergency.core.common

import javax.inject.Inject

class DefaultAppInfoProvider @Inject constructor() : AppInfoProvider {
    override fun getAppName(): String {
        return "Ghana Emergency Connect"
    }
}
