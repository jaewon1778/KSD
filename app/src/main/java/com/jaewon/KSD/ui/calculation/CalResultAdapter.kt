package com.jaewon.KSD.ui.calculation

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.databinding.CalculationResultOneBinding

class CalResultAdapter : RecyclerView.Adapter<CalResultAdapter.CalResultViewHolder>() {
    var calResultInfoList = mutableListOf<Triple<String,String,Int>>()
    inner class CalResultViewHolder(private val binding: CalculationResultOneBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("NotifyDataSetChanged")
        fun bind(resultInfo: Triple<String, String, Int>){
            binding.txtCalResOne.text = resultInfo.first
            binding.txtCalResOther.text = resultInfo.second
            binding.txtCalResAmount.text = DecimalFormat("###,###,###").format(resultInfo.third)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalResultViewHolder {

        return CalResultViewHolder(CalculationResultOneBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return calResultInfoList.size
    }

    override fun onBindViewHolder(holder: CalResultViewHolder, position: Int) {
        holder.bind(calResultInfoList[position])
    }

}