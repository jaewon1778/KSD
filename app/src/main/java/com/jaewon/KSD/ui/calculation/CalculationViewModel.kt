package com.jaewon.KSD.ui.calculation

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.icu.text.DecimalFormat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.R
import com.jaewon.KSD.data.Player
import com.jaewon.KSD.data.ReceiptInfo
import com.jaewon.KSD.databinding.PlayerNameBinding
import com.jaewon.KSD.databinding.PlayerWMoneyBinding
import java.security.PKCS12Attribute

class CalculationViewModel : ViewModel() {

    var nameList = mutableListOf<String>("고", "오", "성", "석", "찬", "영", "김", "이", "박")
    val pnAllList = players(nameList)
    val receiptInfoList = mutableListOf<ReceiptInfo>()

    fun getActivePNList(): MutableList<Player>{
        val activePNList = mutableListOf<Player>()

        for (player in pnAllList){
            if (player.active) activePNList.add(player.copy())
        }

        return activePNList
    }

    fun getCalculatedActivePNList(): MutableList<Player>{
        val newActivePNList = getActivePNList()
        for (receipt in receiptInfoList){
            receipt.calculateReceipt(newActivePNList)
        }

        return newActivePNList
    }

    class PlayerNameAdapter : RecyclerView.Adapter<PlayerNameAdapter.PlayerNameViewHolder>() {
        var pnList : MutableList<Player> = mutableListOf()
        var type : Int = 0

        inner class PlayerNameViewHolder(private val binding: PlayerNameBinding) : RecyclerView.ViewHolder(binding.root){
            @SuppressLint("NotifyDataSetChanged")
            fun bind(player:Player){
                binding.txtPlayerName.text = player.name
            }
            @SuppressLint("NotifyDataSetChanged")
            fun bindForReceiptNBB(player: Player) {
                binding.txtPlayerName.text = player.name
                binding.txtPlayerName.textSize = 27F
                binding.txtPlayerName.setPadding(25,5,25,5)

                if (player.statePay) binding.txtPlayerName.setBackgroundResource(R.drawable.bg_player_name_pay)
                else if (!player.stateOn) binding.txtPlayerName.setBackgroundResource(R.drawable.bg_player_name_off)
                else binding.txtPlayerName.setBackgroundResource(R.drawable.bg_player_name)

                binding.txtPlayerName.setOnClickListener {
                    player.stateOn = !player.stateOn
                    if (player.stateOn) it.setBackgroundResource(R.drawable.bg_player_name)
                    else {
                        it.setBackgroundResource(R.drawable.bg_player_name_off)
                        player.statePay = false
                    }
                    onPNBBChangeListener.onPNBBChanged()
                }
                binding.txtPlayerName.setOnLongClickListener {
                    if (!player.stateOn) return@setOnLongClickListener true
                    for (oPlayer in pnList){
                        oPlayer.statePay = oPlayer.name == player.name
                    }
                    notifyDataSetChanged()

                    return@setOnLongClickListener true
                }
            }
            @SuppressLint("ResourceAsColor")
            fun bindForGameAssignPlayer(player: Player){
                binding.txtPlayerName.text = player.name
                binding.txtPlayerName.textSize = 27F
                binding.txtPlayerName.setPadding(25,5,25,5)

                if (player.gameSurplus) {
                    binding.txtPlayerName.setBackgroundResource(R.drawable.bg_player_name_surplus)
                    binding.txtPlayerName.setTextColor(Color.WHITE)
                }
                else {
                    binding.txtPlayerName.setBackgroundResource(R.drawable.bg_player_name_deficit)
                    binding.txtPlayerName.setTextColor(Color.BLACK)

                }

                binding.txtPlayerName.setOnClickListener {
                    player.gameSurplus = !player.gameSurplus
                    if (player.gameSurplus) {
                        it.setBackgroundResource(R.drawable.bg_player_name_surplus)
                        binding.txtPlayerName.setTextColor(Color.WHITE)

                    }
                    else {
                        it.setBackgroundResource(R.drawable.bg_player_name_deficit)
                        binding.txtPlayerName.setTextColor(Color.BLACK)

                    }
                }

            }
            fun bindForSettingPlayer(){

            }

        }
        interface PNBBChangeListener {
            fun onPNBBChanged()
        }
        private lateinit var onPNBBChangeListener : PNBBChangeListener

        fun setOnPNBBChangeListener (listener: PNBBChangeListener){
            onPNBBChangeListener = listener
        }


        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PlayerNameAdapter.PlayerNameViewHolder {
            return PlayerNameViewHolder(PlayerNameBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }

        override fun onBindViewHolder(
            holder: PlayerNameAdapter.PlayerNameViewHolder,
            position: Int
        ) {
            when (type){
                0 -> holder.bind(pnList[position])
                1 -> holder.bindForReceiptNBB(pnList[position])
                2 -> holder.bindForGameAssignPlayer(pnList[position])
            }

        }

        override fun getItemCount(): Int {
            return pnList.size
        }

    }
    class PlayerWithMoneyAdapter : RecyclerView.Adapter<PlayerWithMoneyAdapter.PlayerWithMoneyViewHolder>() {
        var pnList : MutableList<Player> = mutableListOf()

        inner class PlayerWithMoneyViewHolder(private val binding: PlayerWMoneyBinding) : RecyclerView.ViewHolder(binding.root){
            @SuppressLint("NotifyDataSetChanged")
            fun bind(player:Player){
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

    class PNRecyclerViewDecoration(val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.left = space
            outRect.bottom = space
        }
    }
    fun players(nameList: MutableList<String>):MutableList<Player>{
        val pnList : MutableList<Player> = mutableListOf()
        for(name in nameList){
            pnList.add(Player(name))
        }
        return pnList
    }

}