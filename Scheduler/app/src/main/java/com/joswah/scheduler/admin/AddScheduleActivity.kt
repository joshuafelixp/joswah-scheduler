package com.joswah.scheduler.admin

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.joswah.scheduler.data.Schedule
import com.joswah.scheduler.databinding.ActivityAddScheduleBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


class AddScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddScheduleBinding
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private val c = Calendar.getInstance()
    private var currentDate = c.timeInMillis
    private var date = c.get(Calendar.DAY_OF_MONTH)
    private var month = c.get(Calendar.MONTH) + 1
    private var year = c.get(Calendar.YEAR)
    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private var startHour = c.get(Calendar.HOUR_OF_DAY)
    private var startMinute = c.get(Calendar.MINUTE)
    private var endHour = 0
    private var endMinute = 0
    private var timeStart = timeFormat.format(LocalTime.of(startHour, startMinute))
    private var timeEnd = ""
    private val locations = arrayOf("Malang", "Kepanjen")
    private var room = ""
    private var doc = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener {
            cancelAddSchedule()
        }

        binding.btnDate.text = dateFormat.format(LocalDate.of(year, month, date))
        binding.btnDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                    year = selectedYear
                    month = selectedMonth + 1
                    date = selectedDayOfMonth
                    binding.btnDate.text =
                        dateFormat.format(LocalDate.of(year, month, date))
                },
                year,
                month - 1,
                date
            )
            datePickerDialog.datePicker.minDate = currentDate
            datePickerDialog.show()
        }

        binding.btnStart.text = timeStart
        binding.btnStart.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                    startHour = selectedHour
                    startMinute = selectedMinute
                    timeStart = timeFormat.format(LocalTime.of(startHour, startMinute))
                    binding.btnStart.text = timeStart
                },
                startHour,
                startMinute,
                true
            )
            timePickerDialog.show()
        }

        binding.btnEnd.text = timeEnd
        binding.btnEnd.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                    val updatedTime = timeFormat.format(LocalTime.of(selectedHour, selectedMinute))
                    if (updatedTime <= timeStart) {
                        Toast.makeText(
                            this,
                            "Jam selesai tidak boleh kurang dari jam mulai!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        endHour = selectedHour
                        endMinute = selectedMinute
                        timeEnd = timeFormat.format(LocalTime.of(endHour, endMinute))
                        binding.btnEnd.text = timeEnd
                    }
                },
                endHour,
                endMinute,
                true
            )
            timePickerDialog.show()
        }

        val spinnerLocation = binding.spinnerLocations
        spinnerLocation.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
        spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerRoom = binding.spinnerRooms
                val rooms = ArrayList<String>()
                database = Firebase.database.reference.child("rooms")
                database.orderByChild("location").equalTo(locations[position])
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            rooms.clear()
                            for (item in snapshot.children) {
                                rooms.add(item.child("room_name").value.toString())
                            }
                            spinnerRoom.adapter = ArrayAdapter(
                                this@AddScheduleActivity,
                                android.R.layout.simple_spinner_dropdown_item,
                                rooms
                            )
                            spinnerRoom.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                    ) {
                                        room = rooms[position]
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                        TODO("Not yet implemented")
                                    }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.btnCancel.setOnClickListener {
            cancelAddSchedule()
        }

        binding.btnAdd.setOnClickListener {
            if (binding.inputName.text!!.isEmpty()) {
                binding.inputName.error = "Nama kegiatan harus diisi!"
            } else if (binding.btnStart.text == "" || binding.btnEnd.text == "") {
                Toast.makeText(
                    applicationContext,
                    "Waktu kegiatan harus diisi!!",
                    Toast.LENGTH_SHORT
                ).show()
            } else if(timeEnd<=timeStart){
                Toast.makeText(
                    applicationContext,
                    "Waktu kegiatan tidak valid!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                database = Firebase.database.getReference("schedules")
                val uid = database.push().key!!
                val schedule = Schedule(
                    uid = uid,
                    schedule_name = binding.inputName.text.toString(),
                    note = binding.inputNote.text.toString(),
                    date = date,
                    month = month,
                    year = year,
                    timeStart = timeStart,
                    timeEnd = timeEnd,
                    room_name = room,
                    equipment = binding.inputEquipment.text.toString(),
                    doc = doc
                )
                checkScheduleExist(schedule) { exists ->
                    if (exists) {
                        Toast.makeText(
                            applicationContext,
                            "Jadwal sudah ada!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        database.child(schedule.uid).setValue(schedule).addOnSuccessListener {
                            Toast.makeText(
                                applicationContext,
                                "Jadwal berhasil dibuat!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun checkScheduleExist(schedule: Schedule, callback: (Boolean) -> Unit) {
        val ref = Firebase.database.reference.child("schedules")
        ref.orderByChild("room_name").equalTo(schedule.room_name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var scheduleExists = false
                    for (item in snapshot.children) {
                        val result = item.getValue(Schedule::class.java)
                        if (result?.date == schedule.date && result.month == schedule.month && result.year == schedule.year) {
                            if ((timeStart > result.timeStart && timeStart < result.timeEnd) ||
                                (timeEnd > result.timeStart && timeEnd < result.timeEnd) ||
                                (timeEnd < timeStart)) {
                                scheduleExists = true
                                break
                            }
                        }
                    }
                    callback(scheduleExists)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    private fun cancelAddSchedule() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Keluar")
            .setMessage("Data anda belum tersimpan apakah anda yakin ingin keluar dari halaman ini?")
            .setPositiveButton("Keluar") { _, _ ->
                if (doc != "") {
                    storage = Firebase.storage.getReferenceFromUrl(doc)
                    storage.delete()
                }
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    @SuppressLint("RestrictedApi", "Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (doc != "") {
                storage = Firebase.storage.getReferenceFromUrl(doc)
                storage.delete()
            }
            val uriFile = data!!.data
            val uriString = uriFile.toString()
            var displayName = ""
            if (uriString.startsWith("content://")) {
                var cursor: Cursor? = null
                try {
                    cursor = getActivity(this)!!.contentResolver.query(
                        uriFile!!,
                        null,
                        null,
                        null,
                        null
                    );
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor!!.close();
                }
            }
            storage = Firebase.storage.getReference("Documents")
            val documentUpload = storage.child(c.timeInMillis.toString() + displayName)
            documentUpload.putFile(uriFile!!).addOnSuccessListener {
                documentUpload.downloadUrl.addOnSuccessListener { uri ->
                    doc = uri.toString()
                    Toast.makeText(this, "Berhasil Upload", Toast.LENGTH_SHORT).show()
                }
            }
            binding.textDocument.text = displayName
        }
    }

    override fun onBackPressed() {
        cancelAddSchedule()
    }
}