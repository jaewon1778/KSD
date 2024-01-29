package com.jaewon.KSD.ui.calculation

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.data.Player
import com.jaewon.KSD.databinding.PlayerWMoneyBinding

class PlayerWithMoneyAdapter : RecyclerView.Adapter<PlayerWithMoneyAdapter.PlayerWithMoneyViewHolder>() {
    var pnList : MutableList<Player> = mutableListOf()

    inner class PlayerWithMoneyViewHolder(private val binding: PlayerWMoneyBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("NotifyDataSetChanged")
        fun bind(player: Player){
            binding.txtPwmName.text = player.name
            var pointNumInt = player.amount
            var pointNumStr = DecimalFormat("###,###,###").format(pointNumInt)
            if (pointNumInt != 0){
                binding.edtPwmAmount.setText(pointNumStr)
            }
            binding.edtPwmAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (TextUtils.isEmpty(s.toString())){
                        pointNumInt = 0
                        pointNumStr = ""
                    }
                    if (s.toString() != pointNumStr) {
                        pointNumInt = Integer.parseInt(s.toString().replace(",", ""))
                        pointNumStr = DecimalFormat("###,###,###").format(pointNumInt)
                        binding.edtPwmAmount.setText(pointNumStr)
                        binding.edtPwmAmount.setSelection(pointNumStr.length)  //커서를 오른쪽 끝으로 보냄
                    }
                    player.amount = pointNumInt
                    onPMChangeListener.onPMChanged()
                }

            })
        }

    }

    interface PMChangeListener {
        fun onPMChanged()
    }
    private lateinit var onPMChangeListener : PMChangeListener

    fun setOnPMChangeListener (listener: PMChangeListener){
        onPMChangeListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerWithMoneyViewHolder {
        return PlayerWithMoneyViewHolder(PlayerWMoneyBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(
        holder: PlayerWithMoneyAdapter.PlayerWithMoneyViewHolder,
        position: Int
    ) {
        holder.bind(pnList[position])
    }

    override fun getItemCount(): Int {
        return pnList.size
    }

}