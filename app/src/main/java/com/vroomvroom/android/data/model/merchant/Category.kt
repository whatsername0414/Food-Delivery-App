package com.vroomvroom.android.data.model.merchant

import com.google.gson.annotations.SerializedName

data class Category(
    val id: String,
    val name: String,
    val imageUrl: String,
    val type: String
)
