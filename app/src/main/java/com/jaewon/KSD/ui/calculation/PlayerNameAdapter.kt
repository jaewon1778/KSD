package com.jaewon.KSD.ui.calculation

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jaewon.KSD.R
import com.jaewon.KSD.data.Player
import com.jaewon.KSD.databinding.PlayerNameBinding

class PlayerNameAdapter : RecyclerView.Adapter<PlayerNameAdapter.PlayerNameViewHolder>() {
    var pnList : MutableList<Player> = mutableListOf()
    var type : Int = 0

    inner class PlayerNameViewHolder(private val binding: PlayerNameBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("NotifyDataSetChanged")
        fun bind(player: Player){
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