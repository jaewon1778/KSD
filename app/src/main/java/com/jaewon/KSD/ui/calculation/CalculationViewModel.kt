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
import com.jaewon.KSD.databinding.CalculationResultOneBinding
import com.jaewon.KSD.databinding.PlayerNameBinding
import com.jaewon.KSD.databinding.PlayerWMoneyBinding

class CalculationViewModel : ViewModel() {

    private var nameList = mutableListOf<String>("고", "오", "성", "석", "찬", "영", "현", "도", "장", "민")
    val pnAllList = string2PlayerList(nameList)
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

                if (player.active) binding.txtPlayerName.setBackgroundResource(R.drawable.bg_player_name)
                else binding.txtPlayerName.setBackgroundResource(R.drawable.bg_player_name_off)

                binding.txtPlayerName.setOnClickListener {
                    player.active = !player.active
                    if (player.active) it.setBackgroundResource(R.drawable.bg_player_name)
                    else {
                        it.setBackgroundResource(R.drawable.bg_player_name_off)
                    }
                    onActivePChangeListener.onActivePChanged(player)
                }
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

        }
        interface PNBBChangeListener {
            fun onPNBBChanged()
        }
        private lateinit var onPNBBChangeListener : PNBBChangeListener

        fun setOnPNBBChangeListener (listener: PNBBChangeListener){
            onPNBBChangeListener = listener
        }

        interface ActivePChangeListener {
            fun onActivePChanged(player: Player)
        }
        private lateinit var onActivePChangeListener: ActivePChangeListener
        fun setOnActivePChangeListener (listener: ActivePChangeListener){
            onActivePChangeListener = listener
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
//                3 -> holder.bindForSettingPlayer(pnList[position])
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
    fun string2PlayerList(nameList: MutableList<String>):MutableList<Player>{
        val pnList : MutableList<Player> = mutableListOf()
        for(name in nameList){
            pnList.add(Player(name))
        }
        return pnList
    }

}