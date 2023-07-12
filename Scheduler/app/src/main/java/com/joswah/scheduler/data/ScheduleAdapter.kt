package com.joswah.scheduler.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.joswah.scheduler.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleAdapter(private val scheduleList: MutableList<Schedule>) :
    RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {
    private var listener: OnItemClickListener? = null
    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
    private val database = Firebase.database.reference.child("rooms")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = scheduleList[position]
        val year = schedule.year
        val month = schedule.month
        val date = schedule.date
        val dateText = LocalDate.of(year, month, date)
        val timeStart = schedule.timeStart
        val timeEnd = schedule.timeEnd
        database.child(schedule.room_name).child("location").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val location = snapshot.getValue(String::class.java)
                    if (location.equals("Malang")) {
                        holder.textLocation.text = "MLG"
                    } else {
                        holder.textLocation.text = "KPJ"
                    }
                } else {

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        holder.textDate.text = dateFormat.format(dateText)
        holder.textScheduleName.text = schedule.schedule_name
        holder.textRoom.text = schedule.room_name
        holder.textTime.text = "$timeStart - $timeEnd"

        holder.itemView.setOnClickListener {
            listener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textLocation: TextView = itemView.findViewById(R.id.textLocation)
        val imageDate: ImageView = itemView.findViewById(R.id.imageDate)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textScheduleName: TextView = itemView.findViewById(R.id.textScheduleName)
        val textRoom: TextView = itemView.findViewById(R.id.textRoom)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
        val line: View = itemView.findViewById(R.id.line)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}