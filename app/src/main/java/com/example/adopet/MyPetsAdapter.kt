package com.example.adopet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Adaptör artık Map yerine güvenli Pet nesneleriyle çalışıyor.
class MyPetsAdapter(
    private val items: List<Pet>,              // <-- List<Pet> olarak değiştirildi
    private val onClick: (Pet) -> Unit         // <-- (Pet) -> Unit olarak değiştirildi
) : RecyclerView.Adapter<MyPetsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Not: item_pet_mylist.xml dosyanızda bu ID'lerin olduğundan emin olun.
        val txtName: TextView = view.findViewById(R.id.petName)
        val txtType: TextView = view.findViewById(R.id.petType)
        // Resim için bir ImageView eklediğinizi varsayıyorum.
        val imgPet: ImageView = view.findViewById(R.id.imgPet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_mylist, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = items[position]

        // Verilere artık doğrudan ve güvenli bir şekilde erişiyoruz
        holder.txtName.text = pet.petName
        holder.txtType.text = pet.type

        // Glide ile resmi yüklüyoruz
        if (pet.imageUrl.isNotBlank()) {
            Glide.with(holder.itemView.context)
                .load(pet.imageUrl)
                .centerCrop()
                // Projenize uygun bir placeholder ekleyin
                .placeholder(R.drawable.ic_launcher_foreground) 
                .into(holder.imgPet)
        } else {
            // Resim yoksa varsayılan bir görsel göster
            holder.imgPet.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.itemView.setOnClickListener { onClick(pet) }
    }
}
