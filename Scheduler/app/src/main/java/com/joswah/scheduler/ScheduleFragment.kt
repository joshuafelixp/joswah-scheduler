package com.joswah.scheduler

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.joswah.scheduler.data.Schedule
import com.joswah.scheduler.data.ScheduleAdapter
import com.joswah.scheduler.databinding.FragmentScheduleBinding
import com.joswah.scheduler.scheduleDetail.ScheduleDetailActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private lateinit var scheduleList: MutableList<Schedule>
    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
    private val c = Calendar.getInstance()
    private val currentDate = c.timeInMillis
    private var date = c.get(Calendar.DAY_OF_MONTH)
    private var month = c.get(Calendar.MONTH) + 1
    private var year = c.get(Calendar.YEAR)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDate.text = dateFormat.format(LocalDate.of(year, month, date))
        binding.btnDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                    year = selectedYear
                    month = selectedMonth + 1
                    date = selectedDayOfMonth
                    binding.btnDate.text =
                        dateFormat.format(LocalDate.of(year, month, date))
                    updateScheduleList()
                },
                year,
                month - 1,
                date
            )
            datePickerDialog.datePicker.minDate = currentDate
            datePickerDialog.show()
        }

        scheduleList = mutableListOf()
        recyclerView = binding.recyclerviewSchedule
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        adapter = ScheduleAdapter(scheduleList)
        recyclerView.adapter = adapter
        database = FirebaseDatabase.getInstance().reference
        updateScheduleList()
    }

    private fun updateScheduleList() {
        database.child("schedules").orderByChild("year").equalTo(year.toDouble())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        scheduleList.clear()
                        for (data in snapshot.children) {
                            val schedule = data.getValue(Schedule::class.java)
                            if (schedule?.month == month && schedule.date == date) {
                                scheduleList.add(schedule)
                            }
                        }
                        scheduleList.sortWith(compareBy<Schedule> { it.date }
                            .thenBy { it.timeStart })
                        adapter.notifyDataSetChanged()
                        adapter.setOnItemClickListener(object :
                            ScheduleAdapter.OnItemClickListener {
                            override fun onItemClick(position: Int) {
                                val intent =
                                    Intent(requireContext(), ScheduleDetailActivity::class.java)
                                intent.putExtra("uid", scheduleList[position].uid)
                                intent.putExtra(
                                    "schedule_name",
                                    scheduleList[position].schedule_name
                                )
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

                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            updateScheduleList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}