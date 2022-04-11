package com.vroomvroom.android.view.ui.orders.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vroomvroom.android.view.ui.orders.pagerfragment.ConfirmedFragment
import com.vroomvroom.android.view.ui.orders.pagerfragment.DeliveredFragment
import com.vroomvroom.android.view.ui.orders.pagerfragment.PendingFragment
import com.vroomvroom.android.view.ui.orders.pagerfragment.ToReceiveFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            1 -> {
                return ConfirmedFragment()
            }
            2 -> {
                return ToReceiveFragment()
            }
            3 -> {
                return DeliveredFragment()
            }
        }
        return  PendingFragment()
    }
}