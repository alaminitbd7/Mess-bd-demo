package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessDao {
    // --- Members ---
    @Query("SELECT * FROM members WHERE messId = :messId ORDER BY name ASC")
    fun getMembersByMess(messId: String): Flow<List<MessMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MessMember)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMember(id: String)

    @Query("DELETE FROM members")
    suspend fun clearMembers()

    // --- Payments ---
    @Query("SELECT * FROM payments WHERE messId = :messId ORDER BY dateMillis DESC")
    fun getPaymentsByMess(messId: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE messId = :messId AND (memberName LIKE '%' || :query || '%' OR transactionId LIKE '%' || :query || '%') ORDER BY dateMillis DESC")
    fun searchPayments(messId: String, query: String): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: String)

    @Query("DELETE FROM payments")
    suspend fun clearPayments()

    // --- Invoices ---
    @Query("SELECT * FROM invoices WHERE messId = :messId ORDER BY dateMillis DESC")
    fun getInvoicesByMess(messId: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE messId = :messId AND (memberName LIKE '%' || :query || '%' OR invoiceNumber LIKE '%' || :query || '%') ORDER BY dateMillis DESC")
    fun searchInvoices(messId: String, query: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    suspend fun getInvoiceById(invoiceId: String): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: String)

    @Query("DELETE FROM invoices")
    suspend fun clearInvoices()

    // --- Receipts ---
    @Query("SELECT * FROM receipts WHERE messId = :messId ORDER BY dateMillis DESC")
    fun getReceiptsByMess(messId: String): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE messId = :messId AND (memberName LIKE '%' || :query || '%' OR receiptNumber LIKE '%' || :query || '%') ORDER BY dateMillis DESC")
    fun searchReceipts(messId: String, query: String): Flow<List<Receipt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceipt(id: String)

    @Query("DELETE FROM receipts")
    suspend fun clearReceipts()

    // --- Expenses ---
    @Query("SELECT * FROM expenses WHERE messId = :messId ORDER BY dateMillis DESC")
    fun getExpensesByMess(messId: String): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()
}
