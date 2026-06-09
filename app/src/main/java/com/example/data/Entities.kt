package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String, // barcode or unique item code
    val buyPrice: Double, // for calculating margins/profits
    val sellPrice: Double,
    val quantity: Int, // warehouse inventory stock level
    val minQuantity: Int = 5, // minimum quantity alert
    val category: String = "عمومی"
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val debt: Double = 0.0 // customer total credit balance
)

@Entity(tableName = "staff")
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // owner, manager, salesperson
    val phone: String = "",
    val accessLevel: Int = 0 // 0: Salesperson (فروشنده), 1: Manager (مدیر انبار), 2: Owner (مدیر ارشد)
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerPhone: String = "",
    val staffName: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val totalAmount: Double, // Sum of sells
    val totalCost: Double, // Sum of buys (cost of goods sold)
    val discount: Double = 0.0,
    val finalAmount: Double, // totalAmount - discount
    val isPaid: Boolean = true,
    val paymentMethod: String = "نقد" // نقد, کارتخوان, درگاه آنلاین
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val sellPrice: Double,
    val buyPrice: Double // Cost price at the exact moment of sale for historic accuracy
)
