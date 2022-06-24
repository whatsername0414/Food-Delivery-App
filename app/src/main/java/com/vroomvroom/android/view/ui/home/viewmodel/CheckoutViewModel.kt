package com.vroomvroom.android.view.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroom.android.data.model.cart.CartItemWithOptions
import com.vroomvroom.android.data.model.order.Order
import com.vroomvroom.android.data.model.order.Payment
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.repository.order.OrderRepository
import com.vroomvroom.android.view.state.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _order by lazy { MutableLiveData<ViewState<String>>() }
    val order: LiveData<ViewState<String>>
        get() = _order
    val isLocationConfirmed by lazy { MutableLiveData(false) }
    var subtotal = 0.0
    var isComputed = false

    fun createOrder(
        merchantId: String,
        payment: Payment,
        deliveryFee: Double,
        totalPrice: Double,
        locationEntity: LocationEntity,
        cartItems: List<CartItemWithOptions>
    ) {
        _order.postValue(ViewState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = orderRepository.createOrders(
                merchantId, payment, deliveryFee, totalPrice, locationEntity, cartItems)
            response?.let { data ->
                when (data) {
                    is ViewState.Success -> {
                        _order.postValue(data)
                    }
                    is ViewState.Error -> {
                        _order.postValue(data)
                    }
                    else -> {
                        _order.postValue(data)
                    }
                }
            }
        }
    }

}