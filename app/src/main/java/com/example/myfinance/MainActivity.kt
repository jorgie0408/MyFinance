package com.example.myfinance

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var storage: FinanceStorage

    private lateinit var contentLayout: LinearLayout

    private val transactions = mutableListOf<FinanceTransaction>()
    private val categories = mutableListOf<String>()

    private val localeID = Locale("id", "ID")

    private var currentPage = "dashboard"

    private fun rupiah(amount: Double): String {
        return NumberFormat.getCurrencyInstance(localeID).format(amount)
    }

    private fun today(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        storage = FinanceStorage(this)

        transactions.addAll(storage.getTransactions())
        categories.addAll(storage.getCategories())

        contentLayout = findViewById(R.id.contentLayout)

        findViewById<Button>(R.id.btnDashboard).setOnClickListener {
            showDashboard()
        }

        findViewById<Button>(R.id.btnCategory).setOnClickListener {
            showCategories()
        }

        findViewById<Button>(R.id.btnIncome).setOnClickListener {
            showIncome()
        }

        findViewById<Button>(R.id.btnExpense).setOnClickListener {
            showExpense()
        }

        findViewById<Button>(R.id.btnTransaction).setOnClickListener {
            showTransactions()
        }

        findViewById<Button>(R.id.btnBalance).setOnClickListener {
            showBalance()
        }

        findViewById<Button>(R.id.btnReport).setOnClickListener {
            showReport()
        }

        showDashboard()
    }

    // ==========================================
    // FUNGSI DASAR
    // ==========================================

    private fun saveData() {
        storage.saveTransactions(transactions)
        storage.saveCategories(categories)
    }

    private fun totalIncome(): Double {
        return transactions
            .filter { it.type == "INCOME" }
            .sumOf { it.amount }
    }

    private fun totalExpense(): Double {
        return transactions
            .filter { it.type == "EXPENSE" }
            .sumOf { it.amount }
    }

    private fun currentBalance(): Double {
        return totalIncome() - totalExpense()
    }

    private fun clearContent(title: String) {
        contentLayout.removeAllViews()

        val heading = TextView(this).apply {
            text = title
            textSize = 22f
            setTextColor(android.graphics.Color.rgb(23, 107, 80))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 10, 0, 18)
        }

        contentLayout.addView(heading)
    }

    private fun addText(
        value: String,
        size: Float = 16f,
        color: Int = android.graphics.Color.DKGRAY,
        bold: Boolean = false
    ) {
        val textView = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            setPadding(8, 10, 8, 10)
        }

        contentLayout.addView(textView)
    }

    private fun addButton(
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = title
            isAllCaps = false
            setOnClickListener { action() }
        }

        contentLayout.addView(button)
    }

    private fun addSpace() {
        val space = android.view.View(this)
        space.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            12
        )
        contentLayout.addView(space)
    }

    private fun addCard(
        title: String,
        amount: Double,
        color: Int
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(color)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
        }

        val amountView = TextView(this).apply {
            text = rupiah(amount)
            textSize = 23f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 8, 0, 0)
        }

        card.addView(titleView)
        card.addView(amountView)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 5, 0, 10)

        contentLayout.addView(card, params)
    }

    // ==========================================
    // NOMOR 1: DASHBOARD
    // ==========================================

    private fun showDashboard() {

        currentPage = "dashboard"

        clearContent("Dashboard")

        addText("Selamat datang di MyFinance!")
        addText("Ringkasan keuangan pribadi Anda")

        addSpace()

        addCard(
            "Total Pemasukan",
            totalIncome(),
            android.graphics.Color.rgb(30, 150, 100)
        )

        addCard(
            "Total Pengeluaran",
            totalExpense(),
            android.graphics.Color.rgb(210, 75, 75)
        )

        addCard(
            "Saldo Saat Ini",
            currentBalance(),
            android.graphics.Color.rgb(40, 100, 170)
        )

        addSpace()

        addText(
            "Transaksi Terbaru",
            19f,
            android.graphics.Color.BLACK,
            true
        )

        val recentTransactions = transactions
            .sortedByDescending { it.date }
            .take(5)

        if (recentTransactions.isEmpty()) {
            addText("Belum ada transaksi.")
        } else {
            recentTransactions.forEach {
                addTransactionItem(it)
            }
        }

        addSpace()

        addButton("Tambah Pemasukan") {
            showTransactionDialog("INCOME")
        }

        addButton("Tambah Pengeluaran") {
            showTransactionDialog("EXPENSE")
        }
    }

    // ==========================================
    // NOMOR 2: MASTER KATEGORI
    // ==========================================

    private fun showCategories() {

        currentPage = "category"

        clearContent("Master Kategori")

        addText("Daftar kategori transaksi")

        addButton("Tambah Kategori") {
            showCategoryDialog(null)
        }

        addSpace()

        if (categories.isEmpty()) {
            addText("Belum ada kategori.")
            return
        }

        categories.forEach { category ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val text = TextView(this).apply {
                this.text = category
                textSize = 17f
                setTextColor(android.graphics.Color.BLACK)
                setPadding(10, 12, 10, 12)
            }

            val edit = Button(this).apply {
                this.text = "Edit"
                isAllCaps = false
                setOnClickListener {
                    showCategoryDialog(category)
                }
            }

            val delete = Button(this).apply {
                this.text = "Hapus"
                isAllCaps = false
                setOnClickListener {
                    deleteCategory(category)
                }
            }

            row.addView(
                text,
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            row.addView(edit)
            row.addView(delete)

            contentLayout.addView(row)

            addDivider()
        }
    }

    private fun showCategoryDialog(oldCategory: String?) {

        val input = EditText(this).apply {
            hint = "Nama kategori"
            setText(oldCategory ?: "")
        }

        AlertDialog.Builder(this)
            .setTitle(
                if (oldCategory == null) {
                    "Tambah Kategori"
                } else {
                    "Ubah Kategori"
                }
            )
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->

                val newCategory = input.text.toString().trim()

                if (newCategory.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Nama kategori tidak boleh kosong",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (categories.any {
                        it.equals(newCategory, ignoreCase = true) &&
                                !it.equals(oldCategory, ignoreCase = true)
                    }) {
                    Toast.makeText(
                        this,
                        "Kategori sudah tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                if (oldCategory == null) {

                    categories.add(newCategory)

                } else {

                    val index = categories.indexOf(oldCategory)

                    if (index >= 0) {
                        categories[index] = newCategory
                    }

                    transactions.forEach {
                        if (it.category == oldCategory) {
                            it.category = newCategory
                        }
                    }
                }

                saveData()
                showCategories()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteCategory(category: String) {

        val used = transactions.any {
            it.category == category
        }

        if (used) {
            Toast.makeText(
                this,
                "Kategori sedang digunakan transaksi",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Hapus Kategori")
            .setMessage("Yakin ingin menghapus $category?")
            .setPositiveButton("Hapus") { _, _ ->
                categories.remove(category)
                saveData()
                showCategories()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ==========================================
    // NOMOR 3 DAN 4:
    // INCOME DAN EXPENSE
    // ==========================================

    private fun showIncome() {

        currentPage = "income"

        clearContent("Pemasukan (Income)")

        addText("Kelola seluruh pemasukan Anda.")

        addButton("Tambah Pemasukan") {
            showTransactionDialog("INCOME")
        }

        val data = transactions
            .filter { it.type == "INCOME" }
            .sortedByDescending { it.date }

        addText("Daftar Pemasukan", 19f, android.graphics.Color.BLACK, true)

        if (data.isEmpty()) {
            addText("Belum ada pemasukan.")
        }

        data.forEach {
            addTransactionItem(it)
        }

        addText("Total Pemasukan: ${rupiah(totalIncome())}", 18f, android.graphics.Color.rgb(30, 150, 100), true)
    }

    private fun showExpense() {

        currentPage = "expense"

        clearContent("Pengeluaran (Expense)")

        addText("Kelola seluruh pengeluaran Anda.")

        addButton("Tambah Pengeluaran") {
            showTransactionDialog("EXPENSE")
        }

        val data = transactions
            .filter { it.type == "EXPENSE" }
            .sortedByDescending { it.date }

        addText("Daftar Pengeluaran", 19f, android.graphics.Color.BLACK, true)

        if (data.isEmpty()) {
            addText("Belum ada pengeluaran.")
        }

        data.forEach {
            addTransactionItem(it)
        }

        addText("Total Pengeluaran: ${rupiah(totalExpense())}", 18f, android.graphics.Color.rgb(210, 75, 75), true)
    }

    // ==========================================
    // FORM TAMBAH DAN EDIT TRANSAKSI
    // ==========================================

    private fun showTransactionDialog(
        type: String,
        transaction: FinanceTransaction? = null
    ) {

        if (categories.isEmpty()) {
            Toast.makeText(
                this,
                "Tambahkan kategori terlebih dahulu",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 8)
        }

        val titleInput = EditText(this).apply {
            hint = "Nama transaksi"
            setText(transaction?.title ?: "")
        }

        val amountInput = EditText(this).apply {
            hint = "Jumlah uang"
            inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(
                transaction?.amount?.let {
                    if (it % 1.0 == 0.0) {
                        it.toLong().toString()
                    } else {
                        it.toString()
                    }
                } ?: ""
            )
        }

        val categorySpinner = Spinner(this)

        val categoryAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        categoryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        categorySpinner.adapter = categoryAdapter

        val selectedIndex = categories.indexOf(
            transaction?.category
        )

        if (selectedIndex >= 0) {
            categorySpinner.setSelection(selectedIndex)
        }

        val dateInput = EditText(this).apply {
            hint = "Tanggal (yyyy-MM-dd)"
            setText(transaction?.date ?: today())
        }

        val descriptionInput = EditText(this).apply {
            hint = "Catatan tambahan"
            setText(transaction?.description ?: "")
            minLines = 2
            gravity = Gravity.TOP
        }

        form.addView(titleInput)
        form.addView(amountInput)

        form.addView(
            TextView(this).apply {
                text = "Pilih Kategori"
                setPadding(0, 12, 0, 4)
            }
        )

        form.addView(categorySpinner)
        form.addView(dateInput)
        form.addView(descriptionInput)

        AlertDialog.Builder(this)
            .setTitle(
                if (transaction == null) {
                    if (type == "INCOME") {
                        "Tambah Pemasukan"
                    } else {
                        "Tambah Pengeluaran"
                    }
                } else {
                    "Ubah Transaksi"
                }
            )
            .setView(form)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()
            .apply {

                setOnShowListener {

                    getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {

                            val transactionTitle = titleInput.text.toString().trim()

                            val amount = amountInput.text.toString()
                                .trim()
                                .toDoubleOrNull()

                            val category = categorySpinner.selectedItem
                                .toString()

                            val date = dateInput.text.toString().trim()

                            val description =
                                descriptionInput.text.toString().trim()

                            if (transactionTitle.isEmpty()) {
                                titleInput.error = "Nama transaksi wajib diisi"
                                return@setOnClickListener
                            }

                            if (amount == null || !amount.isFinite() || amount <= 0) {
                                amountInput.error = "Masukkan jumlah uang yang valid"
                                return@setOnClickListener
                            }

                            val validDate = try {
                                val formatter = SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.US
                                )
                                formatter.isLenient = false
                                formatter.parse(date) != null
                            } catch (e: Exception) {
                                false
                            }

                            if (!validDate) {
                                dateInput.error =
                                    "Gunakan format yyyy-MM-dd"
                                return@setOnClickListener
                            }

                            if (transaction == null) {

                                transactions.add(
                                    FinanceTransaction(
                                        title = transactionTitle,
                                        amount = amount,
                                        category = category,
                                        type = type,
                                        date = date,
                                        description = description
                                    )
                                )

                            } else {

                                transaction.title = transactionTitle
                                transaction.amount = amount
                                transaction.category = category
                                transaction.date = date
                                transaction.description = description
                            }

                            saveData()

                            dismiss()

                            refreshPage()
                        }
                }

                show()
            }
    }

    // ==========================================
    // NOMOR 5: DAFTAR TRANSAKSI
    // SEARCH, FILTER, SORTING
    // ==========================================

    private fun showTransactions() {

        currentPage = "transaction"

        clearContent("Daftar Transaksi")

        addText("Cari dan kelola seluruh transaksi.")

        val searchInput = EditText(this).apply {
            hint = "Cari nama transaksi..."
            maxLines = 1
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }

        val filterSpinner = Spinner(this)

        val filterOptions = listOf(
            "Semua",
            "Pemasukan",
            "Pengeluaran"
        )

        filterSpinner.adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            filterOptions
        )

        val sortSpinner = Spinner(this)

        val sortOptions = listOf(
            "Tanggal Terbaru",
            "Tanggal Terlama",
            "Nominal Terbesar",
            "Nominal Terkecil",
            "Nama A-Z"
        )

        sortSpinner.adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions
        )

        contentLayout.addView(searchInput)
        contentLayout.addView(filterSpinner)
        contentLayout.addView(sortSpinner)

        val resultLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        contentLayout.addView(resultLayout)

        fun renderResults() {

            resultLayout.removeAllViews()

            val keyword = searchInput.text.toString()
                .trim()
                .lowercase(Locale.getDefault())

            var data = transactions.filter { transaction ->

                val matchSearch =
                    transaction.title.lowercase(Locale.getDefault())
                        .contains(keyword) ||
                            transaction.category.lowercase(Locale.getDefault())
                                .contains(keyword) ||
                            transaction.description.lowercase(Locale.getDefault())
                                .contains(keyword)

                val matchFilter = when (filterSpinner.selectedItemPosition) {
                    1 -> transaction.type == "INCOME"
                    2 -> transaction.type == "EXPENSE"
                    else -> true
                }

                matchSearch && matchFilter
            }

            data = when (sortSpinner.selectedItemPosition) {

                1 -> data.sortedBy { it.date }

                2 -> data.sortedByDescending { it.amount }

                3 -> data.sortedBy { it.amount }

                4 -> data.sortedBy {
                    it.title.lowercase(Locale.getDefault())
                }

                else -> data.sortedByDescending { it.date }
            }

            val count = TextView(this).apply {
                text = "Ditemukan ${data.size} transaksi"
                textSize = 15f
                setPadding(8, 12, 8, 12)
            }

            resultLayout.addView(count)

            if (data.isEmpty()) {

                resultLayout.addView(
                    TextView(this).apply {
                        text = "Transaksi tidak ditemukan."
                        setPadding(8, 15, 8, 15)
                    }
                )

            } else {

                data.forEach {
                    addTransactionItem(it, resultLayout)
                }
            }
        }

        searchInput.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    renderResults()
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )

        filterSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    renderResults()
                }
            }

        sortSpinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    renderResults()
                }
            }

        renderResults()
    }

    // ==========================================
    // ITEM TRANSAKSI
    // ==========================================

    private fun addTransactionItem(
        transaction: FinanceTransaction,
        target: LinearLayout = contentLayout
    ) {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(
                android.graphics.Color.WHITE
            )
        }

        val color = if (transaction.type == "INCOME") {
            android.graphics.Color.rgb(30, 140, 90)
        } else {
            android.graphics.Color.rgb(200, 60, 60)
        }

        val typeText = if (transaction.type == "INCOME") {
            "PEMASUKAN"
        } else {
            "PENGELUARAN"
        }

        val info = TextView(this).apply {
            text = "${transaction.title}\n" +
                    "$typeText | ${transaction.category}\n" +
                    "Tanggal: ${transaction.date}\n" +
                    "Nominal: ${rupiah(transaction.amount)}\n" +
                    "Catatan: ${transaction.description}"

            textSize = 15f
            setTextColor(color)
            setPadding(5, 5, 5, 10)
        }

        container.addView(info)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val editButton = Button(this).apply {
            text = "Ubah"
            isAllCaps = false
            setOnClickListener {
                showTransactionDialog(
                    transaction.type,
                    transaction
                )
            }
        }

        val deleteButton = Button(this).apply {
            text = "Hapus"
            isAllCaps = false
            setOnClickListener {
                deleteTransaction(transaction)
            }
        }

        actions.addView(
            editButton,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        actions.addView(
            deleteButton,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        container.addView(actions)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 5, 0, 12)

        target.addView(container, params)
    }

    private fun deleteTransaction(
        transaction: FinanceTransaction
    ) {

        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage(
                "Apakah Anda yakin ingin menghapus transaksi ${transaction.title}?"
            )
            .setPositiveButton("Hapus") { _, _ ->

                transactions.removeAll {
                    it.id == transaction.id
                }

                saveData()

                refreshPage()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ==========================================
    // NOMOR 6: SALDO
    // ==========================================

    private fun showBalance() {

        currentPage = "balance"

        clearContent("Saldo Saat Ini")

        addText(
            "Berikut adalah kondisi keuangan Anda.",
            16f
        )

        addCard(
            "Saldo Sekarang",
            currentBalance(),
            android.graphics.Color.rgb(40, 100, 170)
        )

        addCard(
            "Total Pemasukan",
            totalIncome(),
            android.graphics.Color.rgb(30, 150, 100)
        )

        addCard(
            "Total Pengeluaran",
            totalExpense(),
            android.graphics.Color.rgb(210, 75, 75)
        )

        addSpace()

        addText(
            "Rumus perhitungan:",
            17f,
            android.graphics.Color.BLACK,
            true
        )

        addText(
            "${rupiah(totalIncome())} - ${rupiah(totalExpense())}"
        )

        addText(
            "Saldo akhir: ${rupiah(currentBalance())}",
            19f,
            android.graphics.Color.rgb(23, 107, 80),
            true
        )
    }

    // ==========================================
    // NOMOR 7: LAPORAN BULANAN
    // ==========================================

    private fun showReport() {

        currentPage = "report"

        clearContent("Laporan Keuangan")

        addText("Laporan pemasukan dan pengeluaran bulanan.")

        val monthInput = EditText(this).apply {
            hint = "Bulan (yyyy-MM)"
            setText(
                SimpleDateFormat(
                    "yyyy-MM",
                    Locale.US
                ).format(Date())
            )
        }

        contentLayout.addView(monthInput)

        addButton("Tampilkan Laporan") {

            val month = monthInput.text.toString().trim()

            val validMonth = try {
                val formatter = SimpleDateFormat(
                    "yyyy-MM",
                    Locale.US
                )
                formatter.isLenient = false
                formatter.parse(month) != null &&
                        formatter.format(formatter.parse(month)!!) == month
            } catch (e: Exception) {
                false
            }

            if (!validMonth) {
                monthInput.error = "Gunakan format yyyy-MM"
                return@addButton
            }

            renderMonthlyReport(month)
        }

        renderMonthlyReport(
            SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        )
    }

    private fun renderMonthlyReport(month: String) {

        // Hapus hasil laporan sebelumnya,
        // tetapi pertahankan judul dan input bulan.
        while (contentLayout.childCount > 4) {
            contentLayout.removeViewAt(2)
        }

        val monthlyTransactions = transactions.filter {
            it.date.startsWith("$month-")
        }

        val income = monthlyTransactions
            .filter { it.type == "INCOME" }
            .sumOf { it.amount }

        val expense = monthlyTransactions
            .filter { it.type == "EXPENSE" }
            .sumOf { it.amount }

        val balance = income - expense

        addText(
            "Laporan Bulan $month",
            19f,
            android.graphics.Color.BLACK,
            true
        )

        addCard(
            "Pemasukan Bulanan",
            income,
            android.graphics.Color.rgb(30, 150, 100)
        )

        addCard(
            "Pengeluaran Bulanan",
            expense,
            android.graphics.Color.rgb(210, 75, 75)
        )

        addCard(
            "Selisih Bulanan",
            balance,
            android.graphics.Color.rgb(40, 100, 170)
        )

        addText("Jumlah transaksi: ${monthlyTransactions.size}")

        addText(
            "Detail Transaksi",
            18f,
            android.graphics.Color.BLACK,
            true
        )

        if (monthlyTransactions.isEmpty()) {
            addText("Tidak ada transaksi pada bulan ini.")
        } else {
            monthlyTransactions
                .sortedByDescending { it.date }
                .forEach {
                    addTransactionItem(it)
                }
        }
    }
    // ==========================================
    // FUNGSI TAMBAHAN
    // ==========================================

    private fun addDivider() {

        val divider = android.view.View(this).apply {
            setBackgroundColor(
                android.graphics.Color.LTGRAY
            )
        }

        contentLayout.addView(
            divider,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
        )
    }

    private fun refreshPage() {

        when (currentPage) {

            "dashboard" -> showDashboard()

            "category" -> showCategories()

            "income" -> showIncome()

            "expense" -> showExpense()

            "transaction" -> showTransactions()

            "balance" -> showBalance()

            "report" -> showReport()

            else -> showDashboard()
        }
    }
}