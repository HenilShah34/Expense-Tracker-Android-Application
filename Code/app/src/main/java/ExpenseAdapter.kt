package com.example.expensetrackerfinalfull

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetrackerfinalfull.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private var list: List<Expense>, private val onEdit: (Expense)->Unit, private val onDelete: (Int)->Unit) : RecyclerView.Adapter<ExpenseAdapter.VH>() {
    inner class VH(val b: ItemExpenseBinding): RecyclerView.ViewHolder(b.root) {
        fun bind(e: Expense) {
            b.tvCat.text = e.category
            val sm = SessionManager(b.root.context)
            val (sym, amt) = Utils.convertAmount(e.amount, sm.selectedCurrency)
            b.tvAmount.text = String.format("%s%.2f", sym, amt)
            b.tvNote.text = e.note
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            b.tvDate.text = sdf.format(Date(e.timestamp))
            b.btnEdit.setOnClickListener { onEdit(e) }
            b.btnDelete.setOnClickListener {
                AlertDialog.Builder(b.root.context)
                    .setTitle("Delete expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete") { _, _ -> onDelete(e.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }
    override fun onBindViewHolder(holder: VH, position: Int) { holder.bind(list[position]) }
    override fun getItemCount(): Int = list.size
    fun update(newList: List<Expense>) { list = newList; notifyDataSetChanged() }
}
