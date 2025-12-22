package com.example.adopet.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adopet.Pet
import com.example.adopet.databinding.ItemAdminRowBinding

class ListingsAdapter(
    private val items: List<Pet>,
    private val onEdit: (Pet) -> Unit, // onApprove -> onEdit
    private val onDelete: (Pet) -> Unit  // onReject -> onDelete
) : RecyclerView.Adapter<ListingsAdapter.VH>() {

    class VH(val b: ItemAdminRowBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdminRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val pet = items[position]

        h.b.title.text = pet.petName.ifBlank { "(İsimsiz)" }

        h.b.subtitle.text = "${pet.type} - ${pet.city} - [${pet.status}]"

        // Duruma göre subtitle rengini değiştirelim
        when (pet.status) {
            "approved" -> h.b.subtitle.setTextColor(Color.parseColor("#4CAF50")) // Yeşil
            "pending_approval" -> h.b.subtitle.setTextColor(Color.parseColor("#FFC107")) // Sarı
            "rejected" -> h.b.subtitle.setTextColor(Color.parseColor("#F44336")) // Kırmızı
            "adopted" -> h.b.subtitle.setTextColor(Color.parseColor("#607D8B")) // Gri
            else -> h.b.subtitle.setTextColor(Color.GRAY)
        }


        h.b.btnEdit.text = "Düzenle"
        h.b.btnDelete.text = "Sil"

        h.b.btnEdit.setOnClickListener { onEdit(pet) }
        h.b.btnDelete.setOnClickListener { onDelete(pet) }
    }

    override fun getItemCount() = items.size
}
