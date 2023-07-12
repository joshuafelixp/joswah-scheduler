package com.joswah.scheduler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.joswah.scheduler.data.Schedule
import com.joswah.scheduler.data.ScheduleAdapter
import com.joswah.scheduler.databinding.FragmentHomeBinding
import com.joswah.scheduler.scheduleDetail.ScheduleDetailActivity
import java.time.LocalDate
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private lateinit var scheduleList: MutableList<Schedule>
    private val calendar = Calendar.getInstance()
    private val currentYear = calendar.get(Calendar.YEAR)
    private val currentDate = LocalDate.now()
    private val rangeDate = currentDate.plusDays(2)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSchedules.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_nav_home_to_nav_schedule)
        }
        scheduleList = mutableListOf()
        recyclerView = binding.upcomingSchedules
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        adapter = ScheduleAdapter(scheduleList)
        recyclerView.adapter = adapter
        database = FirebaseDatabase.getInstance().reference
        updateScheduleList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            updateScheduleList()
        }
    }

    private fun updateScheduleList() {
        database.child("schedules").orderByChild("year").equalTo(currentYear.toDouble())
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        scheduleList.clear()
                        for (item in snapshot.children) {
                            val schedule = item.getValue(Schedule::class.java)
                            val localDate =
                                LocalDate.of(schedule!!.year, schedule.month, schedule.date)
                            if (localDate in currentDate..rangeDate) {
                                scheduleList.add(schedule)
                            }
                        }
                        scheduleList.sortWith(compareBy<Schedule> { it.date }
                            .thenBy { it.timeStart })

                        adapter.notifyDataSetChanged()
                        adapter.setOnItemClickListener(object : ScheduleAdapter.OnItemClickListener {
                            override fun onItemClick(position: Int) {
                                val intent =
                                    Intent(requireContext(), ScheduleDetailActivity::class.java)
                                intent.putExtra("uid", scheduleList[position].uid)
                                intent.putExtra("schedule_name", scheduleList[position].schedule_name)
                                intent.putExtra("note", scheduleList[position].note)
                                intent.putExtra("date", scheduleList[position].date)
                                intent.putExtra("month", scheduleList[position].month)
                                intent.putExtra("year", scheduleList[position].year)
                                intent.putExtra("timeStart", scheduleList[position].timeStart)
                                intent.putExtra("timeEnd", scheduleList[position].timeEnd)
                                intent.putExtra("room_name", scheduleList[position].room_name)
                                intent.putExtra("equipment", scheduleList[position].equipment)
                                intent.putExtra("doc", scheduleList[position].doc)
                                startActivityForResult(intent, 1)
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}