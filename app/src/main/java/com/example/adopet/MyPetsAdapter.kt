package com.example.adopet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyPetsAdapter(
    private var items: MutableList<Pet>,
    private val onItemClick: (Pet) -> Unit,
    private val onFavoriteClick: (Pet, Int) -> Unit
) : RecyclerView.Adapter<MyPetsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.petName)
        val txtType: TextView = view.findViewById(R.id.petType)
        val imgPet: ImageView = view.findViewById(R.id.imgPet)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_mylist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = items[position]

        holder.txtName.text = pet.petName
        holder.txtType.text = "${pet.type} - ${pet.city}"

        if (pet.imageUrl.isNotBlank()) {
            GlideApp.with(holder.itemView.context).load(pet.imageUrl).centerCrop().into(holder.imgPet)
        } else {
            holder.imgPet.setImageResource(R.drawable.ic_launcher_foreground) 
        }

        holder.btnFavorite.setImageResource(R.drawable.ic_favorite)

        holder.itemView.setOnClickListener { onItemClick(pet) }
        holder.btnFavorite.setOnClickListener { onFavoriteClick(pet, position) }
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateList(newList: List<Pet>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
