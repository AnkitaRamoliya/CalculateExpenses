package com.androideditors.expensemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val categories = listOf("All", "Food", "Travel", "Shopping", "Bills", "Other")
private val purple = Color(0xFF6750A4)
private val indianCurrency = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
private fun money(paise: Long): String = indianCurrency.format(BigDecimal.valueOf(paise, 2))
private fun iconFor(category: String) = when (category) {
    "Food" -> "🍔"; "Travel" -> "🚗"; "Shopping" -> "🛍️"; "Bills" -> "🧾"; else -> "💰"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = ExpenseDatabase(applicationContext)
        setContent {
            val colorScheme = lightColorScheme(primary = purple, background = Color(0xFFF8F7FC))
            MaterialTheme(colorScheme = colorScheme) {
                var expenses by remember { mutableStateOf(db.list()) }
                var category by remember { mutableStateOf("All") }
                var editing by remember { mutableStateOf<Expense?>(null) }
                var showEditor by remember { mutableStateOf(false) }
                var deleting by remember { mutableStateOf<Expense?>(null) }
                fun reload() { expenses = db.list() }

                Scaffold(
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { editing = null; showEditor = true },
                            icon = { Icon(Icons.Default.Add, contentDescription = null) },
                            text = { Text("Add expense") }
                        )
                    }
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                        Spacer(Modifier.height(24.dp))
                        Text("ExpenseMate", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Your daily spending, organized.", color = Color.Gray)
                        Spacer(Modifier.height(24.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = purple), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                            Column(Modifier.padding(24.dp)) {
                                Text("TOTAL SPENDING", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.height(8.dp))
                                Text(money(expenses.sumOf { it.amountPaise }), style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("${expenses.size} transactions recorded", color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { item ->
                                FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) })
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Expense history", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            val filtered = expenses.filter { category == "All" || it.category == category }
                            Text("${filtered.size} records", color = Color.Gray)
                        }
                        Spacer(Modifier.height(10.dp))
                        val filtered = expenses.filter { category == "All" || it.category == category }
                        if (filtered.isEmpty()) {
                            Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(54.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("No expenses here yet", style = MaterialTheme.typography.titleMedium)
                                Text("Tap Add expense to create one.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(filtered, key = { it.id }) { expense ->
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(iconFor(expense.category), style = MaterialTheme.typography.headlineMedium)
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(expense.title, fontWeight = FontWeight.SemiBold)
                                                Text("${expense.category} • ${SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(expense.createdAt))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                Text(money(expense.amountPaise), fontWeight = FontWeight.Bold, color = purple)
                                            }
                                            IconButton(onClick = { editing = expense; showEditor = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit ${expense.title}")
                                            }
                                            IconButton(onClick = { deleting = expense }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete ${expense.title}", tint = Color(0xFFB3261E))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (showEditor) ExpenseEditor(editing, onDismiss = { showEditor = false }, onSave = { title, paise, selected ->
                    db.save(title, paise, selected, editing?.id)
                    reload()
                    showEditor = false
                })
                deleting?.let { expense ->
                    AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Delete expense?") },
                        text = { Text("Remove '${expense.title}' permanently?") },
                        confirmButton = { TextButton(onClick = { db.delete(expense.id); reload(); deleting = null }) { Text("Delete") } },
                        dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } })
                }
            }
        }
    }
}

@Composable
private fun ExpenseEditor(expense: Expense?, onDismiss: () -> Unit, onSave: (String, Long, String) -> Unit) {
    var title by remember(expense?.id) { mutableStateOf(expense?.title ?: "") }
    var amount by remember(expense?.id) { mutableStateOf(expense?.let { BigDecimal.valueOf(it.amountPaise, 2).stripTrailingZeros().toPlainString() } ?: "") }
    var selected by remember(expense?.id) { mutableStateOf(expense?.category ?: "Food") }
    val paise = remember(amount) {
        try {
            val value = BigDecimal(amount.trim()).setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact()
            value.takeIf { it > 0L }
        } catch (_: Exception) { null }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (expense == null) "Add expense" else "Edit expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Expense title") }, singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, isError = amount.isNotEmpty() && paise == null)
                Text("Category", fontWeight = FontWeight.SemiBold)
                categories.drop(1).chunked(3).forEach { group ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        group.forEach { item ->
                            FilterChip(selected = selected == item, onClick = { selected = item }, label = { Text(item) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank() && paise != null, onClick = { paise?.let { onSave(title.trim(), it, selected) } }) {
                Text(if (expense == null) "Save" else "Update")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
