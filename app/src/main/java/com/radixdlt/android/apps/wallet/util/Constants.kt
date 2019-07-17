package com.radixdlt.android.apps.wallet.util

import androidx.annotation.StringDef

const val PREF_SECRET = "secretKey"
const val FAUCET_ADDRESS = "9ecjMNCFDSbLZxVpfbFwFTLWuL7SH3Q49uzGrpK3bUcze6CJtDr"
const val GENESIS_XRD = "/JHuDLpGefPssAY3v1pTXTQWHGv1tkTCEdq7RQYPnLuin1cfoath/XRD"
const val TOTAL = "TOTAL"
const val URL_REPORT_ISSUE = "https://radixdlt.typeform.com/to/kPFmVy"
const val KEYSTORE_FILE = "keystore.key"

@Retention(AnnotationRetention.SOURCE)
@StringDef(ALPHANET, ALPHANET2, BETANET)
annotation class Universe

const val ALPHANET = "ALPHANET"
const val ALPHANET2 = "ALPHANET2"
const val BETANET = "BETANET"

const val IPV4_ADDRESS_PATTERN =
    "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"

const val IPV6_ADDRESS_PATTERN = "^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}\$"
