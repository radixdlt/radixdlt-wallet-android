package com.radixdlt.android.util

import androidx.annotation.StringDef

const val PREF_SECRET = "secretKey"
const val FAUCET_ADDRESS = "9he94tVfQGAVr4xoUpG3uJfB2exURExzFV6E7dq4bxUWRbM5Edd" // alphanetwork
const val URL_REPORT_ISSUE = "https://radixdlt.typeform.com/to/kPFmVy"

@Retention(AnnotationRetention.SOURCE)
@StringDef(ALPHANET, ALPHANET2)
annotation class Universe

const val ALPHANET = "ALPHANET"
const val ALPHANET2 = "ALPHANET2"

enum class UniverseEnum {
    ALPHANET,
    ALPHANET2
}
