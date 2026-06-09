package com.example.data

import kotlinx.coroutines.flow.Flow

class StoreRepository(private val storeDao: StoreDao) {

    // --- Products ---
    val allProducts: Flow<List<Product>> = storeDao.getAllProducts()

    suspend fun getProductById(id: Int): Product? = storeDao.getProductById(id)
    suspend fun getProductBySku(sku: String): Product? = storeDao.getProductBySku(sku)
    suspend fun insertProduct(product: Product) = storeDao.insertProduct(product)
    suspend fun deleteProductById(id: Int) = storeDao.deleteProductById(id)
    suspend fun updateProductQuantity(id: Int, newQty: Int) = storeDao.updateProductQuantity(id, newQty)


    // --- Customers ---
    val allCustomers: Flow<List<Customer>> = storeDao.getAllCustomers()

    suspend fun insertCustomer(customer: Customer) = storeDao.insertCustomer(customer)
    suspend fun deleteCustomerById(id: Int) = storeDao.deleteCustomerById(id)


    // --- Staff ---
    val allStaff: Flow<List<Staff>> = storeDao.getAllStaff()

    suspend fun insertStaff(staff: Staff) = storeDao.insertStaff(staff)
    suspend fun deleteStaffById(id: Int) = storeDao.deleteStaffById(id)


    // --- Invoices ---
    val allInvoices: Flow<List<Invoice>> = storeDao.getAllInvoices()

    suspend fun getInvoiceById(id: Int): Invoice? = storeDao.getInvoiceById(id)
    suspend fun insertInvoice(invoice: Invoice) = storeDao.insertInvoice(invoice)
    suspend fun deleteInvoiceById(id: Int) = storeDao.deleteInvoiceById(id)


    // --- Combined Transactions ---
    suspend fun getItemsForInvoice(invoiceId: Int): List<InvoiceItem> = storeDao.getItemsForInvoice(invoiceId)
    
    suspend fun saveInvoiceWithItems(invoice: Invoice, items: List<InvoiceItem>) {
        storeDao.saveInvoiceWithItems(invoice, items)
    }

    suspend fun refundInvoice(invoiceId: Int) {
        storeDao.refundInvoice(invoiceId)
    }
}
