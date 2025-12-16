package com.example.adopet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyPetsAdapter(
    private val items: List<Pet>,
    private val favoritePetIds: Set<String>,      // YENİ: Favori ilanların ID'lerini tutar
    private val onItemClick: (Pet) -> Unit,       // onClick -> onItemClick olarak yeniden adlandırıldı
    private val onFavoriteClick: (Pet) -> Unit  // YENİ: Favori butonu tıklama fonksiyonu
) : RecyclerView.Adapter<MyPetsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.petName)
        val txtType: TextView = view.findViewById(R.id.petType)
        val imgPet: ImageView = view.findViewById(R.id.imgPet)
        val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite) // YENİ: Favori butonu
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
        holder.txtType.text = "${pet.type} - ${pet.city}" // Şehir bilgisi eklendi

        // Resmi yükle
        if (pet.imageUrl.isNotBlank()) {
            Glide.with(holder.itemView.context).load(pet.imageUrl).centerCrop().into(holder.imgPet)
        } else {
            holder.imgPet.setImageResource(R.drawable.ic_launcher_foreground) 
        }

        // Favori durumuna göre yıldız ikonunu ayarla
        if (favoritePetIds.contains(pet.id)) {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off)
        }

        // Tıklama dinleyicilerini ayarla
        holder.itemView.setOnClickListener { onItemClick(pet) }
        holder.btnFavorite.setOnClickListener { onFavoriteClick(pet) }
    }
}
