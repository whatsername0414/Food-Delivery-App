package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentOrderDetailBinding
import com.vroomvroom.android.data.model.order.OrderDto
import com.vroomvroom.android.utils.Constants.SUCCESS
import com.vroomvroom.android.utils.Constants.FORMAT_DD_MMM_YYYY_HH_MM_SS
import com.vroomvroom.android.utils.Utils.formatStringToDate
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.orders.adapter.OrderProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OrderDetailFragment : BaseFragment<FragmentOrderDetailBinding>(
    FragmentOrderDetailBinding::inflate
) {

    private val args: OrderDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>(SUCCESS)
            ?.observe(currentBackStackEntry) { isCancelled ->
                if (isCancelled) navController.popBackStack()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.toolbar.apply {
            setupToolbar()
            setNavigationOnClickListener {
                if (prevDestinationId == R.id.commonCompleteFragment) {
                    navController.safeNavigate(R.id.action_orderDetailFragment_to_homeFragment)
                } else {
                    navController.popBackStack()
                }
            }
        }
        ordersViewModel.getOrder(args.id)
        observeOrder()
        observeReviewed()
        onBackPressed()

    }

    private fun observeOrder() {
        ordersViewModel.order.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.orderDetailLayout.visibility = View.GONE
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                }
                is ViewState.Success -> {
                    binding.commonNoticeLayout.hideNotice()
                    binding.orderDetailLayout.visibility = View.VISIBLE
                    val order = response.data
                    ordersViewModel.merchantId = order.merchant.id
                    updateButtonModify(order)
                    updateViewsOnDataReady(order)
                    binding.orderMerchantLayout.setOnClickListener {
                        navController.navigate(
                            OrderDetailFragmentDirections.actionGlobalToMerchantFragment(
                                order.merchant.id
                            )
                        )
                    }
                }
                is ViewState.Error -> {
                    binding.orderDetailLayout.visibility = View.GONE
                    binding.shimmerLayout.visibility = View.GONE
                    binding.shimmerLayout.stopShimmer()
                    binding.commonNoticeLayout.showNetworkError {
                        ordersViewModel.getOrder(args.id)
                    }
                }
            }
        }
    }

    private fun updateButtonModify(order: OrderDto) {
        when (order.status) {
            "Pending" -> {
                binding.btnModifyOrder.text = getString(R.string.cancel)
                binding.btnModifyOrder.setOnClickListener {
                    navController.navigate(
                        OrderDetailFragmentDirections.actionOrderDetailFragmentToCancelBottomSheetFragment(order.id)
                    )
                }
            }
            "Confirmed" -> {
                binding.btnModifyOrder.text = getString(R.string.change_address)
                binding.btnModifyOrder.setOnClickListener {
                    navController.navigate(
                        OrderDetailFragmentDirections.actionOrderDetailFragmentToAddressesFragment(order.id)
                    )
                }
            }
            "Delivered" -> {
                if (order.reviewed) {
                    binding.btnModifyOrder.visibility = View.GONE
                } else {
                    binding.btnModifyOrder.text = getString(R.string.write_review)
                    binding.btnModifyOrder.setOnClickListener {
                        reviewNavigation(order.id)
                    }
                    reviewNavigation(order.id)
                }
            }
            else -> {
                binding.btnModifyOrder.visibility = View.GONE
            }
        }
    }

    private fun observeReviewed() {
        mainActivityViewModel.reviewed.observe(viewLifecycleOwner) { reviewed ->
            if (reviewed) {
                binding.btnModifyOrder.visibility = View.GONE
            }
        }
    }

    private fun reviewNavigation(orderId: String) {
        navController.navigate(
            OrderDetailFragmentDirections
                .actionOrderDetailFragmentToReviewBottomSheetFragment(ordersViewModel.merchantId, orderId)
        )
    }

    private fun updateViewsOnDataReady(order: OrderDto) {
        binding.order = order
        binding.subTotalTv.text =
            getString(R.string.peso, "%.2f".format(order.orderDetail.totalPrice))
        binding.deliveryFee.text =
            getString(R.string.peso, "%.2f".format(order.orderDetail.deliveryFee))
        val total = order.orderDetail.totalPrice + order.orderDetail.deliveryFee
        binding.totalTv.text = getString(R.string.peso, "%.2f".format(total))
        binding.orderProductRv.adapter = OrderProductAdapter(order.orderDetail.product)
        binding.placedDate.text =
            getString(R.string.placed_on,
                formatStringToDate(order.createdAt, FORMAT_DD_MMM_YYYY_HH_MM_SS))
        binding.shimmerLayout.visibility = View.GONE
        binding.shimmerLayout.stopShimmer()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (prevDestinationId == R.id.commonCompleteFragment) {
                        navController.safeNavigate(R.id.action_orderDetailFragment_to_homeFragment)
                    } else {
                        navController.popBackStack()
                    }
                }
            })
    }
}