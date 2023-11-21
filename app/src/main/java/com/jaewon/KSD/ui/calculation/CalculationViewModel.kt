package com.jaewon.KSD.ui.calculation

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.Rect
import android.icu.text.DecimalFormat
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.marginTop
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.R
import com.jaewon.KSD.data.Player
import com.jaewon.KSD.databinding.PlayerNameBinding
import com.jaewon.KSD.databinding.PlayerWMoneyBinding

class CalculationViewModel : ViewModel() {

    var nameList = mutableListOf<String>("고", "오", "성", "석", "찬", "영", "김", "이", "박")
    val pnAllList = players(nameList)

    fun makeComma(input:Int): String? {
        val formatter = DecimalFormat("###,###")
        return formatter.format(input)
    }

    fun commaUpgrade(editText: EditText) {

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(s.toString()) && !s.toString().equals(editText.text)) {
                    editText.setText(makeComma(Integer.parseInt(s.toString().replace(",", ""))))
                    editText.setSelection(editText.text.length)  //커서를 오른쪽 끝으로 보냄
                }
            }

        })
    }

    fun getActivePNList(): MutableList<Player>{
        val activePNList = mutableListOf<Player>()

        for (player in pnAllList){
            if (player.active) activePNList.add(player.copy())
        }

        return activePNList
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
                var pointNumStr = ""
                binding.edtPwmAmount.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (!TextUtils.isEmpty(s.toString()) && !s.toString().equals(pointNumStr)) {
                            pointNumStr = DecimalFormat("###,###").format(Integer.parseInt(s.toString().replace(",", "")))
                            binding.edtPwmAmount.setText(pointNumStr)
                            binding.edtPwmAmount.setSelection(pointNumStr.length)  //커서를 오른쪽 끝으로 보냄
                        }
                    }

                })
            }
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