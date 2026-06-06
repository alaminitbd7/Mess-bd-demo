package com.example.util

import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class MessBackupWrapper(
    val members: List<MessMember>,
    val payments: List<Payment>,
    val invoices: List<Invoice>,
    val receipts: List<Receipt>,
    val expenses: List<Expense>
)

object BackupHelper {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private var adapter = moshi.adapter(MessBackupWrapper::class.java)

    /**
     * Converts lists of all Room db entries into a single, formatted JSON wrapper.
     */
    fun exportToJson(
        members: List<MessMember>,
        payments: List<Payment>,
        invoices: List<Invoice>,
        receipts: List<Receipt>,
        expenses: List<Expense>
    ): String {
        val wrapper = MessBackupWrapper(members, payments, invoices, receipts, expenses)
        return adapter.indent("  ").toJson(wrapper)
    }

    /**
     * Deserializes the JSON back into our typed database lists wrapper.
     */
    fun importFromJson(jsonStr: String): MessBackupWrapper? {
        return try {
            adapter.fromJson(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
