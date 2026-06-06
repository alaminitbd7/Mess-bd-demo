package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.BackupHelper
import com.example.util.LanguageHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MonthlyTrendData(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

class MessViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MessRepository(database.messDao())
    private val sharedPrefs = application.getSharedPreferences("smart_mess_prefs", android.content.Context.MODE_PRIVATE)

    // --- Active ERP Preferences ---
    val currentLanguage = MutableStateFlow(getSavedLanguage())

    private fun getSavedLanguage(): LanguageHelper.Lang {
        val saved = sharedPrefs.getString("selected_language", null)
        if (saved != null) {
            return try {
                LanguageHelper.Lang.valueOf(saved)
            } catch (e: java.lang.Exception) {
                getDefaultLanguageSetting()
            }
        }
        return getDefaultLanguageSetting()
    }

    private fun getDefaultLanguageSetting(): LanguageHelper.Lang {
        return if (LanguageHelper.DEFAULT_UI_LANGUAGE_SETTING == "BN") LanguageHelper.Lang.BN
        else LanguageHelper.Lang.EN
    }
    val activeMess = MutableStateFlow("Mess Alpha")
    val activeRole = MutableStateFlow("Super Admin") // "Super Admin", "Admin", "Manager", "Member"
    val showDevHud = MutableStateFlow(true)

    // --- Search Queries ---
    val paymentQuery = MutableStateFlow("")
    val invoiceQuery = MutableStateFlow("")
    val receiptQuery = MutableStateFlow("")

    // --- SaaS Hidden Architecture ---
    val saasPlan = MutableStateFlow("Pro ERP Plan") // "Free", "Basic", "Pro", "Enterprise"
    val saasLicenseStatus = MutableStateFlow("Active") // "Active", "Expired"
    val saasExpiryDate = MutableStateFlow("2026-12-31")
    val trialDaysRemaining = MutableStateFlow(25)

    // --- UI Navigation Tab ---
    val selectedTab = MutableStateFlow(0) // 0:Dashboard, 1:Invoices, 2:Receipts, 3:Payments, 4:Reports, 5:Settings

    // --- Reactive Data Streams ---
    val members: StateFlow<List<MessMember>> = activeMess
        .flatMapLatest { mess -> repository.getMembers(mess) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = combine(activeMess, paymentQuery) { mess, query ->
        Pair(mess, query)
    }.flatMapLatest { (mess, query) ->
        if (query.isBlank()) repository.getPayments(mess) else repository.searchPayments(mess, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = combine(activeMess, invoiceQuery) { mess, query ->
        Pair(mess, query)
    }.flatMapLatest { (mess, query) ->
        if (query.isBlank()) repository.getInvoices(mess) else repository.searchInvoices(mess, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receipts: StateFlow<List<Receipt>> = combine(activeMess, receiptQuery) { mess, query ->
        Pair(mess, query)
    }.flatMapLatest { (mess, query) ->
        if (query.isBlank()) repository.getReceipts(mess) else repository.searchReceipts(mess, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = activeMess
        .flatMapLatest { mess -> repository.getExpenses(mess) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Metrics ---
    val totalCollection = payments.map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBazaar = expenses.map { list -> list.filter { it.category == "Bazaar" }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalUtility = expenses.map { list -> list.filter { it.category != "Bazaar" }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDues = invoices.map { list -> list.sumOf { it.dueAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyIncome = payments.map { list ->
        val cal = java.util.Calendar.getInstance()
        val currentMonth = cal.get(java.util.Calendar.MONTH)
        val currentYear = cal.get(java.util.Calendar.YEAR)
        list.filter { p ->
            val pCal = java.util.Calendar.getInstance().apply { timeInMillis = p.dateMillis }
            pCal.get(java.util.Calendar.MONTH) == currentMonth && pCal.get(java.util.Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpense = expenses.map { list ->
        val cal = java.util.Calendar.getInstance()
        val currentMonth = cal.get(java.util.Calendar.MONTH)
        val currentYear = cal.get(java.util.Calendar.YEAR)
        list.filter { e ->
            val eCal = java.util.Calendar.getInstance().apply { timeInMillis = e.dateMillis }
            eCal.get(java.util.Calendar.MONTH) == currentMonth && eCal.get(java.util.Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyNetBalance = combine(monthlyIncome, monthlyExpense) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyTrendData: StateFlow<List<MonthlyTrendData>> = combine(payments, expenses) { payList, expList ->
        val result = mutableListOf<MonthlyTrendData>()
        
        // Generate data points for the past 6 calendar months (chronological order)
        for (i in 5 downTo 0) {
            val loopCal = java.util.Calendar.getInstance()
            loopCal.add(java.util.Calendar.MONTH, -i)
            val month = loopCal.get(java.util.Calendar.MONTH)
            val year = loopCal.get(java.util.Calendar.YEAR)
            
            val monthName = when (month) {
                0 -> "Jan"
                1 -> "Feb"
                2 -> "Mar"
                3 -> "Apr"
                4 -> "May"
                5 -> "Jun"
                6 -> "Jul"
                7 -> "Aug"
                8 -> "Sep"
                9 -> "Oct"
                10 -> "Nov"
                11 -> "Dec"
                else -> ""
            }
            
            val incomeSum = payList.filter { p ->
                val pCal = java.util.Calendar.getInstance().apply { timeInMillis = p.dateMillis }
                pCal.get(java.util.Calendar.MONTH) == month && pCal.get(java.util.Calendar.YEAR) == year
            }.sumOf { it.amount }
            
            val expenseSum = expList.filter { e ->
                val eCal = java.util.Calendar.getInstance().apply { timeInMillis = e.dateMillis }
                eCal.get(java.util.Calendar.MONTH) == month && eCal.get(java.util.Calendar.YEAR) == year
            }.sumOf { it.amount }
            
            result.add(MonthlyTrendData(monthName, incomeSum, expenseSum))
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Status Messages ---
    val statusMessage = MutableSharedFlow<String>(replay = 0)

    init {
        // Pre-populate database with beautiful mock values so the user gets instant visualization on clean boot
        viewModelScope.launch {
            repository.getMembers("Mess Alpha").first().let { list ->
                if (list.isEmpty()) {
                    repository.prepopulateDemoData("Mess Alpha")
                    repository.prepopulateDemoData("Mess Beta")
                    repository.prepopulateDemoData("Mess Gamma")
                }
            }
        }
    }

    // --- ERP Setters ---
    fun setTab(index: Int) {
        selectedTab.value = index
    }

    fun setLanguage(lang: LanguageHelper.Lang) {
        currentLanguage.value = lang
        sharedPrefs.edit().putString("selected_language", lang.name).apply()
    }

    fun setMess(mess: String) {
        activeMess.value = mess
    }

    fun setRole(role: String) {
        activeRole.value = role
    }

    // --- Record Forms ---
    fun addPayment(amount: Double, method: String, transactionId: String, memberId: String, memberName: String, invoiceId: String?) {
        viewModelScope.launch {
            try {
                repository.recordPayment(amount, method, transactionId, memberId, memberName, activeMess.value, invoiceId)
                statusMessage.emit("Payment of ৳${amount} recorded! Generated companion receipt.")
            } catch (e: Exception) {
                statusMessage.emit("Error recording payment: ${e.message}")
            }
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            try {
                repository.deletePayment(payment)
                statusMessage.emit("Payment transaction removed. Restored invoice due state.")
            } catch (e: Exception) {
                statusMessage.emit("Delete failed: ${e.message}")
            }
        }
    }

    fun addInvoice(invoiceNumber: String, billingMonth: String, amount: Double, memberId: String, memberName: String) {
        viewModelScope.launch {
            try {
                val newInvoice = Invoice(
                    id = java.util.UUID.randomUUID().toString(),
                    invoiceNumber = invoiceNumber.ifEmpty { "INV-${System.currentTimeMillis() / 1000}" },
                    billingMonth = billingMonth,
                    totalPayable = amount,
                    paidAmount = 0.0,
                    dueAmount = amount,
                    memberId = memberId,
                    memberName = memberName,
                    messId = activeMess.value,
                    dateMillis = System.currentTimeMillis()
                )
                repository.insertInvoice(newInvoice)
                statusMessage.emit("Generated new Invoice ${newInvoice.invoiceNumber} for ৳${amount}")
            } catch (e: Exception) {
                statusMessage.emit("Failed generating invoice: ${e.message}")
            }
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteInvoice(invoiceId)
                statusMessage.emit("Invoice permanently removed.")
            } catch (e: Exception) {
                statusMessage.emit("Failed to delete invoice: ${e.message}")
            }
        }
    }

    fun addExpense(category: String, amount: Double, description: String, spenderName: String) {
        viewModelScope.launch {
            try {
                val newExp = Expense(
                    id = java.util.UUID.randomUUID().toString(),
                    category = category,
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    description = description,
                    spenderName = spenderName,
                    messId = activeMess.value
                )
                repository.insertExpense(newExp)
                statusMessage.emit("Logged Expense of ৳${amount} under category $category")
            } catch (e: Exception) {
                statusMessage.emit("Failed to log expense: ${e.message}")
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(id)
                statusMessage.emit("Expense transaction purged from ledger.")
            } catch (e: Exception) {
                statusMessage.emit("Expense purge failed: ${e.message}")
            }
        }
    }

    fun addMember(name: String, phone: String, email: String, role: String) {
        viewModelScope.launch {
            try {
                val member = MessMember(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    phone = phone,
                    email = email,
                    role = role,
                    messId = activeMess.value
                )
                repository.insertMember(member)
                statusMessage.emit("Added member $name to ${activeMess.value}")
            } catch (e: Exception) {
                statusMessage.emit("Member registration failed: ${e.message}")
            }
        }
    }

    fun deleteMember(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteMember(id)
                statusMessage.emit("Member purged.")
            } catch (e: Exception) {
                statusMessage.emit("Failed to remove member: ${e.message}")
            }
        }
    }

    // --- JSON Backup / Portability Actions ---
    fun exportBackup(): String {
        val currentMembers = members.value
        val currentPayments = payments.value
        val currentInvoices = invoices.value
        val currentReceipts = receipts.value
        val currentExpenses = expenses.value

        return BackupHelper.exportToJson(
            currentMembers,
            currentPayments,
            currentInvoices,
            currentReceipts,
            currentExpenses
        )
    }

    fun importBackup(jsonStr: String): Boolean {
        val wrapper = BackupHelper.importFromJson(jsonStr) ?: return false
        viewModelScope.launch {
            try {
                // Clear all current data
                repository.clearAllData()

                // Re-insert imported items
                wrapper.members.forEach { repository.insertMember(it) }
                wrapper.invoices.forEach { repository.insertInvoice(it) }
                wrapper.expenses.forEach { repository.insertExpense(it) }
                
                // Manual database commands for direct table imports
                // For tables not exposed by repository, use direct inserts inside DAOs if needed,
                // but since repository supports insertPayment and insertReceipt, we insert those!
                wrapper.payments.forEach { database.messDao().insertPayment(it) }
                wrapper.receipts.forEach { database.messDao().insertReceipt(it) }

                statusMessage.emit("Database restored successfully!")
            } catch (e: Exception) {
                statusMessage.emit("Restore parse failed: ${e.message}")
            }
        }
        return true
    }
}
