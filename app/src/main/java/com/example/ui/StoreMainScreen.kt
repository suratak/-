package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreMainScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    var isGatewayOpen by remember { mutableStateOf(false) }
    var gatewayInvoiceAmount by remember { mutableStateOf(0.0) }
    var onGatewaySuccess by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Wrap entire layout in beautiful RTL Direction for native Persian layout rendering
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val scope = rememberCoroutineScope()
        
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "سیستم حسابداری و انبارداری آسان",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                
                                // Dark Mode Switch & User Persona Indicator
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // User badge tag
                                    Surface(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = viewModel.activeStaff?.name ?: "مهمان",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.isDarkTheme = !viewModel.isDarkTheme },
                                        modifier = Modifier.testTag("theme_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (viewModel.isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                            contentDescription = "تغییر تم"
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                bottomBar = {
                    // Responsive Navigation Bar: Show bottom bar only on standard mobile viewports (< 600dp)
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            val items = listOf(
                                Triple("dashboard", "داشبورد", Icons.Default.Dashboard),
                                Triple("pos", "صندوق فروش", Icons.Default.ShoppingCart),
                                Triple("products", "انبار کالا", Icons.Default.Inventory),
                                Triple("invoices", "فاکتورها", Icons.Default.ReceiptLong),
                                Triple("reports", "گزارش مالی", Icons.Default.Assessment)
                            )
                            items.forEach { (screen, label, icon) ->
                                NavigationBarItem(
                                    selected = viewModel.currentScreen == screen,
                                    onClick = { viewModel.currentScreen = screen },
                                    icon = { Icon(imageVector = icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("nav_btn_$screen")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Responsive Navigation Rail: Show side rail only on wider viewports (tablets/landscape)
                    if (isWideScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            header = {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).padding(vertical = 8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            val railItems = listOf(
                                Triple("dashboard", "داشبورد", Icons.Default.Dashboard),
                                Triple("pos", "صندوق", Icons.Default.ShoppingCart),
                                Triple("products", "انبار", Icons.Default.Inventory),
                                Triple("invoices", "فاکتورها", Icons.Default.ReceiptLong),
                                Triple("customers", "مشتریان", Icons.Default.People),
                                Triple("staff", "پرسنل", Icons.Default.Badge),
                                Triple("reports", "گزارش مالی", Icons.Default.Assessment)
                            )
                            railItems.forEach { (screen, label, icon) ->
                                NavigationRailItem(
                                    selected = viewModel.currentScreen == screen,
                                    onClick = { viewModel.currentScreen = screen },
                                    icon = { Icon(imageVector = icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("wide_nav_btn_$screen")
                                )
                            }
                        }
                    }

                    // Main App View Controller Router
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (viewModel.currentScreen) {
                            "dashboard" -> DashboardScreen(viewModel)
                            "pos" -> PosScreen(viewModel, onTrigGateway = { amt, callback ->
                                gatewayInvoiceAmount = amt
                                onGatewaySuccess = callback
                                isGatewayOpen = true
                            })
                            "products" -> ProductsListScreen(viewModel)
                            "invoices" -> InvoicesLogScreen(viewModel)
                            "customers" -> CustomersScreen(viewModel)
                            "staff" -> StaffScreen(viewModel)
                            "reports" -> ReportsScreen(viewModel)
                            else -> DashboardScreen(viewModel)
                        }
                    }
                }
            }
        }
        
        // --- Popups and Dialog Portals ---
        if (isGatewayOpen) {
            GatewayDialog(
                amount = gatewayInvoiceAmount,
                onCancel = { isGatewayOpen = false },
                onSuccess = {
                    isGatewayOpen = false
                    onGatewaySuccess?.invoke()
                }
            )
        }
    }
}

// --- HELPER FORMATTING UTILS FOR PERSIAN COINS AND DECIMALS ---
fun formatPersianCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(amount) + " ریال"
}

// ==========================================
// 1. DASHBOARD SCREEN (داشبورد اصلی)
// ==========================================
@Composable
fun DashboardScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val allProds by viewModel.products.collectAsStateWithLifecycle()
    val allInvoices by viewModel.invoices.collectAsStateWithLifecycle()
    
    // Quick calculations for the today metric
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val todayCode = sdf.format(Date())
    
    val todayInvoices = allInvoices.filter {
        sdf.format(Date(it.dateMillis)) == todayCode
    }
    
    val todaySales = todayInvoices.sumOf { it.finalAmount }
    val todayCOGS = todayInvoices.sumOf { it.totalCost }
    val todayDiscounts = todayInvoices.sumOf { it.discount }
    val todayProfit = (todaySales - todayCOGS).coerceAtLeast(0.0)
    
    val totalInventoryValue = allProds.sumOf { it.quantity * it.sellPrice }
    val lowStockItems = allProds.filter { it.quantity <= it.minQuantity }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Cloud Sync section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مدیریت یکپارچه انبار و صندوق",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "نرم‌افزار شما ابری است. همگام‌سازی دوره‌ای به امنیت داده‌ها کمک می‌کند.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "آخرین همگام‌سازی ابری: ${viewModel.lastBackupTime}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                    
                    Box(modifier = Modifier.padding(start = 12.dp)) {
                        if (viewModel.isBackupInProgress) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Button(
                                onClick = { viewModel.triggerCloudBackup(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("cloud_backup_btn")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("پشتیبان‌گیری", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick KPI Stats Grid
        item {
            Column {
                Text(
                    text = "وضعیت مالی امروز کارهای فروشگاه",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Net sales today
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("فروش خالص امروز", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(formatPersianCurrency(todaySales), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Profits today
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("سود برآوردی امروز", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(formatPersianCurrency(todayProfit), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Inventory Assets value card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ارزش دارایی کالا در انبار", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(formatPersianCurrency(totalInventoryValue), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }

                    // Invoices Count
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("فاکتورهای امروز", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("${todayInvoices.size} فاکتور ثبت شده", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Low stock Warning Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (lowStockItems.isNotEmpty()) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning, 
                                contentDescription = null, 
                                tint = if (lowStockItems.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "کالاهای نیازمند انبوه‌سازی و خرید",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        
                        Surface(
                            shape = CircleShape,
                            color = (if (lowStockItems.isNotEmpty()) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.clip(CircleShape)
                        ) {
                            Text(
                                text = "${lowStockItems.size} کالا بحرانی",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lowStockItems.isNotEmpty()) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (lowStockItems.isEmpty()) {
                        Text(
                            text = "موجودی تمامی کالاهای تعریف شده در حد مناسبی است.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lowStockItems.take(4).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("شناسه فنی: ${item.sku} | دسته‌بندی: ${item.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "موجودی: ${item.quantity} عدد",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = "(حداقل آلارم: ${item.minQuantity})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                            
                            if (lowStockItems.size > 4) {
                                TextButton(
                                    onClick = { viewModel.currentScreen = "products" },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("مشاهده اطلاعات تمامی کسری‌های انبار", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Fast Action Buttons Footer Row
        item {
            Column {
                Text(
                    text = "دسترسی‌های سریع",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.currentScreen = "pos" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فاکتور جدید (POS)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.currentScreen = "customers" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مدیریت مشتریان", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.currentScreen = "staff" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تعریف کاربر/همکار", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. POS SCREEN (ثبت فاکتور سریع)
// ==========================================
@Composable
fun PosScreen(viewModel: StoreViewModel, onTrigGateway: (Double, () -> Unit) -> Unit) {
    val context = LocalContext.current
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    val allCustomers by viewModel.customers.collectAsStateWithLifecycle()
    val cart by viewModel.activeCart
    
    var showCustomerDropdown by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("نقد") }
    var inputDiscountStr by remember { mutableStateOf("") }

    val filteredProducts = allProducts.filter {
        it.name.contains(viewModel.productSearchQuery, ignoreCase = true) ||
        it.sku.contains(viewModel.productSearchQuery, ignoreCase = true)
    }

    // Calculations for cart values
    val itemsSubtotal = cart.entries.sumOf { (pId, qty) ->
        val prod = allProducts.find { it.id == pId }
        (prod?.sellPrice ?: 0.0) * qty
    }

    val discount = inputDiscountStr.toDoubleOrNull() ?: 0.0
    val totalToPay = (itemsSubtotal - discount).coerceAtLeast(0.0)

    Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Left Column: Cart checkout section (Responsive weighting)
        Card(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text(
                    text = "فاکتور جاری صادر شده",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Selected items list
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (cart.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ProductionQuantityLimits,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "فاکتور خالی است. کالاها را اضافه کنید.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(cart.toList()) { (productId, qty) ->
                            val item = allProducts.find { it.id == productId } ?: return@items
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                    Text(
                                        text = "${qty} عدد × ${formatPersianCurrency(item.sellPrice)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.removeProductFromCart(productId) },
                                        modifier = Modifier.size(28.dp).testTag("cart_remove_$productId")
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                    
                                    Text(
                                        text = qty.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.addProductToCart(productId, item.quantity) },
                                        modifier = Modifier.size(28.dp).testTag("cart_add_$productId")
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Customer selector dropdown
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Button(
                        onClick = { showCustomerDropdown = !showCustomerDropdown },
                        modifier = Modifier.fillMaxWidth().testTag("pos_customer_selector"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.selectedCustomerForCart?.name ?: "انتخاب مشتری (متفرقه)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }

                    DropdownMenu(
                        expanded = showCustomerDropdown,
                        onDismissRequest = { showCustomerDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("مشتری متفرقه (بدون ثبت نام)", fontSize = 13.sp) },
                            onClick = {
                                viewModel.selectedCustomerForCart = null
                                showCustomerDropdown = false
                            }
                        )
                        allCustomers.forEach { cust ->
                            DropdownMenuItem(
                                text = { Text("${cust.name} (${cust.phone})", fontSize = 13.sp) },
                                onClick = {
                                    viewModel.selectedCustomerForCart = cust
                                    showCustomerDropdown = false
                                }
                            )
                        }
                    }
                }

                // Sale variables insets
                OutlinedTextField(
                    value = inputDiscountStr,
                    onValueChange = { inputDiscountStr = it },
                    label = { Text("میزان تخفیف (ریال)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("pos_discount_input"),
                    singleLine = true
                )

                // Payment mechanism selection row
                Text("روش تسویه فاکتور:", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val methods = listOf("نقد", "کارتخوان", "درگاه آنلاین")
                    methods.forEach { mode ->
                        val selected = selectedPaymentMethod == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedPaymentMethod = mode }
                                .testTag("pay_mode_$mode"),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = mode,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // Totals summary panel
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("مبلغ کل خرید:", fontSize = 12.sp)
                    Text(formatPersianCurrency(itemsSubtotal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("میزان تخفیف:", fontSize = 12.sp)
                    Text(formatPersianCurrency(discount), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("قایل پرداخت نهایی:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(formatPersianCurrency(totalToPay), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Final Pay Trigger Button
                Button(
                    onClick = {
                        if (cart.isEmpty()) {
                            Toast.makeText(context, "فاکتور خالی است!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.cartDiscount = discount
                        
                        if (selectedPaymentMethod == "درگاه آنلاین") {
                            // Launch payment gateway mock popup
                            onTrigGateway(totalToPay) {
                                viewModel.checkoutCart("درگاه آنلاین", true) {
                                    inputDiscountStr = ""
                                    Toast.makeText(context, "فاکتور آنلاین با موفقیت صادر و تایید شد", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            viewModel.checkoutCart(selectedPaymentMethod, true) {
                                inputDiscountStr = ""
                                Toast.makeText(context, "فاکتور تسویه شد و انبار آپدیت گردید", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pos_checkout_submit"),
                    enabled = cart.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ثبت فاکتور و کسر از انبار", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Right Column: Products inventory selector panel
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = viewModel.productSearchQuery,
                onValueChange = { viewModel.productSearchQuery = it },
                placeholder = { Text("جستجو در انبار کالا (نام، کلمه‌کلیدی، بارکد...)", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("pos_search_input"),
                singleLine = true
            )

            // Dynamic grid layout displaying the stocks
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "هیچ کالا یا محصولی با این مشخصه یافت نشد.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { item ->
                        val currentCartQty = cart[item.id] ?: 0
                        val actualAvailable = item.quantity - currentCartQty
                        val outOfStock = item.quantity == 0
                        val limitReached = actualAvailable <= 0

                        Card(
                            modifier = Modifier
                                .clickable(enabled = !outOfStock && !limitReached) {
                                    viewModel.addProductToCart(item.id, item.quantity)
                                }
                                .testTag("pos_prod_item_${item.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentCartQty > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (currentCartQty > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Surface(
                                    modifier = Modifier.align(Alignment.End),
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (outOfStock) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (outOfStock) "اتمام" else "انبار: ${item.quantity} عدد",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (outOfStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "شناسه: ${item.sku}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatPersianCurrency(item.sellPrice),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    if (currentCartQty > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = currentCartQty.toString(),
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. PRODUCTS LIST / WAREHOUSE SCREEN (انبارداری)
// ==========================================
@Composable
fun ProductsListScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    var isAddDialogVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Form states
    var prodName by remember { mutableStateOf("") }
    var prodSku by remember { mutableStateOf("") }
    var prodBuyUnit by remember { mutableStateOf("") }
    var prodSellUnit by remember { mutableStateOf("") }
    var prodQuantity by remember { mutableStateOf("") }
    var prodMinQty by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("خواروبار") }

    val filtered = allProducts.filter {
        it.name.contains(viewModel.productSearchQuery, ignoreCase = true) ||
        it.sku.contains(viewModel.productSearchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("مدیریت فیزیکی انبار کالاها", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            // Check Staff authority before adding products
            Button(
                onClick = { isAddDialogVisible = true },
                enabled = viewModel.simulatedAccessLevel >= 1, // Manager (1) or Owner (2) required!
                modifier = Modifier.testTag("add_product_trigger")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("افزودن کالای جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        if (viewModel.simulatedAccessLevel < 1) {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("سطح دسترسی شما کافی نیست. افزودن/حذف کالا فقط مخصوص مدیر انبار و املاک است.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        OutlinedTextField(
            value = viewModel.productSearchQuery,
            onValueChange = { viewModel.productSearchQuery = it },
            placeholder = { Text("بین کالاهای انبار جستجو کنید...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("warehouse_search_input"),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("کالایی یافت نشد.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                items(filtered) { prod ->
                    val isDeficient = prod.quantity <= prod.minQuantity
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("warehouse_prod_${prod.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isDeficient) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = prod.category,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "کد بارکد فنی: ${prod.sku} | خرید: ${formatPersianCurrency(prod.buyPrice)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                
                                Text(
                                    text = "فروش مصوب: ${formatPersianCurrency(prod.sellPrice)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Right controllers (quantity edit/delete)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                                    Text(
                                        text = "${prod.quantity} عدد موجود",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDeficient) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                    )
                                    if (isDeficient) {
                                        Text(
                                            text = "نیازمند خرید فوری (حداقل: ${prod.minQuantity})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                // Interactive Quick-Edit inventory quantity
                                if (viewModel.simulatedAccessLevel >= 1) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                viewModel.updateProductQuantity(prod.id, prod.quantity + 5)
                                                Toast.makeText(context, "۵ عدد به موجودی ${prod.name} اضافه شد", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.testTag("add_qty_btn_${prod.id}")
                                    ) {
                                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteProduct(prod.id)
                                            Toast.makeText(context, "کالا با موفقیت حذف گردید", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("delete_prod_btn_${prod.id}"),
                                        enabled = viewModel.simulatedAccessLevel >= 2 // Owner only can delete items!
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to add kalar/product
    if (isAddDialogVisible) {
        Dialog(onDismissRequest = { isAddDialogVisible = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("تعریف و افزودن شناسه کالای جدید به انبار", fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text("نام دقیق محصول کالا") },
                        modifier = Modifier.fillMaxWidth().testTag("add_prod_field_name")
                    )

                    OutlinedTextField(
                        value = prodSku,
                        onValueChange = { prodSku = it },
                        label = { Text("کد استاندارد یا بارکد (SKU)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_prod_field_sku"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = prodCategory,
                        onValueChange = { prodCategory = it },
                        label = { Text("دسته‌بندی (ادویه، حبوبات، نوشیدنی...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = prodBuyUnit,
                            onValueChange = { prodBuyUnit = it },
                            label = { Text("قیمت خرید (ریال)") },
                            modifier = Modifier.weight(1f).testTag("add_prod_field_buy"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = prodSellUnit,
                            onValueChange = { prodSellUnit = it },
                            label = { Text("قیمت فروش (ریال)") },
                            modifier = Modifier.weight(1f).testTag("add_prod_field_sell"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = prodQuantity,
                            onValueChange = { prodQuantity = it },
                            label = { Text("موجودی اولیه") },
                            modifier = Modifier.weight(1f).testTag("add_prod_field_qty"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = prodMinQty,
                            onValueChange = { prodMinQty = it },
                            label = { Text("حداقل هشدار") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAddDialogVisible = false }) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val buyVal = prodBuyUnit.toDoubleOrNull() ?: 0.0
                                val sellVal = prodSellUnit.toDoubleOrNull() ?: 0.0
                                val qtyVal = prodQuantity.toIntOrNull() ?: 0
                                val minVal = prodMinQty.toIntOrNull() ?: 5
                                
                                if (prodName.isNotEmpty() && prodSku.isNotEmpty() && buyVal > 0 && sellVal > 0) {
                                    viewModel.addProduct(prodName, prodSku, buyVal, sellVal, qtyVal, minVal, prodCategory)
                                    isAddDialogVisible = false
                                    // reset inputs
                                    prodName = ""
                                    prodSku = ""
                                    prodBuyUnit = ""
                                    prodSellUnit = ""
                                    prodQuantity = ""
                                    prodMinQty = ""
                                    Toast.makeText(context, "کالا به انبار اضافه شد.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "شرح دقیق مقادیر کالا را تکمیل کنید.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("add_product_dialog_submit")
                        ) {
                            Text("ثبت و درج انبار")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. INVOICES LOG SCREEN (مشاهده فاکتورهای صادر شده)
// ==========================================
@Composable
fun InvoicesLogScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val allInvoices by viewModel.invoices.collectAsStateWithLifecycle()
    var selectedDetailsInvoice by remember { mutableStateOf<Invoice?>(null) }
    var activeInvoiceItems by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
    val scope = rememberCoroutineScope()

    var filterQuery by remember { mutableStateOf("") }
    
    val filtered = allInvoices.filter {
        it.customerName.contains(filterQuery, ignoreCase = true) ||
        it.id.toString() == filterQuery ||
        it.paymentMethod.contains(filterQuery)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("دفتر اسناد و فاکتورهای فروشگاهی صادر شده", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            // Share All Excel Export Button
            Button(
                onClick = {
                    val csvReport = viewModel.generateSalesCsvReport(allInvoices)
                    
                    // Copy to clipboard
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("گزارش حسابداری فروش", csvReport)
                    clipboard.setPrimaryClip(clip)
                    
                    // Open share sheet
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csvReport)
                        type = "text/csv"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "ارسال و خروجی اکسل فاکتورها")
                    context.startActivity(shareIntent)
                    
                    Toast.makeText(context, "داده‌های اکسل در حافظه کپی و فایل جهت اشتراک‌گذاری آماده گردید", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.testTag("export_excel_all_btn")
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("خروجی اکسل (CSV)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            placeholder = { Text("پیش جستجوی فاکتور با نام مشتری، شناسه عددی فاکتور یا درگاه پرداخت...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("invoice_search_input"),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("موردی یافت نشد.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                items(filtered) { invoice ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val dateFormatted = sdf.format(Date(invoice.dateMillis))
                    val profit = invoice.finalAmount - invoice.totalCost

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    activeInvoiceItems = viewModel.getItemsForInvoice(invoice.id)
                                    selectedDetailsInvoice = invoice
                                }
                            }
                            .testTag("invoice_row_${invoice.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("فاکتور شماره: ${invoice.id} #", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = invoice.paymentMethod,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "مشتری: ${invoice.customerName} | صندوقدار: ${invoice.staffName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "زمان صدور سند: $dateFormatted",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatPersianCurrency(invoice.finalAmount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "حاشیه سود: ${formatPersianCurrency(profit)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal details display popup
    selectedDetailsInvoice?.let { invoice ->
        Dialog(onDismissRequest = { selectedDetailsInvoice = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "تفاصیل اقلام فاکتور صادر شده",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مشتری: ${invoice.customerName}", fontSize = 12.sp)
                        Text("روش خرید: ${invoice.paymentMethod}", fontSize = 12.sp)
                    }

                    Divider()

                    // Table items lazy loading inside Dialog
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        activeInvoiceItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("- ${item.productName} (تعداد: ${item.quantity})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(formatPersianCurrency(item.sellPrice * item.quantity), fontSize = 12.sp)
                            }
                        }
                    }

                    Divider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مجموع کل پیش از تخفیف:", fontSize = 12.sp)
                        Text(formatPersianCurrency(invoice.totalAmount), fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تخفیف کسر شده:", fontSize = 12.sp)
                        Text(formatPersianCurrency(invoice.discount), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("پرداخت نهایی:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(formatPersianCurrency(invoice.finalAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Refund controls (Only Owners can roll-back and restore inventory)
                        if (viewModel.simulatedAccessLevel >= 2) {
                            Button(
                                onClick = {
                                    viewModel.refundInvoice(invoice.id)
                                    selectedDetailsInvoice = null
                                    Toast.makeText(context, "فاکتور مرجوع گردید و اقلام آن به انبار بازگردانده شدند", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("refund_invoice_btn")
                            ) {
                                Icon(Icons.Default.RestorePage, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مرجوع کردن فاکتور", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("مرجوع فاکتور مخصوص مالک فروشگاه است.", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }

                        TextButton(onClick = { selectedDetailsInvoice = null }) {
                            Text("بستن")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. CUSTOMERS PANEL SCREEN (مدیریت مشتریان)
// ==========================================
@Composable
fun CustomersScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val allCustomers by viewModel.customers.collectAsStateWithLifecycle()
    var isAddDialogVisible by remember { mutableStateOf(false) }

    var custName by remember { mutableStateOf("") }
    var custPhone by remember { mutableStateOf("") }
    var custEmail by remember { mutableStateOf("") }
    var custDebt by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("سامانه مدیریت پرونده و اعتبار مشتریان", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(onClick = { isAddDialogVisible = true }, modifier = Modifier.testTag("add_customer_trigger")) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("افزودن خریدار جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = viewModel.customerSearchQuery,
            onValueChange = { viewModel.customerSearchQuery = it },
            placeholder = { Text("جستجوی خریداران بر اساس نام و شماره تماس فنی...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).testTag("customer_search_input"),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val filtered = allCustomers.filter {
                it.name.contains(viewModel.customerSearchQuery, ignoreCase = true) ||
                it.phone.contains(viewModel.customerSearchQuery)
            }

            if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("مشتری ثبت شده‌ای وجود ندارد.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            } else {
                items(filtered) { cust ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("cust_card_${cust.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("تلفن همراه: ${cust.phone} | ایمیل: ${cust.email.ifEmpty { "ثبت نشده" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                                    Text("مانده بدهی دفتر:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text(
                                        text = if (cust.debt > 0) formatPersianCurrency(cust.debt) else "تسویه کامل",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (cust.debt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.deleteCustomer(cust.id)
                                        Toast.makeText(context, "پرونده مشتری با موفقیت بایگانی گردید.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("delete_cust_btn_${cust.id}"),
                                    enabled = viewModel.simulatedAccessLevel >= 1
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddDialogVisible) {
        Dialog(onDismissRequest = { isAddDialogVisible = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("تعریف و ثبت اطلاعات مشتری جدید", fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    
                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        label = { Text("نام و نام خانوادگی") },
                        modifier = Modifier.fillMaxWidth().testTag("add_cust_field_name")
                    )

                    OutlinedTextField(
                        value = custPhone,
                        onValueChange = { custPhone = it },
                        label = { Text("تلفن همراه") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("add_cust_field_phone")
                    )

                    OutlinedTextField(
                        value = custEmail,
                        onValueChange = { custEmail = it },
                        label = { Text("نشانی ایمیل و شبکه مجازی (اختیاری)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = custDebt,
                        onValueChange = { custDebt = it },
                        label = { Text("میزان بدهی دفتری گذشته (ریال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAddDialogVisible = false }) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val debtVal = custDebt.toDoubleOrNull() ?: 0.0
                                if (custName.isNotEmpty() && custPhone.isNotEmpty()) {
                                    viewModel.addCustomer(custName, custPhone, custEmail, debtVal)
                                    isAddDialogVisible = false
                                    custName = ""
                                    custPhone = ""
                                    custEmail = ""
                                    custDebt = ""
                                    Toast.makeText(context, "اطلاعات مشتری ثبت گردید", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "نام و تلفن اجباری هستند", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("add_customer_dialog_submit")
                        ) {
                            Text("ثبت و درج دفتر")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. STAFF & ROLES ACCESS MANAGEMENT SCREEN (پرسنل)
// ==========================================
@Composable
fun StaffScreen(viewModel: StoreViewModel) {
    val context = LocalContext.current
    val allStaff by viewModel.staffMembers.collectAsStateWithLifecycle()
    var isAddDialogVisible by remember { mutableStateOf(false) }

    var staffName by remember { mutableStateOf("") }
    var staffRole by remember { mutableStateOf("") }
    var staffPhone by remember { mutableStateOf("") }
    var selectedLevelIndex by remember { mutableStateOf(0) } // 0=Sales, 1=Manager, 2=Owner

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("مدیریت کارکنان و تعویض سطوح دسترسی شبیه‌سازی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = { isAddDialogVisible = true },
                enabled = viewModel.simulatedAccessLevel >= 2, // Only owners can register staff!
                modifier = Modifier.testTag("add_staff_trigger")
            ) {
                Icon(Icons.Default.AddBusiness, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("افزودن همکار جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Switch Active Staff simulation card
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "تغییر سریع کاربر سیستم (صرفا جهت دمو و ارزیابی سطوح دسترسی):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    allStaff.forEach { st ->
                        val isCurrent = viewModel.activeStaff?.id == st.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.switchUser(st) }
                                .padding(8.dp)
                                .testTag("sim_login_${st.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(st.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = when (st.accessLevel) {
                                        2 -> "مالک ارشد"
                                        1 -> "مدیر انبار"
                                        else -> "صندوقدار"
                                    },
                                    fontSize = 9.sp,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allStaff) { st ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("staff_row_${st.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(st.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (st.accessLevel) {
                                        2 -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        1 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = when (st.accessLevel) {
                                            2 -> "تمامی دسترسی‌ها (صاحب کالا)"
                                            1 -> "مدیر انبار (دسترسی کالا)"
                                            else -> "صندوق‌دار (سطح مجاز محدود)"
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (st.accessLevel) {
                                            2 -> MaterialTheme.colorScheme.error
                                            1 -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                            }
                            Text("سمت اداری: ${st.role} | تلفن همراه: ${st.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }

                        if (viewModel.simulatedAccessLevel >= 2) {
                            IconButton(
                                onClick = {
                                    viewModel.deleteStaff(st.id)
                                    Toast.makeText(context, "سند پرسنل لغو شد", Toast.LENGTH_SHORT).show()
                                },
                                enabled = viewModel.activeStaff?.id != st.id, // cannot delete self
                                modifier = Modifier.testTag("delete_staff_btn_${st.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddDialogVisible) {
        Dialog(onDismissRequest = { isAddDialogVisible = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("استخدام و تعریف پرونده ثبت نام همکار جدید", fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    
                    OutlinedTextField(
                        value = staffName,
                        onValueChange = { staffName = it },
                        label = { Text("نام و نام خانوادگی همکار") },
                        modifier = Modifier.fillMaxWidth().testTag("add_staff_field_name")
                    )

                    OutlinedTextField(
                        value = staffRole,
                        onValueChange = { staffRole = it },
                        label = { Text("نقش اداری (مثلا: حسابدار، صندوقدار، مسئول انبار)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_staff_field_role")
                    )

                    OutlinedTextField(
                        value = staffPhone,
                        onValueChange = { staffPhone = it },
                        label = { Text("شماره همراه مستقل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("تعیین لایه دسترسی امنیتی:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val levels = listOf("صندوقدار (سطح ۰)", "مدیر انبار (سطح ۱)", "مالک ارشد (سطح ۲)")
                        levels.forEachIndexed { idx, label ->
                            val selected = selectedLevelIndex == idx
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedLevelIndex = idx }
                                    .testTag("dialog_staff_lvl_$idx"),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isAddDialogVisible = false }) {
                            Text("انصراف")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (staffName.isNotEmpty() && staffRole.isNotEmpty()) {
                                    viewModel.addStaff(staffName, staffRole, staffPhone, selectedLevelIndex)
                                    isAddDialogVisible = false
                                    staffName = ""
                                    staffRole = ""
                                    staffPhone = ""
                                    selectedLevelIndex = 0
                                    Toast.makeText(context, "همکار جدید استخدام و ثبت گردید", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "نام و نقش پرسنل الزامی است", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("add_staff_dialog_submit")
                        ) {
                            Text("تایید استخدام")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. REPORTS & EXCEL PERIOD SCREEN (گزارش سود و زیان)
// ==========================================
@Composable
fun ReportsScreen(viewModel: StoreViewModel) {
    val allInvoices by viewModel.invoices.collectAsStateWithLifecycle()
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Set time definitions for period calculations
    val now = System.currentTimeMillis()
    val dayMillis = 24 * 60 * 60 * 1000L

    val filteredInvoices = allInvoices.filter { invoice ->
        when (viewModel.selectedReportPeriod) {
            "today" -> {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                sdf.format(Date(invoice.dateMillis)) == sdf.format(Date(now))
            }
            "yesterday" -> {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                sdf.format(Date(invoice.dateMillis)) == sdf.format(Date(now - dayMillis))
            }
            "week" -> invoice.dateMillis >= (now - (7 * dayMillis))
            "month" -> invoice.dateMillis >= (now - (30 * dayMillis))
            else -> true // all time
        }
    }

    // Calculations for values within scope
    val totalRevenue = filteredInvoices.sumOf { it.finalAmount }
    val totalCost = filteredInvoices.sumOf { it.totalCost }
    val totalDiscounts = filteredInvoices.sumOf { it.discount }
    val totalProfit = (totalRevenue - totalCost).coerceAtLeast(0.0)

    val methodsBreakdownMap = filteredInvoices.groupBy { it.paymentMethod }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تحلیل حسابرسی سود و زیان دوره‌ای فروشگاه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                // Backup sync trigger
                IconButton(onClick = { viewModel.triggerCloudBackup(context) }) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Period filter buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val periods = listOf(
                    Pair("today", "امروز"),
                    Pair("yesterday", "دیروز"),
                    Pair("week", "۷ روز اخیر"),
                    Pair("month", "۳۰ روز اخیر"),
                    Pair("all", "کل دوره")
                )
                periods.forEach { (code, label) ->
                    val selected = viewModel.selectedReportPeriod == code
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectedReportPeriod = code }
                            .testTag("report_period_$code"),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Financial summary cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "جدول برآورد سود و درآمد بر اساس فاکتورها",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Revenue Item
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("درآمد ناخالص (فروش فاکتورها):", fontSize = 13.sp)
                        }
                        Text(formatPersianCurrency(totalRevenue), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    // COGS Item
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("هزینه تمام‌شده انبارداری کالاهای فروخته شده:", fontSize = 13.sp)
                        }
                        Text(formatPersianCurrency(totalCost), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }

                    // Discounts Given
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("کل تخفیفات نقدی اعطایی به مشتریان:", fontSize = 13.sp)
                        }
                        Text(formatPersianCurrency(totalDiscounts), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    // Neto Profit Item (Calculated with brush highlights)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("سود حاشیه‌ای خالص روزها (سود واقعی فروشگاه):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(formatPersianCurrency(totalProfit), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Transactions count statistics
        item {
            Column {
                Text(
                    text = "آمار توزیع روش‌های مالی تسویه حساب",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val modes = listOf("نقد", "کارتخوان", "درگاه آنلاین")
                        modes.forEach { mode ->
                            val linked = methodsBreakdownMap[mode] ?: emptyList()
                            val count = linked.size
                            val totalVal = linked.sumOf { it.finalAmount }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (mode) {
                                            "نقد" -> Icons.Default.Money
                                            "کارتخوان" -> Icons.Default.CreditCard
                                            else -> Icons.Default.Language
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "$mode ($count تراکنش فاکتور)", fontSize = 13.sp)
                                }
                                Text(formatPersianCurrency(totalVal), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Excel sheet action buttons
        item {
            Button(
                onClick = {
                    val csvText = viewModel.generateSalesCsvReport(filteredInvoices)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("گزارش سود و زیان دوره‌ای", csvText)
                    clipboard.setPrimaryClip(clip)

                    // Action share sheet
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csvText)
                        type = "text/csv"
                    }
                    val shareCmd = Intent.createChooser(sendIntent, "ارسال و اشتراک فایل گزارشاکسل")
                    context.startActivity(shareCmd)

                    Toast.makeText(context, "گزارش اکسل بازه انتخابی کپی و فایل جهت بازکردن در اکسل اشتراک‌گذاری شد.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("excel_export_period_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("خروجی اکسل فاکتورهای دوره‌ای انتخابی", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 8. ACCESSIBLE ONLINE PAYMENT GATEWAY SANDBOX SYSTEM (درگاه مستقیم بانکی شبیه‌ساز)
// ==========================================
@Composable
fun GatewayDialog(
    amount: Double,
    onCancel: () -> Unit,
    onSuccess: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var cvv2 by remember { mutableStateOf("") }
    var expMonth by remember { mutableStateOf("") }
    var expYear by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = { onCancel() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)) // mimic official bank bright slate design
        ) {
            Column(
                modifier = Modifier.padding(18.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bank Portal header banner
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFF0F172A)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("درگاه پرداخت الکترونیک بانک ملی", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("سامانه پرداخت اینترنتی شاپرک", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        }
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                    }
                }

                // Amount Alert row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مبلغ تراکنش فاکتور:", fontSize = 12.sp, color = Color.Gray)
                    Text(formatPersianCurrency(amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }

                // Field options mimicking classic bank input rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 16) cardNumber = it },
                        placeholder = { Text("شماره کارت ۱۶ رقمی (بدون خط تیره)", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("bank_card_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF059669)
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = cvv2,
                            onValueChange = { if (it.length <= 4) cvv2 = it },
                            placeholder = { Text("CVV2", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("bank_cvv_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                        )

                        Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = expMonth,
                                onValueChange = { if (it.length <= 2) expMonth = it },
                                placeholder = { Text("ماه", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                            Text("/", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = expYear,
                                onValueChange = { if (it.length <= 2) expYear = it },
                                placeholder = { Text("سال", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = pin2,
                        onValueChange = { if (it.length <= 8) pin2 = it },
                        placeholder = { Text("رمز دوم پویا یا ثابت کارت بانکی", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("bank_pin_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onCancel() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.weight(1f).testTag("bank_cancel_pay")
                    ) {
                        Text("لغو پرداخت", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (cardNumber.length >= 16 && cvv2.isNotEmpty() && pin2.isNotEmpty()) {
                                Toast.makeText(context, "اتصال به شبکه شتاب و کسر وجه با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            } else {
                                Toast.makeText(context, "اطلاعات کارت انتخابی ناقص است (مثال: شماره ۱۶ رقمی، رمز، CVV2)", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.weight(1.5f).testTag("bank_success_pay")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پرداخت موفق", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
