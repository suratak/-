package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE sku = :sku")
    suspend fun getProductBySku(sku: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    @Query("UPDATE products SET quantity = :newQty WHERE id = :id")
    suspend fun updateProductQuantity(id: Int, newQty: Int)


    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Int)


    // --- Staff ---
    @Query("SELECT * FROM staff ORDER BY accessLevel DESC, name ASC")
    fun getAllStaff(): Flow<List<Staff>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff): Long

    @Query("DELETE FROM staff WHERE id = :id")
    suspend fun deleteStaffById(id: Int)


    // --- Invoices ---
    @Query("SELECT * FROM invoices ORDER BY dateMillis DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Int): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: Int)


    // --- Invoice Items ---
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: Int): List<InvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItem): Long

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Int)

    // Transaction for saving complete sales invoice and updating stock
    @Transaction
    suspend fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) {
        val invoiceId = insertInvoice(invoice).toInt()
        for (item in items) {
            val invoiceItemWithId = item.copy(invoiceId = invoiceId)
            insertInvoiceItem(invoiceItemWithId)
            
            // Deduct stock levels in warehouse
            val product = getProductById(item.productId)
            if (product != null) {
                val newQty = (product.quantity - item.quantity).coerceAtLeast(0)
                updateProductQuantity(product.id, newQty)
            }
        }
    }
    
    // Transaction for deleting invoice and restoring inventory
    @Transaction
    suspend fun refundInvoice(invoiceId: Int) {
        val invoice = getInvoiceById(invoiceId) ?: return
        val items = getItemsForInvoice(invoiceId)
        for (item in items) {
            val product = getProductById(item.productId)
            if (product != null) {
                // Restore stock
                val restoredQty = product.quantity + item.quantity
                updateProductQuantity(product.id, restoredQty)
            }
        }
        deleteItemsForInvoice(invoiceId)
        deleteInvoiceById(invoiceId)
    }
}
