package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.OrderDao
import com.berling.marketplace.data.local.entities.OrderEntity
import com.berling.marketplace.data.remote.SupabaseApi
import com.berling.marketplace.data.remote.models.CreateOrderRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val api: SupabaseApi
) {

    fun getBuyerOrders(buyerId: String): Flow<List<OrderEntity>> {
        return orderDao.getBuyerOrders(buyerId)
    }

    fun getSellerOrders(sellerId: String): Flow<List<OrderEntity>> {
        return orderDao.getSellerOrders(sellerId)
    }

    suspend fun getOrderById(orderId: String): OrderEntity? {
        return orderDao.getOrderById(orderId)
    }

    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByStatus(status)
    }

    suspend fun createOrder(
        productId: String,
        productTitle: String,
        quantity: Int,
        price: Double,
        buyerId: String,
        sellerId: String
    ): OrderEntity? {
        val order = OrderEntity(
            id = System.currentTimeMillis().toString(),
            productId = productId,
            productTitle = productTitle,
            buyerId = buyerId,
            sellerId = sellerId,
            price = price,
            quantity = quantity,
            status = "pending",
            paymentMethod = "razorpay",
            trackingNumber = "",
            createdAt = System.currentTimeMillis().toString(),
            deliveryDate = "",
            isSynced = false
        )

        // Save to local DB
        orderDao.insertOrder(order)

        // Attempt to sync to remote
        try {
            val request = CreateOrderRequest(
                productId = productId,
                productTitle = productTitle,
                quantity = quantity,
                price = price,
                buyerId = buyerId,
                sellerId = sellerId
            )
            api.createOrder("", request)
            orderDao.updateOrderStatus(order.id, "synced")
        } catch (e: Exception) {
            // Order saved locally, will be synced later
        }

        return order
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        orderDao.updateOrderStatus(orderId, status)
        
        try {
            api.updateOrderStatus("", orderId, mapOf("status" to status))
        } catch (e: Exception) {
            // Status updated locally, will be synced later
        }
    }

    suspend fun getCompletedOrdersCount(buyerId: String): Int {
        return orderDao.getCompletedOrdersCount(buyerId)
    }

    suspend fun getTotalEarnings(sellerId: String): Double {
        return orderDao.getTotalEarnings(sellerId) ?: 0.0
    }
}
