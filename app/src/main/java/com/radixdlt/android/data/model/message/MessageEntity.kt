package com.radixdlt.android.data.model.message

import androidx.room.Entity

@Entity(
    primaryKeys = ["timestamp"]
)
data class MessageEntity(
    val fromAddress: String,
    val toAddress: String,
    val content: String,
    val timestamp: Long
)
