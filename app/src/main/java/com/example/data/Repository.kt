package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MessRepository(private val messDao: MessDao) {

    fun getMembers(messId: String): Flow<List<MessMember>> = messDao.getMembersByMess(messId)
    
    fun getPayments(messId: String): Flow<List<Payment>> = messDao.getPaymentsByMess(messId)
    fun searchPayments(messId: String, query: String): Flow<List<Payment>> = messDao.searchPayments(messId, query)

    fun getInvoices(messId: String): Flow<List<Invoice>> = messDao.getInvoicesByMess(messId)
    fun searchInvoices(messId: String, query: String): Flow<List<Invoice>> = messDao.searchInvoices(messId, query)

    fun getReceipts(messId: String): Flow<List<Receipt>> = messDao.getReceiptsByMess(messId)
    fun searchReceipts(messId: String, query: String): Flow<List<Receipt>> = messDao.searchReceipts(messId, query)

    fun getExpenses(messId: String): Flow<List<Expense>> = messDao.getExpensesByMess(messId)

    // --- Core Operations ---
    suspend fun insertMember(member: MessMember) = messDao.insertMember(member)
    suspend fun deleteMember(id: String) = messDao.deleteMember(id)

    suspend fun insertInvoice(invoice: Invoice) = messDao.insertInvoice(invoice)
    suspend fun deleteInvoice(id: String) = messDao.deleteInvoice(id)

    suspend fun insertExpense(expense: Expense) = messDao.insertExpense(expense)
    suspend fun deleteExpense(id: String) = messDao.deleteExpense(id)

    /**
     * Professional ERP Transaction:
     * When a payment is recorded, also update the linked Invoice's due amount
     * and automatically generate a companion Receipt.
     */
    suspend fun recordPayment(
        amount: Double,
        method: String,
        transactionId: String,
        memberId: String,
        memberName: String,
        messId: String,
        invoiceId: String?
    ) {
        val paymentId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // 1. Save payment
        val payment = Payment(
            id = paymentId,
            amount = amount,
            dateMillis = timestamp,
            transactionId = transactionId.ifEmpty { "TXN-${timestamp / 1000}" },
            paymentMethod = method,
            memberId = memberId,
            memberName = memberName,
            messId = messId,
            isSynced = false,
            invoiceId = invoiceId
        )
        messDao.insertPayment(payment)

        // 2. Reduce Due on associated Invoice
        if (invoiceId != null) {
            val invoice = messDao.getInvoiceById(invoiceId)
            if (invoice != null) {
                val newPaid = invoice.paidAmount + amount
                val newDue = (invoice.totalPayable - newPaid).coerceAtLeast(0.0)
                
                val updatedInvoice = invoice.copy(
                    paidAmount = newPaid,
                    dueAmount = newDue,
                    isSynced = false
                )
                messDao.insertInvoice(updatedInvoice)

                // 3. Generate Receipt
                val rcNum = "RCP-${timestamp / 100000}-${(100..999).random()}"
                val receipt = Receipt(
                    id = UUID.randomUUID().toString(),
                    receiptNumber = rcNum,
                    invoiceId = invoiceId,
                    amountPaid = amount,
                    dateMillis = timestamp,
                    remarks = "Payment captured via $method on ${invoice.billingMonth}",
                    memberId = memberId,
                    memberName = memberName,
                    messId = messId,
                    paymentMethod = method,
                    isSynced = false
                )
                messDao.insertReceipt(receipt)
            }
        }
    }

    suspend fun deletePayment(payment: Payment) {
        messDao.deletePayment(payment.id)
        // If it was linked to an invoice, restore the due balance
        if (payment.invoiceId != null) {
            val invoice = messDao.getInvoiceById(payment.invoiceId)
            if (invoice != null) {
                val newPaid = (invoice.paidAmount - payment.amount).coerceAtLeast(0.0)
                val newDue = (invoice.totalPayable - newPaid).coerceAtLeast(0.0)
                messDao.insertInvoice(invoice.copy(
                    paidAmount = newPaid,
                    dueAmount = newDue
                ))
            }
        }
    }

    suspend fun deleteReceipt(receiptId: String) {
        messDao.deleteReceipt(receiptId)
    }

    // --- Bulk Operations for Backup/Restore ---
    suspend fun clearAllData() {
        messDao.clearMembers()
        messDao.clearPayments()
        messDao.clearInvoices()
        messDao.clearReceipts()
        messDao.clearExpenses()
    }

    /**
     * Prepends the database with premium mock dataset on clean install to avoid dry screens.
     */
    suspend fun prepopulateDemoData(messId: String) {
        // Members
        val m1 = MessMember("m1", "Alamin Imran", "+8801700000001", "alamin@smartmess.com", "Super Admin", messId)
        val m2 = MessMember("m2", "Karim Uddin", "+8801800000002", "karim@smartmess.com", "Manager", messId)
        val m3 = MessMember("m3", "Sabbir Hossain", "+8801900000003", "sabbir@smartmess.com", "Admin", messId)
        val m4 = MessMember("m4", "Sumon Ahmed", "+8801500000004", "sumon@smartmess.com", "Member", messId)

        insertMember(m1)
        insertMember(m2)
        insertMember(m3)
        insertMember(m4)

        // Invoices
        val now = System.currentTimeMillis()
        val inv1 = Invoice("inv1", "INV-2026-001", "June 2026", 4500.0, 3000.0, 1500.0, "m1", "Alamin Imran", messId, now - 5 * 86400000)
        val inv2 = Invoice("inv2", "INV-2026-002", "June 2026", 4200.0, 4200.0, 0.0, "m2", "Karim Uddin", messId, now - 4 * 86400000)
        val inv3 = Invoice("inv3", "INV-2026-003", "June 2026", 4500.0, 0.0, 4500.0, "m4", "Sumon Ahmed", messId, now - 3 * 86400000)

        insertInvoice(inv1)
        insertInvoice(inv2)
        insertInvoice(inv3)

        // Payments (with correct transaction numbers matching Bangladeshi MFS trends)
        val p1 = Payment("p1", 3000.0, now - 4 * 86400000, "BK2606A035", "bKash", "m1", "Alamin Imran", messId, false, "inv1")
        val p2 = Payment("p2", 4200.0, now - 3 * 86400000, "NG985L29X3", "Nagad", "m2", "Karim Uddin", messId, false, "inv2")
        
        messDao.insertPayment(p1)
        messDao.insertPayment(p2)

        // Receipts
        val r1 = Receipt("r1", "RCP-260655-301", "inv1", 3000.0, now - 4 * 86400000, "June partial mess meal token payment", "m1", "Alamin Imran", messId, "bKash")
        val r2 = Receipt("r2", "RCP-260656-788", "inv2", 4200.0, now - 3 * 86400000, "June full dining bill clearance", "m2", "Karim Uddin", messId, "Nagad")
        
        messDao.insertReceipt(r1)
        messDao.insertReceipt(r2)

        // Expenses
        val exp1 = Expense("exp1", "Bazaar", 2400.0, now - 2 * 86400000, "Daily fish, eggs and vegetables purchase", "Karim Uddin", messId)
        val exp2 = Expense("exp2", "Rent & Utility", 5000.0, now - 1 * 86400000, "Broadband Internet and Gas bills", "Alamin Imran", messId)
        
        insertExpense(exp1)
        insertExpense(exp2)
    }
}
