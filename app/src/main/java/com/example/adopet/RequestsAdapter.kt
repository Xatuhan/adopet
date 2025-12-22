package com.example.adopet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adopet.databinding.ItemRequestBinding

class RequestsAdapter(
    private val items: List<AdoptionRequest>,
    private val isIncoming: Boolean,
    private val onAccept: (AdoptionRequest) -> Unit = {},
    private val onReject: (AdoptionRequest) -> Unit = {}
) : RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = items[position]
        with(holder.binding) {
            tvPetName.text = request.petName
            tvStatus.text = "Durum: ${request.status}"

            if (isIncoming) {
                tvRequesterInfo.text = "Başvuran: ${request.requesterName}"

                if (request.status == "pending") {
                    layoutButtons.visibility = View.VISIBLE
                    btnAccept.setOnClickListener { onAccept(request) }
                    btnReject.setOnClickListener { onReject(request) }
                } else {
                    layoutButtons.visibility = View.GONE
                }
            } else {
                tvRequesterInfo.visibility = View.GONE
                layoutButtons.visibility = View.GONE
            }

            if (request.petImageUrl.isNotBlank()) {
                Glide.with(root.context).load(request.petImageUrl).into(ivPetPhoto)
            } else {
                ivPetPhoto.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }
}
