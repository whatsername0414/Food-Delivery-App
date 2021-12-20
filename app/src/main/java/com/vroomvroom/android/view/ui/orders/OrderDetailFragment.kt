package com.vroomvroom.android.view.ui.orders

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.vroomvroom.android.OrderQuery
import com.vroomvroom.android.databinding.FragmentOrderDetailBinding
import com.vroomvroom.android.utils.Utils.dateBuilder
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.orders.adapter.OrderDetailProductAdapter
import com.vroomvroom.android.view.ui.orders.viewmodel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OrderDetailFragment : Fragment() {

    private val viewModel by viewModels<OrdersViewModel>()
    private val adapter by lazy { OrderDetailProductAdapter() }
    private val args: OrderDetailFragmentArgs by navArgs()

    private lateinit var binding: FragmentOrderDetailBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailBinding.inflate(inflater)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.orderProductRv.adapter = adapter

        viewModel.queryOrder(args.orderId)
        observeOrder()

        binding.btnRetry.setOnClickListener {
            viewModel.queryOrder(args.orderId)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun observeOrder() {
        viewModel.order.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.orderDetailLayout.visibility = View.GONE
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                }
                is ViewState.Success -> {
                    binding.connectionFailedLayout.visibility = View.GONE
                    binding.orderDetailLayout.visibility = View.VISIBLE
                    val order = response.result.getOrder
                    updateViewsOnDataReady(order)
                    binding.orderMerchantLayout.setOnClickListener {
                        navController.navigate(
                            OrderDetailFragmentDirections.actionOrderDetailFragmentToMerchantFragment(order.merchant._id)
                        )
                    }
                }
                is ViewState.Error -> {
                    binding.orderDetailLayout.visibility = View.GONE
                    binding.shimmerLayout.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.connectionFailedLayout.visibility = View.VISIBLE
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateViewsOnDataReady(order: OrderQuery.GetOrder) {
        binding.order = order
        binding.subTotalTv.text = "₱${"%.2f".format(order.order_detail.total_price)}"
        binding.deliveryFee.text = "₱${"%.2f".format(order.order_detail.delivery_fee)}"
        val total = order.order_detail.total_price + order.order_detail.delivery_fee
        binding.totalTv.text = "₱${"%.2f".format(total)}"
        adapter.submitList(order.order_detail.product)
        binding.placedDate.text = "Placed on: " + dateBuilder(order.createdAt as String)
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmer()
    }
}