package com.example.util

object ValidationHelper {

    /**
     * Validates Bangladeshi phone numbers (e.g. 01700000000 or +8801700000000)
     */
    fun isValidBDPhone(phone: String): Boolean {
        val cleaned = phone.replace(" ", "").replace("-", "")
        val regex = Regex("^(?:\\+88|88)?(01[3-9]\\d{8})$")
        return regex.matches(cleaned)
    }

    /**
     * Validates that amounts are double positive values.
     */
    fun isValidAmount(amountStr: String): Double? {
        val amount = amountStr.toDoubleOrNull()
        return if (amount != null && amount > 0.0) amount else null
    }

    /**
     * Optional regex validation for MFS Txn ID codes.
     */
    fun isValidTxnId(txnId: String): Boolean {
        if (txnId.isEmpty()) return true
        // MFS transactions are usually alphanumeric, length 8 to 12
        val regex = Regex("^[A-Z0-9]{8,12}$", RegexOption.IGNORE_CASE)
        return regex.matches(txnId)
    }
}
