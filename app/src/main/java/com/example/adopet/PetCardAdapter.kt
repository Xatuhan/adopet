package com.example.adopet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class PetCardAdapter(
    private var items: List<Pet>,
    private val onItemClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val petImage: ShapeableImageView = view.findViewById(R.id.ivPetImage)
        val petName: TextView = view.findViewById(R.id.tvPetName)
        val location: TextView = view.findViewById(R.id.tvLocation)
        val description: TextView = view.findViewById(R.id.tvShortDescription)
        val tag1: TextView = view.findViewById(R.id.tag1)
        val tag2: TextView = view.findViewById(R.id.tag2)
        val tag3: TextView = view.findViewById(R.id.tag3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = items[position]

        holder.petName.text = pet.petName
        holder.location.text = "${pet.city} - ${pet.district}".trim()
        holder.description.text = pet.description

        if (pet.imageUrl.isNotBlank()) {
            GlideApp.with(holder.itemView.context).load(pet.imageUrl).into(holder.petImage)
        } else {
            holder.petImage.setImageResource(R.drawable.logo_adopet)
        }

        holder.tag1.text = pet.gender.ifBlank { "Cinsiyet" }
        holder.tag2.text = pet.breed.ifBlank { "Cins" }
        holder.tag3.text = if (pet.age > 0) "${pet.age} yaş" else "Yaş"

        holder.itemView.setOnClickListener { onItemClick(pet) }
    }
    
    fun updateList(newList: List<Pet>) {
        items = newList
        notifyDataSetChanged()
    }
}