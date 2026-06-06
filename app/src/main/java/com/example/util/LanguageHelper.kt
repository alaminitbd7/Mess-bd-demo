package com.example.util

import org.json.JSONObject

object LanguageHelper {
    enum class Lang { EN, BN }

    // --- Developer Settings Constant ---
    // Change this constant to "EN" or "BN" to toggle/set the default UI language of the app.
    const val DEFAULT_UI_LANGUAGE_SETTING = "EN"

    private const val LOCALIZATION_JSON = """
    {
      "en": {
        "app_title": "SMART MESS BD",
        "version_tag": "v2.6.3 STABLE",
        "btn_add_payment": "Add Payment",
        "btn_add_invoice": "New Invoice",
        "btn_add_expense": "Add Expense",
        "btn_add_member": "Add Member",
        "tab_dashboard": "Dashboard",
        "tab_invoices": "Invoices",
        "tab_receipts": "Receipts",
        "tab_payments": "Payments",
        "tab_reports": "Reports",
        "tab_settings": "Settings",
        "lbl_active_mess": "Active Mess",
        "lbl_switch_mess": "Switch Mess",
        "lbl_active_role": "Role",
        "lbl_currency": "৳",
        "lbl_total_collection": "Total Collection",
        "lbl_total_bazaar": "Total Bazaar Expense",
        "lbl_total_utility": "Other Utilities & Rent",
        "lbl_total_due": "Total Remaining Dues",
        "lbl_payment_history": "Payment History",
        "lbl_receipt_history": "Receipt Records",
        "lbl_due": "Due",
        "lbl_paid": "Paid",
        "lbl_total_payable": "Total Payable",
        "lbl_amount": "Amount",
        "lbl_method": "Payment Method",
        "lbl_txnid": "Transaction ID",
        "lbl_select_member": "Select Member",
        "lbl_select_invoice": "Select Associated Invoice",
        "lbl_date": "Date",
        "lbl_remarks": "Remarks & Description",
        "lbl_monthly_summary": "Monthly Financial Statement",
        "lbl_recent_activity": "Recent ERP Activity",
        "lbl_expense_by_category": "Expenses by Category",
        "lbl_payment_by_method": "Payments by Method",
        "lbl_due_by_member": "Due Balances by Member",
        "lbl_sub_status": "SaaS Subscription Status",
        "lbl_plan": "Active Plan",
        "lbl_expiry": "Expiry Date",
        "lbl_license": "License Status",
        "lbl_db_backup": "Database Backup & Portability",
        "lbl_backup_desc": "Export/Import complete ERP dataset as portable JSON",
        "btn_export_json": "Export Backup JSON",
        "btn_import_json": "Import / Restore Backup",
        "msg_backup_copied": "Backup JSON copied to clipboard!",
        "msg_import_success": "ERP Database successfully restored!",
        "msg_import_error": "Failed to parse JSON. Please check the backup format.",
        "msg_save_success": "Transaction successfully recorded!",
        "msg_delete_success": "Record permanent deletion completed!",
        "validation_empty_fields": "Please fill in all mandatory fields.",
        "validation_invalid_amount": "Amount must be a positive number.",
        "validation_invalid_phone": "Please enter a valid Bangladeshi phone number.",
        "role_super_admin": "Super Admin",
        "role_admin": "Admin",
        "role_manager": "Manager",
        "role_member": "Member",
        "category_bazaar": "Bazaar",
        "category_rent": "Rent & Utility",
        "category_gas": "Gas",
        "category_water": "Current & Water",
        "category_others": "Others",
        "live_dev_mode": "LIVE DEV HUD",
        "lbl_current_file": "Active Frame",
        "lbl_progress": "Integrity Progress",
        "lbl_last_bug": "Last Interceptor Check",
        "btn_print": "Print PDF Receipt",
        "btn_whatsapp": "Share on WhatsApp"
      },
      "bn": {
        "app_title": "স্মার্ট মেস বিডি",
        "version_tag": "v2.6.3 স্ট্যাবল",
        "btn_add_payment": "টাকা জমা করুন",
        "btn_add_invoice": "নতুন ইনভয়েস",
        "btn_add_expense": "খরচ যোগ করুন",
        "btn_add_member": "মেম্বার যোগ করুন",
        "tab_dashboard": "ড্যাশবোর্ড",
        "tab_invoices": "ইনভয়েস সমূহ",
        "tab_receipts": "রিসিট সমূহ",
        "tab_payments": "পেমেন্ট সমূহ",
        "tab_reports": "রিপোর্ট",
        "tab_settings": "সেটিংস",
        "lbl_active_mess": "চলতি মেস",
        "lbl_switch_mess": "মেস পরিবর্তন",
        "lbl_active_role": "পদবী",
        "lbl_currency": "৳",
        "lbl_total_collection": "মোট জমা",
        "lbl_total_bazaar": "মোট বাজার খরচ",
        "lbl_total_utility": "ইউটিলিটি ও বাসা ভাড়া",
        "lbl_total_due": "মোট বকেয়া",
        "lbl_payment_history": "জমা হিসেব বিবরণী",
        "lbl_receipt_history": "রিসিটের ইতিহাস",
        "lbl_due": "বকেয়া",
        "lbl_paid": "পরিশোধিত",
        "lbl_total_payable": "মোট ধার্যকৃত",
        "lbl_amount": "টাকার পরিমাণ",
        "lbl_method": "মাধ্যম",
        "lbl_txnid": "লেনদেন আইডি (TxnID)",
        "lbl_select_member": "মেম্বার নির্বাচন করুন",
        "lbl_select_invoice": "ইনভয়েস নির্বাচন করুন",
        "lbl_date": "তারিখ",
        "lbl_remarks": "বিবরণ ও মন্তব্য",
        "lbl_monthly_summary": "মাসিক আর্থিক প্রতিবেদন",
        "lbl_recent_activity": "মেসের সাম্প্রতিক কার্যক্রম",
        "lbl_expense_by_category": "খাতভিত্তিক খরচের চার্ট",
        "lbl_payment_by_method": "মাধ্যম ভিত্তিক জমার চার্ট",
        "lbl_due_by_member": "মেম্বারভিত্তিক বকেয়া তালিকা",
        "lbl_sub_status": "SaaS সাবস্ক্রিপশন স্ট্যাটাস",
        "lbl_plan": "বর্তমান প্ল্যান",
        "lbl_expiry": "মেয়াদ উত্তীর্ণের তারিখ",
        "lbl_license": "লাইসেন্স অবস্থা",
        "lbl_db_backup": "ডাটাবেস ব্যাকআপ ও পোর্টাবিলিটি",
        "lbl_backup_desc": "সব ডাটা পোর্টবল JSON আকারে সংগ্রহ বা পুনরুদ্ধার করুন",
        "btn_export_json": "ব্যাকআপ ডাউনলোড (JSON)",
        "btn_import_json": "রিস্টোর ব্যাকআপ",
        "msg_backup_copied": "ব্যাকআপ কোড ক্লিপবোর্ডে কপি হয়েছে!",
        "msg_import_success": "মেস ডাটা সম্পূর্ণ পুনরুদ্ধার হয়েছে!",
        "msg_import_error": "ডাটা রিড ব্যর্থ হয়েছে, ব্যাকআপ ফরম্যাট চেক করুন।",
        "msg_save_success": "লেনদেন সফলভাবে লিপিবদ্ধ হয়েছে!",
        "msg_delete_success": "কমান্ড ডেটা চিরতরে মুছে ফেলা হয়েছে!",
        "validation_empty_fields": "সবগুলো বাধ্যতামূলক ঘর পূরণ করুন।",
        "validation_invalid_amount": "টাকার অঙ্ক অবশ্যই পজিটিভ সংখ্যা হতে হবে।",
        "validation_invalid_phone": "অনুগ্রহ করে সঠিক ১১ ডিজিটের বাংলাদেশী মোবাইল নাম্বার দিন।",
        "role_super_admin": "সুপার এডমিন",
        "role_admin": "এডমিন",
        "role_manager": "ম্যানেজার",
        "role_member": "মেম্বার (সাধারণ)",
        "category_bazaar": "বাজার খরচ",
        "category_rent": "বাসা ভাড়া ও বুয়া বিল",
        "category_gas": "গ্যাস বিল",
        "category_water": "বিদ্যুৎ ও পানি বিল",
        "category_others": "অন্যান্য খরচ",
        "live_dev_mode": "লাইভ কোড মনিটর",
        "lbl_current_file": "চলতি ফাইল",
        "lbl_progress": "চলতি অগ্রগতি",
        "lbl_last_bug": "বাগ ইন্টারসেপ্টর চেক",
        "btn_print": "রিসিট প্রিন্ট করুন",
        "btn_whatsapp": "হোয়াটসঅ্যাপে শেয়ার"
      }
    }
    """

    private val EN_STRINGS = mutableMapOf<String, String>()
    private val BN_STRINGS = mutableMapOf<String, String>()

    init {
        try {
            val root = JSONObject(LOCALIZATION_JSON)
            
            val enObj = root.optJSONObject("en")
            if (enObj != null) {
                val keys = enObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    EN_STRINGS[key] = enObj.getString(key)
                }
            }

            val bnObj = root.optJSONObject("bn")
            if (bnObj != null) {
                val keys = bnObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    BN_STRINGS[key] = bnObj.getString(key)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun translate(key: String, lang: Lang): String {
        return if (lang == Lang.BN) {
            BN_STRINGS[key] ?: EN_STRINGS[key] ?: key
        } else {
            EN_STRINGS[key] ?: key
        }
    }
}
