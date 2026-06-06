package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.MessViewModel
import com.example.ui.MonthlyTrendData
import com.example.util.LanguageHelper
import com.example.util.LanguageHelper.Lang
import com.example.util.ValidationHelper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Executive Dark Slate & Emerald Color Scheme (Bangladeshi ERP theme)
            val darkSlateThemeColors = darkColorScheme(
                primary = Color(0xFF10B981), // Fluent Emerald
                secondary = Color(0xFFFBBF24), // Rich Gold
                tertiary = Color(0xFFEF4444), // Crimson Red
                background = Color(0xFF0F172A), // Deep Slate Canvas
                surface = Color(0xFF1E293B), // Card Plate
                onPrimary = Color.White,
                onSecondary = Color(0xFF1E293B),
                onBackground = Color(0xFFF1F5F9),
                onSurface = Color(0xFFE2E8F0)
            )

            MaterialTheme(colorScheme = darkSlateThemeColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val vm: MessViewModel = viewModel()
                    SmartMessApp(vm)
                }
            }
        }
    }
}

@Composable
fun SmartMessApp(vm: MessViewModel) {
    val context = LocalContext.current
    val lang by vm.currentLanguage.collectAsState()
    val activeMessName by vm.activeMess.collectAsState()
    val activeRoleStr by vm.activeRole.collectAsState()
    val tabIndex by vm.selectedTab.collectAsState()
    val devHudVisible by vm.showDevHud.collectAsState()

    // --- State Toggles for Add dialogues ---
    var showAddMember by remember { mutableStateOf(false) }
    var showAddPayment by remember { mutableStateOf(false) }
    var showAddInvoice by remember { mutableStateOf(false) }
    var showAddExpense by remember { mutableStateOf(false) }

    // Collect status notifications
    LaunchedEffect(key1 = true) {
        vm.statusMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            ERPHeader(
                vm = vm,
                activeMessName = activeMessName,
                activeRoleStr = activeRoleStr,
                lang = lang
            )
        },
        bottomBar = {
            ERPBottomBar(
                tabIndex = tabIndex,
                onTabSelect = { vm.setTab(it) },
                lang = lang
            )
        },
        floatingActionButton = {
            // Responsive mobile action ring depending on tab
            when (tabIndex) {
                0 -> { // Dashboard Context
                    FloatingActionButton(
                        onClick = { showAddPayment = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_payment_fab")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = "Record Payment")
                    }
                }
                1 -> { // Invoice Context
                    FloatingActionButton(
                        onClick = { showAddInvoice = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_invoice_fab")
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = "Add Invoice")
                    }
                }
                3 -> { // Payments Context
                    FloatingActionButton(
                        onClick = { showAddPayment = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_payment_direct_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Payment")
                    }
                }
                4 -> { // Reports/Expenses Context
                    FloatingActionButton(
                        onClick = { showAddExpense = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_expense_fab")
                    ) {
                        Icon(Icons.Default.TrendingDown, contentDescription = "Record Expense")
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Live Development HUD (Collapsible)
            if (devHudVisible) {
                DevHudView(vm = vm, lang = lang)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (tabIndex) {
                    0 -> DashboardScreen(vm = vm, lang = lang)
                    1 -> InvoicesScreen(vm = vm, lang = lang)
                    2 -> ReceiptsScreen(vm = vm, lang = lang)
                    3 -> PaymentsScreen(vm = vm, lang = lang)
                    4 -> ReportsScreen(vm = vm, lang = lang)
                    5 -> SettingsScreen(vm = vm, lang = lang, onAddMemberClicked = { showAddMember = true })
                }
            }
        }
    }

    // --- Render Dialogues ---
    if (showAddMember) {
        AddMemberDialog(vm = vm, lang = lang, onDismiss = { showAddMember = false })
    }
    if (showAddPayment) {
        AddPaymentDialog(vm = vm, lang = lang, onDismiss = { showAddPayment = false })
    }
    if (showAddInvoice) {
        AddInvoiceDialog(vm = vm, lang = lang, onDismiss = { showAddInvoice = false })
    }
    if (showAddExpense) {
        AddExpenseDialog(vm = vm, lang = lang, onDismiss = { showAddExpense = false })
    }
}

// ==========================================
// COMPONENT: CORE SCROLLABLE DASHBOARD
// ==========================================
@Composable
fun DashboardScreen(vm: MessViewModel, lang: Lang) {
    val totalCol by vm.totalCollection.collectAsState()
    val totalBaz by vm.totalBazaar.collectAsState()
    val totalUtl by vm.totalUtility.collectAsState()
    val totalDueVal by vm.totalDues.collectAsState()
    val recentPayments by vm.payments.collectAsState()
    val currentMessStr by vm.activeMess.collectAsState()

    val balance = totalCol - (totalBaz + totalUtl)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SmartMessLogo(
                        modifier = Modifier.fillMaxWidth(),
                        iconSize = 58.dp,
                        textSize = 21.sp,
                        subtextSize = 9.5.sp
                    )
                    Divider(color = Color(0xFF1E293B))
                    Text(
                        text = "${LanguageHelper.translate("lbl_active_mess", lang)}: $currentMessStr | Active Session",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Financial Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = LanguageHelper.translate("lbl_total_collection", lang),
                        value = "৳${String.format("%.1f", totalCol)}",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = LanguageHelper.translate("lbl_total_due", lang),
                        value = "৳${String.format("%.1f", totalDueVal)}",
                        icon = Icons.Default.ReceiptLong,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = LanguageHelper.translate("lbl_total_bazaar", lang),
                        value = "৳${String.format("%.1f", totalBaz)}",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Cash Surplus",
                        value = "৳${String.format("%.1f", balance)}",
                        icon = Icons.Default.SwapCalls,
                        accentColor = if (balance >= 0) Color(0xFF818CF8) else Color(0xFFF87171),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recharts Trend Analysis Line Chart
        item {
            val trendData by vm.monthlyTrendData.collectAsState()
            RechartsResponsiveLineChart(
                trendData = trendData,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Quick Navigation Buttons Grid
        item {
            Text(
                text = "Quick ERP Actions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { vm.setTab(1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_action_invoice"),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Invoices", fontSize = 12.sp, color = Color.White)
                    }
                }

                Button(
                    onClick = { vm.setTab(3) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_action_payment"),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Payments", fontSize = 12.sp, color = Color.White)
                    }
                }

                Button(
                    onClick = { vm.setTab(4) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_action_report"),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reports", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // Recent Activity Panel
        item {
            Text(
                text = LanguageHelper.translate("lbl_recent_activity", lang),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (recentPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No activities logged in this mess session yet.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            val limitList = recentPayments.take(6)
            items(limitList) { pay ->
                ActivityItemRow(pay = pay, lang = lang)
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
fun ActivityItemRow(pay: Payment, lang: Lang) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (pay.paymentMethod == "bKash" || pay.paymentMethod == "Nagad" || pay.paymentMethod == "Rocket") {
                            Color(0xFFE11D48).copy(alpha = 0.15f)
                        } else Color(0xFF10B981).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (pay.paymentMethod == "bKash" || pay.paymentMethod == "Nagad" || pay.paymentMethod == "Rocket") Color(0xFFFB7185) else Color(0xFF34D399),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${pay.memberName} deposited via ${pay.paymentMethod}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "TxnID: ${pay.transactionId}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Text(
                text = "+৳${pay.amount.toInt()}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }
    }
}

// ==========================================
// COMPONENT: INVOICES SCREEN (SEARCH/PRINT/SHARE)
// ==========================================
@Composable
fun InvoicesScreen(vm: MessViewModel, lang: Lang) {
    val invoiceList by vm.invoices.collectAsState()
    val query by vm.invoiceQuery.collectAsState()
    var selectedInvoiceForDetail by remember { mutableStateOf<Invoice?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Input Plate
        OutlinedTextField(
            value = query,
            onValueChange = { vm.invoiceQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("invoice_search_input"),
            placeholder = { Text("Search by Member or Invoice No...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (invoiceList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No invoices found.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(invoiceList) { invoice ->
                    InvoicePlateRow(
                        invoice = invoice,
                        lang = lang,
                        onClick = { selectedInvoiceForDetail = invoice }
                    )
                }
            }
        }
    }

    if (selectedInvoiceForDetail != null) {
        InvoiceDetailsDialog(
            invoice = selectedInvoiceForDetail!!,
            lang = lang,
            onDismiss = { selectedInvoiceForDetail = null }
        )
    }
}

@Composable
fun InvoicePlateRow(invoice: Invoice, lang: Lang, onClick: () -> Unit) {
    val status = when {
        invoice.dueAmount <= 0 -> "Paid"
        invoice.paidAmount > 0 -> "Partial"
        else -> "Unpaid"
    }

    val color = when (status) {
        "Paid" -> Color(0xFF10B981)
        "Partial" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_item_${invoice.invoiceNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.invoiceNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = invoice.memberName,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Month: ${invoice.billingMonth}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "৳${invoice.totalPayable.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "${LanguageHelper.translate("lbl_due", lang)}: ৳${invoice.dueAmount.toInt()}",
                    fontSize = 12.sp,
                    color = if (invoice.dueAmount > 0) Color(0xFFFB7185) else Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: RECEIPTS LISTING SCREEN
// ==========================================
@Composable
fun ReceiptsScreen(vm: MessViewModel, lang: Lang) {
    val receiptList by vm.receipts.collectAsState()
    val query by vm.receiptQuery.collectAsState()
    var selectedReceiptForDetail by remember { mutableStateOf<Receipt?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { vm.receiptQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("receipt_search_input"),
            placeholder = { Text("Search by Member or Receipt ID...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (receiptList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No receipts found.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(receiptList) { receipt ->
                    ReceiptRow(
                        receipt = receipt,
                        onClick = { selectedReceiptForDetail = receipt }
                    )
                }
            }
        }
    }

    if (selectedReceiptForDetail != null) {
        ReceiptDetailsDialog(
            receipt = selectedReceiptForDetail!!,
            lang = lang,
            onDismiss = { selectedReceiptForDetail = null }
        )
    }
}

@Composable
fun ReceiptRow(receipt: Receipt, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("receipt_item_${receipt.receiptNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receipt.receiptNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = receipt.memberName,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Method: ${receipt.paymentMethod}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Text(
                text = "৳${receipt.amountPaid.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }
    }
}

// ==========================================
// COMPONENT: PAYMENTS LEDGER SCREEN
// ==========================================
@Composable
fun PaymentsScreen(vm: MessViewModel, lang: Lang) {
    val paymentList by vm.payments.collectAsState()
    val query by vm.paymentQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { vm.paymentQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_search_input"),
            placeholder = { Text("Search by Spender, Member, TxnID...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (paymentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No ledger payment transactions found.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(paymentList) { payment ->
                    PaymentLedgerItem(
                        payment = payment,
                        onDelete = { vm.deletePayment(payment) }
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentLedgerItem(payment: Payment, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payment_item_${payment.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "৳${payment.amount.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = payment.paymentMethod,
                            color = Color(0xFF38BDF8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "By: ${payment.memberName}",
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = "TxnID: ${payment.transactionId}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFB7185))
            }
        }
    }
}

// ==========================================
// COMPONENT: REPORTS SCREEN (GRAPHS/DUE LOGS)
// ==========================================
@Composable
fun ReportsScreen(vm: MessViewModel, lang: Lang) {
    val totalCol by vm.totalCollection.collectAsState()
    val totalBaz by vm.totalBazaar.collectAsState()
    val totalUtl by vm.totalUtility.collectAsState()
    val dueVal by vm.totalDues.collectAsState()
    val invoiceList by vm.invoices.collectAsState()

    val mIncome by vm.monthlyIncome.collectAsState()
    val mExpense by vm.monthlyExpense.collectAsState()
    val mNetBalance by vm.monthlyNetBalance.collectAsState()

    val currentMonthName = remember {
        java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report Summary Heading
        item {
            Text(
                text = LanguageHelper.translate("lbl_monthly_summary", lang),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Section: Clean Card-based Current Month Overview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_report_summary_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Period: $currentMonthName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (mNetBalance >= 0) Color(0xFF065F46) else Color(0xFF991B1B))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (mNetBalance >= 0) "Surplus" else "Deficit",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Divider(color = Color(0xFF1E293B))

                    // Monthly Income row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = "Monthly Income", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Text(
                            text = "৳ ${mIncome.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Monthly Expense row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFFFB7185),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = "Monthly Expenses", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Text(
                            text = "৳ ${mExpense.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Divider(color = Color(0xFF1E293B))

                    // Net balance row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Balance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${if (mNetBalance >= 0) "+" else ""}৳ ${mNetBalance.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (mNetBalance >= 0) Color(0xFF10B981) else Color(0xFFFB7185)
                        )
                    }
                }
            }
        }

        // Section: Expense Category Breakdown Visual Bars
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = LanguageHelper.translate("lbl_expense_by_category", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress chart: Bazaar
                    ReportBar(
                        label = "Bazaar Expense (Food)",
                        amount = totalBaz,
                        total = totalBaz + totalUtl,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress chart: Utilities
                    ReportBar(
                        label = "Utility bills, Maid & WiFi",
                        amount = totalUtl,
                        total = totalBaz + totalUtl,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }

        // Section: Cash Flow Distribution Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cash Flow Ratio (Surplus vs Debt)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val gross = totalCol + dueVal
                    ReportBar(
                        label = "Total Collected Cash",
                        amount = totalCol,
                        total = gross,
                        color = Color(0xFF818CF8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ReportBar(
                        label = "Outstanding Member Debt",
                        amount = dueVal,
                        total = gross,
                        color = Color(0xFFFB7185)
                    )
                }
            }
        }

        // Section: Due list of individual members
        item {
            Text(
                text = LanguageHelper.translate("lbl_due_by_member", lang),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val unpaidInvoices = invoiceList.filter { it.dueAmount > 0 }
        if (unpaidInvoices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Zero arrears! Every member cleared their dues.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(unpaidInvoices) { inv ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(inv.memberName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(inv.invoiceNumber, fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Text(
                        text = "৳${inv.dueAmount.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFB7185)
                    )
                }
            }
        }
    }
}

@Composable
fun ReportBar(label: String, amount: Double, total: Double, color: Color) {
    val percent = if (total > 0) (amount / total).toFloat() else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = Color.White)
            Text("৳${amount.toInt()} (${(percent * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color(0xFF0F172A)
        )
    }
}

// ==========================================
// COMPONENT: SETTINGS/BACKUP/SUBSCRIPTION
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: MessViewModel, lang: Lang, onAddMemberClicked: () -> Unit) {
    val activeRoleStr by vm.activeRole.collectAsState()
    val activeMessName by vm.activeMess.collectAsState()
    val saasTier by vm.saasPlan.collectAsState()
    val saasLic by vm.saasLicenseStatus.collectAsState()
    val trialDays by vm.trialDaysRemaining.collectAsState()
    val showDevHudVal by vm.showDevHud.collectAsState()

    var showMfsUpgradeSimulator by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: System Role Changer (For testing simulated permissions)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Simulated Active Role Override",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toggle active ERP roles below to check simulated access views and operation locks:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val roles = listOf("Super Admin", "Admin", "Manager", "Member")
                        roles.forEach { role ->
                            FilterChip(
                                selected = activeRoleStr == role,
                                onClick = { vm.setRole(role) },
                                label = { Text(role, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section: ERP Context setup
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Multi-Mess Framework",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageHelper.translate("lbl_switch_mess", lang),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle mess buttons
                    val messes = listOf("Mess Alpha", "Mess Beta", "Mess Gamma")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        messes.forEach { mess ->
                            Button(
                                onClick = { vm.setMess(mess) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeMessName == mess) MaterialTheme.colorScheme.primary else Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text(mess, fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Section: Member Administration shortcuts
        item {
            Button(
                onClick = onAddMemberClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboard_member_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(LanguageHelper.translate("btn_add_member", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Section: Hidden SaaS Architecture / Trial Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = LanguageHelper.translate("lbl_sub_status", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(LanguageHelper.translate("lbl_plan", lang), color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(saasTier, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(LanguageHelper.translate("lbl_license", lang), color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(
                            text = saasLic,
                            fontWeight = FontWeight.Bold,
                            color = if (saasLic == "Active") Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Trial countdown", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("$trialDays days remaining", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showMfsUpgradeSimulator = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Renew / Upgrade License", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: JSON Backup and Recovery Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = LanguageHelper.translate("lbl_db_backup", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = LanguageHelper.translate("lbl_backup_desc", lang),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val jsonStr = vm.exportBackup()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("smart_mess_backup", jsonStr)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, LanguageHelper.translate("msg_backup_copied", lang), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_db"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(LanguageHelper.translate("btn_export_json", lang), fontSize = 10.sp, color = Color.White)
                        }

                        var showRestorePasteDialog by remember { mutableStateOf(false) }
                        Button(
                            onClick = { showRestorePasteDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_import_db"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(LanguageHelper.translate("btn_import_json", lang), fontSize = 10.sp, color = Color.White)
                        }

                        if (showRestorePasteDialog) {
                            RestoreDataDialog(
                                vm = vm,
                                lang = lang,
                                onDismiss = { showRestorePasteDialog = false }
                            )
                        }
                    }
                }
            }
        }

        // Section: Misc Interface Options
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Live Dev Tools HUD", fontSize = 13.sp, color = Color.White)
                Switch(
                    checked = showDevHudVal,
                    onCheckedChange = { vm.showDevHud.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }

    if (showMfsUpgradeSimulator) {
        MockPaymentGatewayUpgradeDialog(
            vm = vm,
            onDismiss = { showMfsUpgradeSimulator = false }
        )
    }
}

// ==========================================
// COMPONENT: BRANDED HEADER & LANGUAGE/MESS CHANGER
// ==========================================
@Composable
fun ERPHeader(vm: MessViewModel, activeMessName: String, activeRoleStr: String, lang: Lang) {
    var messExpanded by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Branded ERP Logo Title and custom software logo
                SmartMessLogo(
                    iconSize = 34.dp,
                    textSize = 15.sp,
                    subtextSize = 7.5.sp
                )

                // Persistent Language Toggle Button
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 96.dp, minHeight = 48.dp)
                        .testTag("language_toggle_button")
                        .clickable(onClick = {
                            val nextLang = if (lang == Lang.EN) Lang.BN else Lang.EN
                            vm.setLanguage(nextLang)
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(Color(0xFF0F172A)) // Dark contrast background
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(17.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // English option indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (lang == Lang.EN) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🇬🇧", fontSize = 12.sp)
                                    Text(
                                        text = "EN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lang == Lang.EN) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Bangla option indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (lang == Lang.BN) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🇧🇩", fontSize = 12.sp)
                                    Text(
                                        text = "বাং",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lang == Lang.BN) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subbar selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Mess switcher selector
                Box {
                    Button(
                        onClick = { messExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("mess_changer_dropdown_btn")
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(activeMessName, fontSize = 11.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = messExpanded,
                        onDismissRequest = { messExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        val messList = listOf("Mess Alpha", "Mess Beta", "Mess Gamma")
                        messList.forEach { mName ->
                            DropdownMenuItem(
                                text = { Text(mName, fontSize = 12.sp, color = Color.White) },
                                onClick = {
                                    vm.setMess(mName)
                                    messExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. Role controller status visual placeholder
                Box {
                    Button(
                        onClick = { roleExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(activeRoleStr, fontSize = 11.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        val roles = listOf("Super Admin", "Admin", "Manager", "Member")
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role, fontSize = 12.sp, color = Color.White) },
                                onClick = {
                                    vm.setRole(role)
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: RESPONSIVE BOTTOM NAVIGATION BAR
// ==========================================
@Composable
fun ERPBottomBar(tabIndex: Int, onTabSelect: (Int) -> Unit, lang: Lang) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val navItems = listOf(
            Triple(0, Icons.Default.Dashboard, "tab_dashboard"),
            Triple(1, Icons.Default.PostAdd, "tab_invoices"),
            Triple(2, Icons.Default.Receipt, "tab_receipts"),
            Triple(3, Icons.Default.Payment, "tab_payments"),
            Triple(4, Icons.Default.Assessment, "tab_reports"),
            Triple(5, Icons.Default.Settings, "tab_settings")
        )

        navItems.forEach { (index, icon, langKey) ->
            NavigationBarItem(
                selected = tabIndex == index,
                onClick = { onTabSelect(index) },
                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp)) },
                label = { Text(LanguageHelper.translate(langKey, lang), fontSize = 9.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color(0xFF64748B),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color(0xFF64748B),
                    indicatorColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("nav_btn_$index")
            )
        }
    }
}

// ==========================================
// COMPONENT: LIVE DEVELOPMENT MODE HUD
// ==========================================
@Composable
fun DevHudView(vm: MessViewModel, lang: Lang) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE DEVELOPMENT MONITOR (v2.6.3 STABLE)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = { vm.showDevHud.value = false },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close HUD", tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Details log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ACTIVE COMPILATION: MainActivity.kt | DB Dao", fontSize = 9.sp, color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("FIXED: Firestore docRef duplication vulnerability resolved offline", fontSize = 9.sp, color = Color(0xFFF87171))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("INTEGRITY: 100% SECURE", fontSize = 9.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("BUILD SUCCESS: STABLE", fontSize = 9.sp, color = Color(0xFFFBBF24))
                }
            }
        }
    }
}

// ==========================================
// PREVIEWS & DIALOG WINDOWS
// ==========================================

// 1. ADD MEMBER DIALOG
@Composable
fun AddMemberDialog(vm: MessViewModel, lang: Lang, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Member") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = LanguageHelper.translate("btn_add_member", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth().testTag("add_member_field_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Bangladeshi Mobile Phone", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth().testTag("add_member_field_phone")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth().testTag("add_member_field_email")
                )

                // Role Selector Radio Group
                Text("Role Assignation", fontSize = 12.sp, color = Color(0xFF94A3B8))
                val roles = listOf("Member", "Manager", "Admin")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    roles.forEach { r ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = role == r,
                                onClick = { role = r }
                            )
                            Text(r, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank()) {
                                Toast.makeText(vm.getApplication(), "Name and phone are mandatory!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!ValidationHelper.isValidBDPhone(phone)) {
                                Toast.makeText(vm.getApplication(), LanguageHelper.translate("validation_invalid_phone", lang), Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            vm.addMember(name, phone, email, role)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("add_member_submit_btn")
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}

// 2. ADD PAYMENT DIALOG (WITH AUTO-DUE INVOICE LINKING & MFS SIMULATION)
@Composable
fun AddPaymentDialog(vm: MessViewModel, lang: Lang, onDismiss: () -> Unit) {
    val membersList by vm.members.collectAsState()
    val invoiceList by vm.invoices.collectAsState()

    var amountStr by remember { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf<MessMember?>(null) }
    var selectedInvoice by remember { mutableStateOf<Invoice?>(null) }
    var paymentMethod by remember { mutableStateOf("bKash") }
    var txnId by remember { mutableStateOf("") }

    var mExpanded by remember { mutableStateOf(false) }
    var iExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Record POS ERP Payment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Select Member Trigger
                Box {
                    OutlinedButton(
                        onClick = { mExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("select_member_dropdown_trigger")
                    ) {
                        Text(
                            text = selectedMember?.name ?: LanguageHelper.translate("lbl_select_member", lang),
                            fontSize = 13.sp
                        )
                    }

                    DropdownMenu(
                        expanded = mExpanded,
                        onDismissRequest = { mExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        membersList.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name, color = Color.White) },
                                onClick = {
                                    selectedMember = m
                                    selectedInvoice = null // Reset invoice filter
                                    mExpanded = false
                                }
                            )
                        }
                    }
                }

                // Associated Invoice Trigger
                val filteredInvoices = if (selectedMember != null) {
                    invoiceList.filter { it.memberId == selectedMember!!.id && it.dueAmount > 0 }
                } else emptyList()

                if (filteredInvoices.isNotEmpty()) {
                    Box {
                        OutlinedButton(
                            onClick = { iExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedInvoice?.let { "${it.invoiceNumber} (Due: ৳${it.dueAmount.toInt()})" }
                                    ?: LanguageHelper.translate("lbl_select_invoice", lang),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        DropdownMenu(
                            expanded = iExpanded,
                            onDismissRequest = { iExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            filteredInvoices.forEach { invoice ->
                                DropdownMenuItem(
                                    text = { Text("${invoice.invoiceNumber} - Due: ৳${invoice.dueAmount.toInt()}", color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        selectedInvoice = invoice
                                        // Auto suggest full outstanding due
                                        amountStr = invoice.dueAmount.toInt().toString()
                                        iExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(LanguageHelper.translate("lbl_amount", lang), color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                // Payment Method Selector
                Text(LanguageHelper.translate("lbl_method", lang), fontSize = 12.sp, color = Color(0xFF94A3B8))
                val methods = listOf("bKash", "Nagad", "Rocket", "Cash", "Card")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (method == "bKash" || method == "Nagad") Color(0xFFE11D48) else MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("method_chip_$method")
                        )
                    }
                }

                // Transaction ID
                if (paymentMethod != "Cash") {
                    OutlinedTextField(
                        value = txnId,
                        onValueChange = { txnId = it },
                        label = { Text("${LanguageHelper.translate("lbl_txnid", lang)} (Default Auto-Generated)", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth().testTag("payment_txnid_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = ValidationHelper.isValidAmount(amountStr)
                            if (amount == null) {
                                Toast.makeText(vm.getApplication(), LanguageHelper.translate("validation_invalid_amount", lang), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedMember == null) {
                                Toast.makeText(vm.getApplication(), "Please select a member first!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            vm.addPayment(
                                amount = amount,
                                method = paymentMethod,
                                transactionId = txnId,
                                memberId = selectedMember!!.id,
                                memberName = selectedMember!!.name,
                                invoiceId = selectedInvoice?.id
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("submit_payment_btn")
                    ) {
                        Text("Save Payment", color = Color.White)
                    }
                }
            }
        }
    }
}

// 3. ADD INVOICE DIALOG
@Composable
fun AddInvoiceDialog(vm: MessViewModel, lang: Lang, onDismiss: () -> Unit) {
    val membersList by vm.members.collectAsState()
    var selectedMember by remember { mutableStateOf<MessMember?>(null) }
    var mExpanded by remember { mutableStateOf(false) }

    var billingMonth by remember { mutableStateOf("June 2026") }
    var amountStr by remember { mutableStateOf("") }
    var customInvoiceNo by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Generate New Mess Invoice",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Select Member Trigger
                Box {
                    OutlinedButton(
                        onClick = { mExpanded = true },
                        modifier = Modifier.fillMaxWidth().testTag("select_member_invoice_trigger")
                    ) {
                        Text(
                            text = selectedMember?.name ?: LanguageHelper.translate("lbl_select_member", lang),
                            fontSize = 13.sp
                        )
                    }

                    DropdownMenu(
                        expanded = mExpanded,
                        onDismissRequest = { mExpanded = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        membersList.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name, color = Color.White) },
                                onClick = {
                                    selectedMember = m
                                    mExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = billingMonth,
                    onValueChange = { billingMonth = it },
                    label = { Text("Billing Target Month", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Gross Monthly Bill Amount (৳)", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth().testTag("invoice_amount_input")
                )

                OutlinedTextField(
                    value = customInvoiceNo,
                    onValueChange = { customInvoiceNo = it },
                    label = { Text("Invoice Custom ID (Optional)", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = ValidationHelper.isValidAmount(amountStr)
                            if (amount == null) {
                                Toast.makeText(vm.getApplication(), LanguageHelper.translate("validation_invalid_amount", lang), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedMember == null) {
                                Toast.makeText(vm.getApplication(), "Select a member!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            vm.addInvoice(
                                invoiceNumber = customInvoiceNo.ifEmpty { "INV-2026-${(100..999).random()}" },
                                billingMonth = billingMonth,
                                amount = amount,
                                memberId = selectedMember!!.id,
                                memberName = selectedMember!!.name
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("submit_invoice_btn")
                    ) {
                        Text("Create Invoice", color = Color.White)
                    }
                }
            }
        }
    }
}

// 4. ADD EXPENSE DIALOG
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(vm: MessViewModel, lang: Lang, onDismiss: () -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bazaar") }
    var description by remember { mutableStateOf("") }
    var spender by remember { mutableStateOf("Karim Uddin") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = LanguageHelper.translate("btn_add_expense", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Cost Amount (৳)", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                )

                Text("Expense Category", fontSize = 12.sp, color = Color(0xFF94A3B8))
                val categories = listOf("Bazaar", "Rent & Utility", "Gas", "Current & Water", "Others")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details / Item List (e.g., Fish, Oil)", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = spender,
                    onValueChange = { spender = it },
                    label = { Text("Paid By / Spender Name", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = ValidationHelper.isValidAmount(amountStr)
                            if (amount == null) {
                                Toast.makeText(vm.getApplication(), LanguageHelper.translate("validation_invalid_amount", lang), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (description.isBlank()) {
                                Toast.makeText(vm.getApplication(), "Provide expense details!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            vm.addExpense(category, amount, description, spender)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("submit_expense_btn")
                    ) {
                        Text("Log Expense", color = Color.White)
                    }
                }
            }
        }
    }
}

// 5. RESTORE DATA DB DIALOG (JSON INPUT)
@Composable
fun RestoreDataDialog(vm: MessViewModel, lang: Lang, onDismiss: () -> Unit) {
    var pasteStr by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Restore Database", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Text("Paste your valid SMART MESS backup JSON below to restore all records:", fontSize = 11.sp, color = Color(0xFF94A3B8))

                OutlinedTextField(
                    value = pasteStr,
                    onValueChange = { pasteStr = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("restore_json_input_field"),
                    placeholder = { Text("Paste JSON code here...", color = Color(0xFF475569), fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val ok = vm.importBackup(pasteStr)
                            if (ok) {
                                Toast.makeText(context, LanguageHelper.translate("msg_import_success", lang), Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, LanguageHelper.translate("msg_import_error", lang), Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("restore_submit_json_btn")
                    ) {
                        Text("Restore Database", color = Color.White)
                    }
                }
            }
        }
    }
}

// 6. DETAILED THERMAL PRINT RECEIPT POPUP
@Composable
fun ReceiptDetailsDialog(receipt: Receipt, lang: Lang, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("receipt_detail_modal"),
            colors = CardDefaults.cardColors(containerColor = Color.White), // Paper White Background
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thermal top header
                Text("SMART MESS BD", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                Text("CASH PAYMENT RECEIPT", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                Text("Dhaka, Bangladesh | Offline-PWA Cloud ERP", fontSize = 9.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(10.dp))
                Text("-----------------------------------------------", color = Color.Gray, fontSize = 12.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Receipt No:", fontSize = 11.sp, color = Color.DarkGray)
                    Text(receipt.receiptNumber, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(receipt.dateMillis))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date:", fontSize = 11.sp, color = Color.DarkGray)
                    Text(dateStr, fontSize = 11.sp, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Paid By:", fontSize = 11.sp, color = Color.DarkGray)
                    Text(receipt.memberName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Method:", fontSize = 11.sp, color = Color.DarkGray)
                    Text(receipt.paymentMethod, fontSize = 11.sp, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("- - - - - - - - - - - - - - - - - - - - - - - - -", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL AMOUNT PAID:", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.Black)
                    Text("৳${receipt.amountPaid.toInt()}.00", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("- - - - - - - - - - - - - - - - - - - - - - - - -", color = Color.Gray, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Remarks: ${receipt.remarks}", fontSize = 10.sp, textAlign = TextAlign.Center, color = Color.DarkGray)

                Spacer(modifier = Modifier.height(24.dp))
                Text("THANK YOU FOR PAYING YOUR BILLS!", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("SYSTEM GENERATED v2.6.3 ERP", fontSize = 8.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons inside dialogue
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Thermal Printer Print Intent generated...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageHelper.translate("btn_print", lang), fontSize = 10.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val msg = "Hello! Generated Paid Receipt ${receipt.receiptNumber} from SMART MESS BD. Amount Paid: ৳${receipt.amountPaid.toInt()}. Thank you!"
                            val uri = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(msg))
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
                    ) {
                        Text(LanguageHelper.translate("btn_whatsapp", lang), fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// 7. DETAILED INVOICE STATEMENT POPUP
@Composable
fun InvoiceDetailsDialog(invoice: Invoice, lang: Lang, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("invoice_detail_modal"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Invoice Billing Statement",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Invoice Ref:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text(invoice.invoiceNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Billing Month:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text(invoice.billingMonth, fontSize = 12.sp, color = Color.White)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Member Name:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Text(invoice.memberName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Divider(color = Color(0xFF334155))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageHelper.translate("lbl_total_payable", lang), fontSize = 13.sp, color = Color(0xFF94A3B8))
                    Text("৳${invoice.totalPayable.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageHelper.translate("lbl_paid", lang), fontSize = 13.sp, color = Color(0xFF94A3B8))
                    Text("৳${invoice.paidAmount.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(LanguageHelper.translate("lbl_due", lang), fontSize = 13.sp, color = Color(0xFF94A3B8))
                    Text("৳${invoice.dueAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (invoice.dueAmount > 0) Color(0xFFEF4444) else Color(0xFF10B981))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Preparing dynamic billing PDF rendering...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val msg = "Dear ${invoice.memberName}, your Invoice ${invoice.invoiceNumber} for ${invoice.billingMonth} has outstanding DUES of ৳${invoice.dueAmount.toInt()}. Please clear via bKash/Nagad. Thank you, SMART MESS BD ERP."
                            val uri = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(msg))
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WhatsApp", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

// 8. MOCK PAYMENT GATEWAY UPGRADE / LICENSE PAYMENT SIMULATOR
@Composable
fun MockPaymentGatewayUpgradeDialog(vm: MessViewModel, onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(1) } // 1: Select payment method, 2: Insert Pin/Sim, 3: Success!
    var simulatedMethod by remember { mutableStateOf("bKash") }
    var userNumber by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (simulatedMethod == "bKash" && step == 2) Color(0xFFE11D48) else MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (step == 1) {
                    Text("SaaS License Renewer (MFS SDK)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    Text("Simulate payment processing through local high-security Bangladeshi gateways.", fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)

                    val providers = listOf("bKash", "Nagad", "Rocket", "Card")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        providers.forEach { prod ->
                            Button(
                                onClick = { simulatedMethod = prod; step = 2 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (prod) {
                                        "bKash" -> Color(0xFFE11D48)
                                        "Nagad" -> Color(0xFFEA580C)
                                        else -> Color(0xFF0F172A)
                                    }
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(prod, fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = onDismiss) { Text("Cancel Integration") }
                } else if (step == 2) {
                    Text(
                        text = "$simulatedMethod Merchant Checkout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = userNumber,
                        onValueChange = { userNumber = it },
                        placeholder = { Text("Your 11 digit MFS wallet number", color = Color.LightGray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("By clicking proceed you agree to simulate purchasing a 1-year Pro SaaS license for SMART MESS BD.", fontSize = 9.sp, color = Color.LightGray, textAlign = TextAlign.Center)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { step = 1 }, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Text("Back") }
                        
                        Button(
                            onClick = {
                                if (userNumber.length < 11) {
                                    return@Button
                                }
                                isVerifying = true
                                // Simulate network callback
                                step = 3
                                vm.saasPlan.value = "Enterprise ERP Plan"
                                vm.saasLicenseStatus.value = "Active"
                                vm.trialDaysRemaining.value = 365
                                vm.saasExpiryDate.value = "2027-06-06"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0F172A)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("PROCEED", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Success!
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("LICENSE UNLOCKED!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Text("Enterprise SaaS Features active for 365 days until June 2027. Autogenerated receipt emailed to account admin.", fontSize = 11.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Get back to ERP Tools")
                    }
                }
            }
        }
    }
}

@Composable
fun RechartsResponsiveLineChart(
    trendData: List<MonthlyTrendData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_line_chart_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash Flow Trend Analysis",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "recharts-compose-render-v1.4",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
                
                // Indicators Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Income", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFB7185))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expenses", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (trendData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No historical records matching", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            } else {
                val maxVal = remember(trendData) {
                    val maxInc = trendData.maxOfOrNull { it.income } ?: 0.0
                    val maxExp = trendData.maxOfOrNull { it.expense } ?: 0.0
                    maxOf(maxInc, maxExp, 1000.0) * 1.15 // 15% padding top
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 110f
                    val paddingRight = 40f
                    val paddingTop = 15f
                    val paddingBottom = 40f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    // Draw Horizontal Gridlines
                    val gridLinesCount = 3
                    for (i in 0..gridLinesCount) {
                        val y = paddingTop + (chartHeight / gridLinesCount) * i
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Compute point locations
                    val pointSpacing = chartWidth / (trendData.size - 1).coerceAtLeast(1)
                    val incomePoints = mutableListOf<Offset>()
                    val expensePoints = mutableListOf<Offset>()

                    trendData.forEachIndexed { index, data ->
                        val x = paddingLeft + index * pointSpacing
                        
                        // Y coordinate for Income (scaled)
                        val yInc = paddingTop + chartHeight - ((data.income / maxVal) * chartHeight).toFloat()
                        incomePoints.add(Offset(x, yInc))
                        
                        // Y coordinate for Expense (scaled)
                        val yExp = paddingTop + chartHeight - ((data.expense / maxVal) * chartHeight).toFloat()
                        expensePoints.add(Offset(x, yExp))
                    }

                    // Draw Income Line Path (Smooth cubic curve)
                    val incomePath = Path().apply {
                        if (incomePoints.isNotEmpty()) {
                            moveTo(incomePoints[0].x, incomePoints[0].y)
                            for (index in 1 until incomePoints.size) {
                                val p0 = incomePoints[index - 1]
                                val p1 = incomePoints[index]
                                val controlPointX1 = p0.x + (p1.x - p0.x) / 3f
                                val controlPointY1 = p0.y
                                val controlPointX2 = p0.x + 2f * (p1.x - p0.x) / 3f
                                val controlPointY2 = p1.y
                                cubicTo(controlPointX1, controlPointY1, controlPointX2, controlPointY2, p1.x, p1.y)
                            }
                        }
                    }

                    drawPath(
                        path = incomePath,
                        color = Color(0xFF10B981),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                    )

                    // Draw Expense Line Path (Smooth cubic curve)
                    val expensePath = Path().apply {
                        if (expensePoints.isNotEmpty()) {
                            moveTo(expensePoints[0].x, expensePoints[0].y)
                            for (index in 1 until expensePoints.size) {
                                val p0 = expensePoints[index - 1]
                                val p1 = expensePoints[index]
                                val controlPointX1 = p0.x + (p1.x - p0.x) / 3f
                                val controlPointY1 = p0.y
                                val controlPointX2 = p0.x + 2f * (p1.x - p0.x) / 3f
                                val controlPointY2 = p1.y
                                cubicTo(controlPointX1, controlPointY1, controlPointX2, controlPointY2, p1.x, p1.y)
                            }
                        }
                    }

                    drawPath(
                        path = expensePath,
                        color = Color(0xFFFB7185),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                    )

                    // Draw Data Dots & Labels
                    trendData.forEachIndexed { index, data ->
                        val x = paddingLeft + index * pointSpacing

                        // Draw Dot for Income
                        val yInc = incomePoints[index].y
                        drawCircle(
                            color = Color(0xFF10B981),
                            radius = 4.dp.toPx(),
                            center = Offset(x, yInc)
                        )
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = 2.dp.toPx(),
                            center = Offset(x, yInc)
                        )

                        // Draw Dot for Expense
                        val yExp = expensePoints[index].y
                        drawCircle(
                            color = Color(0xFFFB7185),
                            radius = 4.dp.toPx(),
                            center = Offset(x, yExp)
                        )
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = 2.dp.toPx(),
                            center = Offset(x, yExp)
                        )

                        // Labels: X Axis Month Names
                        drawContext.canvas.nativeCanvas.drawText(
                            data.monthLabel,
                            x,
                            height - 10f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#94A3B8")
                                textSize = 26f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }

                    // Draw Y Axis Labels (Min / Max)
                    val labelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 24f
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "৳${(maxVal / 2).toInt()}",
                        10f,
                        paddingTop + chartHeight / 2 + 8f,
                        labelPaint
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "৳${maxVal.toInt()}",
                        10f,
                        paddingTop + 24f,
                        labelPaint
                    )
                }
            }
        }
    }
}

@Composable
fun SmartMessLogo(
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 52.dp,
    showText: Boolean = true,
    textSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    subtextSize: androidx.compose.ui.unit.TextUnit = 9.sp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(iconSize)
                .testTag("smart_mess_logo_canvas")
        ) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            val radius = w / 2f - 2f.dp.toPx()

            // 1. Draw outer green circle stroke (representing high-quality meal circle frame)
            drawCircle(
                color = Color(0xFF047857), // emerald-700 green
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f.dp.toPx())
            )

            // 2. House Roof: triangle from (0.25w, 0.42h) to (0.5w, 0.18h) to (0.75w, 0.42h)
            val pRoof = Path().apply {
                moveTo(w * 0.25f, h * 0.42f)
                lineTo(w * 0.5f, h * 0.18f)
                lineTo(w * 0.75f, h * 0.42f)
            }
            drawPath(
                path = pRoof,
                color = Color(0xFF047857),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f.dp.toPx())
            )

            // 3. Chimney on the roof side
            drawRect(
                color = Color(0xFF047857),
                topLeft = Offset(w * 0.64f, h * 0.22f),
                size = androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.12f)
            )

            // 4. 4-Paned Window inside the house
            val winSize = w * 0.05f
            val winGap = w * 0.012f
            val winLeft = w * 0.455f
            val winTop = h * 0.28f
            drawRect(Color(0xFF047857), topLeft = Offset(winLeft, winTop), size = androidx.compose.ui.geometry.Size(winSize, winSize))
            drawRect(Color(0xFF047857), topLeft = Offset(winLeft + winSize + winGap, winTop), size = androidx.compose.ui.geometry.Size(winSize, winSize))
            drawRect(Color(0xFF047857), topLeft = Offset(winLeft, winTop + winSize + winGap), size = androidx.compose.ui.geometry.Size(winSize, winSize))
            drawRect(Color(0xFF047857), topLeft = Offset(winLeft + winSize + winGap, winTop + winSize + winGap), size = androidx.compose.ui.geometry.Size(winSize, winSize))

            // 5. Meal Cover Cloche (the dome dish representing Meal Tracking)
            val clocheWidth = w * 0.32f
            val clocheHeight = h * 0.16f
            val clocheLeft = w * 0.34f
            val clocheTop = h * 0.5f
            drawArc(
                color = Color(0xFF047857),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(clocheLeft, clocheTop),
                size = androidx.compose.ui.geometry.Size(clocheWidth, clocheHeight * 2)
            )
            // Handle on top of meal cloche
            drawCircle(
                color = Color(0xFF047857),
                radius = w * 0.035f,
                center = Offset(w * 0.5f, clocheTop)
            )

            // 6. Steam Heat waves rising
            val steamPath = Path().apply {
                moveTo(w * 0.46f, clocheTop - 3f.dp.toPx())
                quadraticTo(w * 0.44f, clocheTop - 7f.dp.toPx(), w * 0.46f, clocheTop - 11f.dp.toPx())
                
                moveTo(w * 0.5f, clocheTop - 4f.dp.toPx())
                quadraticTo(w * 0.48f, clocheTop - 10f.dp.toPx(), w * 0.5f, clocheTop - 15f.dp.toPx())
                
                moveTo(w * 0.54f, clocheTop - 3f.dp.toPx())
                quadraticTo(w * 0.49f, clocheTop - 7f.dp.toPx(), w * 0.54f, clocheTop - 11f.dp.toPx())
            }
            drawPath(
                path = steamPath,
                color = Color(0xFF047857),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f.dp.toPx())
            )

            // 7. Platter solid curved band below the cloche
            drawArc(
                color = Color(0xFF0F172A),
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(w * 0.30f, h * 0.51f),
                size = androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.22f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.2f.dp.toPx())
            )

            // 8. Knife/Fork on left side
            drawLine(
                color = Color(0xFF0F172A),
                start = Offset(w * 0.28f, h * 0.48f),
                end = Offset(w * 0.28f, h * 0.70f),
                strokeWidth = 2.dp.toPx()
            )
            drawOval(
                color = Color(0xFF0F172A),
                topLeft = Offset(w * 0.25f, h * 0.40f),
                size = androidx.compose.ui.geometry.Size(w * 0.055f, h * 0.08f)
            )

            // 9. Spoon on right side
            drawLine(
                color = Color(0xFF0F172A),
                start = Offset(w * 0.72f, h * 0.48f),
                end = Offset(w * 0.72f, h * 0.70f),
                strokeWidth = 2.dp.toPx()
            )
            drawOval(
                color = Color(0xFF0F172A),
                topLeft = Offset(w * 0.69f, h * 0.38f),
                size = androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.11f)
            )
        }

        if (showText) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Smart",
                        fontSize = textSize,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981) // emerald accent green
                    )
                    Text(
                        text = "Mess",
                        fontSize = textSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White // Elegant white of modern styling
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF047857))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BD",
                            fontSize = (subtextSize.value + 1.2f).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "Smart Meal & Mess Management Solution",
                    fontSize = subtextSize,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

