package com.radixdlt.android.apps.wallet.data.model.message

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface MessagesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessages(messageEntity: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessage(messageEntity: MessageEntity)

    @Query("SELECT * FROM MessageEntity WHERE (fromAddress = :fromAddress AND toAddress = :toAddress) OR (fromAddress = :toAddress AND toAddress = :fromAddress) ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessageByAddresses(fromAddress: String, toAddress: String): Flowable<MutableList<MessageEntity>>

    @Query("SELECT * FROM MessageEntity WHERE (fromAddress = :fromAddress AND toAddress = :toAddress) OR (fromAddress = :toAddress AND toAddress = :fromAddress) ORDER BY timestamp")
    fun getAllMessagesByAddresses(fromAddress: String, toAddress: String): Maybe<MutableList<MessageEntity>>

    @Query("SELECT * FROM MessageEntity GROUP BY MIN(fromAddress, toAddress), MAX(fromAddress, toAddress) ORDER BY timestamp DESC")
    fun getLastMessageFromEachConversation(): Flowable<MutableList<MessageEntity>>

    @Query("DELETE FROM MessageEntity")
    fun deleteTable()
}
