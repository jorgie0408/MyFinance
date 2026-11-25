package com.example.myfinance

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FinanceStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "MyFinancePreferences",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    companion object {
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_CATEGORIES = "categories"
    }

    fun saveTransactions(data: List<FinanceTransaction>) {
        val json = gson.toJson(data)

        preferences.edit()
            .putString(KEY_TRANSACTIONS, json)
            .apply()
    }

    fun getTransactions(): MutableList<FinanceTransaction> {
        val json = preferences.getString(KEY_TRANSACTIONS, null)
            ?: return mutableListOf()

        return try {
            val type = object : TypeToken<MutableList<FinanceTransaction>>() {}.type
            gson.fromJson<MutableList<FinanceTransaction>>(json, type)
                ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveCategories(data: List<String>) {
        val json = gson.toJson(data)

        preferences.edit()
            .putString(KEY_CATEGORIES, json)
            .apply()
    }

    fun getCategories(): MutableList<String> {
        val json = preferences.getString(KEY_CATEGORIES, null)

        if (json == null) {
            return mutableListOf(
                "Gaji",
                "Bonus",
                "Makanan",
                "Transportasi",
                "Belanja",
                "Tagihan",
                "Hiburan",
                "Lainnya"
            )
        }

        return try {
            val type = object : TypeToken<MutableList<String>>() {}.type
            gson.fromJson<MutableList<String>>(json, type)
                ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }
}