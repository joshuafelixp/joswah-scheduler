package com.joswah.scheduler.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.joswah.scheduler.R

class RoomAdapter (private val context : Context, private val roomList : ArrayList<Room>): RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_room,parent,false)
        return RoomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val currentItem = roomList[position]
        val room_name = currentItem.room_name
        val capacity = currentItem.capacity.toString()
        holder.roomName.text = "Ruangan : $room_name"
        holder.roomCapacity.text = "Kapasitas : $capacity"
        Glide.with(context)
            .load(currentItem.image)
            .into(holder.roomImage)
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    class RoomViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        val roomName : TextView = itemView.findViewById(R.id.roomName)
        val roomCapacity : TextView = itemView.findViewById(R.id.roomCapacity)
        val roomImage : ImageView = itemView.findViewById(R.id.roomImage)
    }
}