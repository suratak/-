package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class StoreViewModel(private val repository: StoreRepository) : ViewModel() {

    // --- Dynamic Dark Theme Selection ---
    var isDarkTheme by mutableStateOf(true) // defaults to elegant dark theme

    // --- Active Personnel / Access level simulation ---
    var activeStaff by mutableStateOf<Staff?>(null)
        private set
    
    // Default current access level for testing (2: Owner, 1: Manager, 0: Salesperson)
    var simulatedAccessLevel by mutableStateOf(2) 

    // --- Selected Navigation Screen State ---
    // Screens: "dashboard", "products", "invoices", "pos", "customers", "staff", "reports"
    var currentScreen by mutableStateOf("dashboard")

    // --- Search & Filter Queries ---
    var productSearchQuery by mutableStateOf("")
    var customerSearchQuery by mutableStateOf("")
    var invoiceSearchQuery by mutableStateOf("")

    // --- Database Flow Properties ---
    val products = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customers = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val staffMembers = repository.allStaff.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val invoices = repository.allInvoices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Active Sale POS Cart State ---
    // Product ID -> Selected Quantity
    var activeCart = mutableStateOf<Map<Int, Int>>(emptyMap())
    var selectedCustomerForCart by mutableStateOf<Customer?>(null)
    var cartDiscount by mutableStateOf(0.0)

    // --- Reporting Window Filter ---
    // Options: "today", "yesterday", "week", "month", "all"
    var selectedReportPeriod by mutableStateOf("all")

    // --- Cloud Backup Simulation State ---
    var isBackupInProgress by mutableStateOf(false)
    var lastBackupTime by mutableStateOf("هرگز")

    init {
        seedInitialDemoData()
        // Try searching for an owner to set as default active staff
        viewModelScope.launch {
            repository.allStaff.collectLatest { sList ->
                if (sList.isNotEmpty() && activeStaff == null) {
                    val owner = sList.find { it.accessLevel == 2 } ?: sList.first()
                    activeStaff = owner
                    simulatedAccessLevel = owner.accessLevel
                }
            }
        }
    }

    // Switch active personnel
    fun switchUser(staff: Staff) {
        activeStaff = staff
        simulatedAccessLevel = staff.accessLevel
    }

    // --- Preloading Realistic Persian Seed Data ---
    private fun seedInitialDemoData() {
        viewModelScope.launch {
            // Wait for DB flow to see if products is empty
            products.first { true } // wait for dynamic list initialization
            
            val currentProds = repository.allProducts.first()
            if (currentProds.isEmpty()) {
                // Insert Sample Products
                val p1 = Product(name = "زعفران ۱ مثقال ممتاز", sku = "1001", buyPrice = 400000.0, sellPrice = 550000.0, quantity = 45, minQuantity = 10, category = "ادویه‌جات")
                val p2 = Product(name = "برنج ایرانی طارم ۱۰ کیلو", sku = "1002", buyPrice = 950000.0, sellPrice = 1200000.0, quantity = 30, minQuantity = 5, category = "خواروبار")
                val p3 = Product(name = "روغن سرخ‌کردنی ۱.۵ لیتر", sku = "1003", buyPrice = 750000.0, sellPrice = 980000.0, quantity = 3, minQuantity = 10, category = "خواروبار")
                val p4 = Product(name = "چای سیاه مطر ۴۵۰ گرم", sku = "1004", buyPrice = 140000.0, sellPrice = 185000.0, quantity = 4, minQuantity = 5, category = "نوشیدنی")
                val p5 = Product(name = "عسل طبیعی سبلان ۱ کیلو", sku = "1005", buyPrice = 220000.0, sellPrice = 295000.0, quantity = 15, minQuantity = 3, category = "صبحانه")
                val p6 = Product(name = "شکلات کادویی تلخ آیدین", sku = "1006", buyPrice = 110000.0, sellPrice = 155000.0, quantity = 22, minQuantity = 5, category = "تنقلات")
                
                repository.insertProduct(p1)
                repository.insertProduct(p2)
                repository.insertProduct(p3)
                repository.insertProduct(p4)
                repository.insertProduct(p5)
                repository.insertProduct(p6)

                // Insert Sample Customers
                val c1 = Customer(name = "امیرحسین رضایی", phone = "09121112233", email = "amir@reza.com", debt = 150000.0)
                val c2 = Customer(name = "مریم احمدی", phone = "09193334455", email = "maryam@ahmadi.com", debt = 0.0)
                val c3 = Customer(name = "سارا کریمی", phone = "09125556677", email = "sara@karimi.com", debt = 830000.0)
                
                repository.insertCustomer(c1)
                repository.insertCustomer(c2)
                repository.insertCustomer(c3)

                // Insert Sample Staff
                val s1 = Staff(name = "علیرضا نوری", role = "مدیر ارشد و مالک", phone = "09120001111", accessLevel = 2)
                val s2 = Staff(name = "مهدی علیزاده", role = "مدیر انبار", phone = "09352223333", accessLevel = 1)
                val s3 = Staff(name = "زهرا احمدی", role = "مسئول صندوقدار", phone = "09194445555", accessLevel = 0)
                
                repository.insertStaff(s1)
                repository.insertStaff(s2)
                repository.insertStaff(s3)

                // Insert dynamic mock historic sales invoices
                val dayMillis = 24 * 60 * 60 * 1000L
                val now = System.currentTimeMillis()

                // Invoice 1: Today
                val inv1Item1 = InvoiceItem(invoiceId = 0, productId = 1, productName = "زعفران ۱ مثقال ممتاز", quantity = 2, sellPrice = 550000.0, buyPrice = 400000.0)
                val inv1Item2 = InvoiceItem(invoiceId = 0, productId = 3, productName = "روغن سرخ‌کردنی ۱.۵ لیتر", quantity = 3, sellPrice = 98000.0, buyPrice = 75000.0)
                val inv1 = Invoice(
                    customerName = "امیرحسین رضایی",
                    customerPhone = "09121112233",
                    staffName = "زهرا احمدی",
                    dateMillis = now,
                    totalAmount = 1394000.0,
                    totalCost = 1025000.0,
                    discount = 50000.0,
                    finalAmount = 1344000.0,
                    isPaid = true,
                    paymentMethod = "کارتخوان"
                )
                repository.saveInvoiceWithItems(inv1, listOf(inv1Item1, inv1Item2))

                // Invoice 2: Yesterday
                val inv2Item1 = InvoiceItem(invoiceId = 0, productId = 2, productName = "برنج ایرانی طارم ۱۰ کیلو", quantity = 1, sellPrice = 1200000.0, buyPrice = 950000.0)
                val inv2 = Invoice(
                    customerName = "مریم احمدی",
                    customerPhone = "09193334455",
                    staffName = "زهرا احمدی",
                    dateMillis = now - dayMillis,
                    totalAmount = 1200000.0,
                    totalCost = 950000.0,
                    discount = 0.0,
                    finalAmount = 1200000.0,
                    isPaid = true,
                    paymentMethod = "درگاه آنلاین"
                )
                repository.saveInvoiceWithItems(inv2, listOf(inv2Item1))

                // Invoice 3: 3 days ago
                val inv3Item1 = InvoiceItem(invoiceId = 0, productId = 5, productName = "عسل طبیعی سبلان ۱ کیلو", quantity = 2, sellPrice = 295000.0, buyPrice = 220000.0)
                val inv3Item2 = InvoiceItem(invoiceId = 0, productId = 6, productName = "شکلات کادویی تلخ آیدین", quantity = 1, sellPrice = 155000.0, buyPrice = 110000.0)
                val inv3 = Invoice(
                    customerName = "سارا کریمی",
                    customerPhone = "09125556677",
                    staffName = "علیرضا نوری",
                    dateMillis = now - (3 * dayMillis),
                    totalAmount = 745000.0,
                    totalCost = 550000.0,
                    discount = 45000.0,
                    finalAmount = 700000.0,
                    isPaid = true,
                    paymentMethod = "نقد"
                )
                repository.saveInvoiceWithItems(inv3, listOf(inv3Item1, inv3Item2))
            }
        }
    }

    // --- Product Management Actions ---
    fun addProduct(name: String, sku: String, buyPrice: Double, sellPrice: Double, quantity: Int, minQty: Int, category: String) {
        viewModelScope.launch {
            val prod = Product(name = name, sku = sku, buyPrice = buyPrice, sellPrice = sellPrice, quantity = quantity, minQuantity = minQty, category = category)
            repository.insertProduct(prod)
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProductById(id)
        }
    }

    // --- Customer Management Actions ---
    fun addCustomer(name: String, phone: String, email: String, debt: Double) {
        viewModelScope.launch {
            val cust = Customer(name = name, phone = phone, email = email, debt = debt)
            repository.insertCustomer(cust)
        }
    }

    fun deleteCustomer(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomerById(id)
        }
    }

    // --- Staff Management Actions ---
    fun addStaff(name: String, role: String, phone: String, accessLevel: Int) {
        viewModelScope.launch {
            val st = Staff(name = name, role = role, phone = phone, accessLevel = accessLevel)
            repository.insertStaff(st)
        }
    }

    fun deleteStaff(id: Int) {
        viewModelScope.launch {
            repository.deleteStaffById(id)
        }
    }

    // --- Cart / POS Operations ---
    fun addProductToCart(productId: Int, maxQuantity: Int) {
        val currentQty = activeCart.value[productId] ?: 0
        if (currentQty < maxQuantity) {
            val updated = activeCart.value.toMutableMap()
            updated[productId] = currentQty + 1
            activeCart.value = updated
        }
    }

    fun removeProductFromCart(productId: Int) {
        val currentQty = activeCart.value[productId] ?: 0
        if (currentQty > 0) {
            val updated = activeCart.value.toMutableMap()
            if (currentQty == 1) {
                updated.remove(productId)
            } else {
                updated[productId] = currentQty - 1
            }
            activeCart.value = updated
        }
    }

    fun clearCart() {
        activeCart.value = emptyMap()
        selectedCustomerForCart = null
        cartDiscount = 0.0
    }

    // Invoice complete checkout
    fun checkoutCart(paymentMethod: String, isPaid: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val cartList = activeCart.value
            if (cartList.isEmpty()) return@launch

            val allProdList = repository.allProducts.first()
            val invoiceItems = mutableListOf<InvoiceItem>()
            var totalAmt = 0.0
            var totalCostPrice = 0.0

            for ((pId, qty) in cartList) {
                val prod = allProdList.find { it.id == pId } ?: continue
                val sellPrice = prod.sellPrice
                val buyPrice = prod.buyPrice

                totalAmt += sellPrice * qty
                totalCostPrice += buyPrice * qty

                invoiceItems.add(
                    InvoiceItem(
                        invoiceId = 0,
                        productId = pId,
                        productName = prod.name,
                        quantity = qty,
                        sellPrice = sellPrice,
                        buyPrice = buyPrice
                    )
                )
            }

            val finalAmt = (totalAmt - cartDiscount).coerceAtLeast(0.0)

            val invoice = Invoice(
                customerName = selectedCustomerForCart?.name ?: "مشتری متفرقه",
                customerPhone = selectedCustomerForCart?.phone ?: "",
                staffName = activeStaff?.name ?: "ناشناس",
                totalAmount = totalAmt,
                totalCost = totalCostPrice,
                discount = cartDiscount,
                finalAmount = finalAmt,
                isPaid = isPaid,
                paymentMethod = paymentMethod
            )

            repository.saveInvoiceWithItems(invoice, invoiceItems)
            clearCart()
            onSuccess()
        }
    }

    fun refundInvoice(invoiceId: Int) {
        viewModelScope.launch {
            repository.refundInvoice(invoiceId)
        }
    }

    fun updateProductQuantity(id: Int, newQty: Int) {
        viewModelScope.launch {
            repository.updateProductQuantity(id, newQty)
        }
    }

    suspend fun getItemsForInvoice(invoiceId: Int): List<InvoiceItem> {
        return repository.getItemsForInvoice(invoiceId)
    }

    // --- Automatic Cloud Backup Simulation ---
    fun triggerCloudBackup(context: Context) {
        viewModelScope.launch {
            isBackupInProgress = true
            
            // Collect database current models to build realistic backup file content
            val prods = repository.allProducts.first()
            val custs = repository.allCustomers.first()
            val invs = repository.allInvoices.first()
            
            val parentObject = JSONObject()
            val productsArr = JSONArray()
            prods.forEach {
                val jo = JSONObject().put("id", it.id).put("name", it.name).put("sku", it.sku).put("quantity", it.quantity)
                productsArr.put(jo)
            }
            parentObject.put("products", productsArr)
            parentObject.put("backup_timestamp", System.currentTimeMillis())

            // Simulate cloud delay
            kotlinx.coroutines.delay(1800)
            
            isBackupInProgress = false
            val backupTimeStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            lastBackupTime = backupTimeStr
            Toast.makeText(context, "پشتیبان‌گیری ابری با موفقیت انجام شد و آرشیو همگام گردید", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Generate EXCEL (CSV format) reports sheet to share/copy ---
    fun generateSalesCsvReport(invoicesList: List<Invoice>): String {
        val sb = java.lang.StringBuilder()
        // CSV Header row (standard UTF-8 with BOM to display Persian characters correctly in Excel!)
        sb.append('\ufeff') // Byte Order Mark for excel
        sb.append("شناسه فاکتور,تاریخ,مشتری,صندوقدار,مبلغ کل (ریال),تخفیف,مبلغ نهایی,روش پرداخت,سود ناخالص\n")
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        for (inv in invoicesList) {
            val dateStr = sdf.format(java.util.Date(inv.dateMillis))
            val profit = inv.finalAmount - inv.totalCost
            sb.append("${inv.id},")
                .append("${dateStr},")
                .append("\"${inv.customerName.replace("\"", "\"\"")}\",")
                .append("\"${inv.staffName.replace("\"", "\"\"")}\",")
                .append("${inv.totalAmount},")
                .append("${inv.discount},")
                .append("${inv.finalAmount},")
                .append("\"${inv.paymentMethod}\",")
                .append("${profit}\n")
        }
        return sb.toString()
    }
}

class StoreViewModelFactory(private val repository: StoreRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            return StoreViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
