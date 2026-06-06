package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MessMember(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String, // "Super Admin", "Admin", "Manager", "Member"
    val messId: String // "Mess A", "Mess B", "Mess C"
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey val id: String,
    val amount: Double,
    val dateMillis: Long,
    val transactionId: String, // Transaction ID from payment providers
    val paymentMethod: String, // "Cash", "bKash", "Nagad", "Rocket", "Card"
    val memberId: String,
    val memberName: String,
    val messId: String,
    val isSynced: Boolean = false,
    val invoiceId: String? = null
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val billingMonth: String, // e.g., "June 2026"
    val totalPayable: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val memberId: String,
    val memberName: String,
    val messId: String,
    val dateMillis: Long,
    val isSynced: Boolean = false
)

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey val id: String,
    val receiptNumber: String,
    val invoiceId: String,
    val amountPaid: Double,
    val dateMillis: Long,
    val remarks: String,
    val memberId: String,
    val memberName: String,
    val messId: String,
    val paymentMethod: String,
    val isSynced: Boolean = false
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String,
    val category: String, // "Bazaar", "Rent & Utility", "Gas", "Current & Water", "Others"
    val amount: Double,
    val dateMillis: Long,
    val description: String,
    val spenderName: String,
    val messId: String
)
