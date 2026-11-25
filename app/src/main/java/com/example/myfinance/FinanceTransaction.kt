package com.example.myfinance

data class FinanceTransaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String,
    var amount: Double,
    var category: String,
    var type: String,
    var date: String,
    var description: String = ""
)