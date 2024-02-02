package com.jaewon.KSD.ui.calculation

import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.R
import com.jaewon.KSD.databinding.CalculationResultDetailLineBinding

class DetailAdapter : RecyclerView.Adapter<DetailAdapter.DetailLineViewHolder>() {

    var detailInfoList : MutableList<Pair<String,Int>> = mutableListOf()
    var type = 0

    inner class DetailLineViewHolder(private val binding: CalculationResultDetailLineBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(detailInfo: Pair<String,Int>){
            binding.txtCalResDetailLineHead.text = detailInfo.first
            binding.txtCalResDetailLineAmount.text = DecimalFormat("###,###,###").format(detailInfo.second)

            if (type == 1) {
                binding.txtCalResDetailLineHead.setBackgroundResource(R.drawable.bg_player_name)
                binding.txtCalResDetailLineHead.setPadding(15,3,15,3)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailLineViewHolder {
        return DetailLineViewHolder(CalculationResultDetailLineBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return detailInfoList.size
    }

    override fun onBindViewHolder(holder: DetailLineViewHolder, position: Int) {
        holder.bind(detailInfoList[position])
    }
}