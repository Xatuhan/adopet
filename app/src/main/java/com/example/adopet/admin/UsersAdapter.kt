package com.example.adopet.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adopet.databinding.ItemAdminUserBinding

class UsersAdapter(
    private val items: List<AppUser>,
    private val onEdit: (AppUser) -> Unit,
    private val onDeactivate: (AppUser) -> Unit,
    private val onActivate: (AppUser) -> Unit,
    private val onDeleteDoc: (AppUser) -> Unit
) : RecyclerView.Adapter<UsersAdapter.VH>() {

    class VH(val b: ItemAdminUserBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val u = items[position]
        h.b.title.text = u.fullName.ifBlank { "(İsimsiz)" }
        h.b.subtitle.text = "${u.email} • ${u.city}/${u.district} • ${if (u.isActive) "aktif" else "pasif"}"

        h.b.btnEdit.setOnClickListener { onEdit(u) }
        h.b.btnDeactivate.setOnClickListener { onDeactivate(u) }
        h.b.btnActivate.setOnClickListener { onActivate(u) }
        h.b.btnDelete.setOnClickListener { onDeleteDoc(u) }
    }

    override fun getItemCount() = items.size
}
