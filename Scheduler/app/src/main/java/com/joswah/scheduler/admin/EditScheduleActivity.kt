package com.joswah.scheduler.admin

import android.R
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.internal.ContextUtils
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.joswah.scheduler.data.Schedule
import com.joswah.scheduler.databinding.ActivityEditScheduleBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class EditScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditScheduleBinding
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private val c = Calendar.getInstance()
    private val localDateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val locations = arrayOf("Malang", "Kepanjen")

    private var uid = ""
    private var schedule_name = ""
    private var note = ""
    private var date = 0
    private var month = 0
    private var year = 0
    private var timeStart = ""
    private var timeEnd = ""
    private var room_name = ""
    private var equipment = ""
    private var doc = ""
    private var newDoc = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle: Bundle? = intent.extras
        uid = bundle?.getString("uid")!!
        schedule_name = bundle.getString("schedule_name")!!
        note = bundle.getString("note")!!
        date = bundle.getInt("date")
        month = bundle.getInt("month")
        year = bundle.getInt("year")
        timeStart = bundle.getString("timeStart")!!
        timeEnd = bundle.getString("timeEnd")!!
        room_name = bundle.getString("room_name")!!
        equipment = bundle.getString("equipment")!!
        doc = bundle.getString("doc")!!

        newDoc = doc

        val currentDate = c.timeInMillis
        var startHour = timeStart.split(":")[0].toInt()
        var startMinute = timeStart.split(":")[1].toInt()
        var endHour = timeEnd.split(":")[0].toInt()
        var endMinute = timeEnd.split(":")[1].toInt()

        binding.btnClose.setOnClickListener {
            cancel()
        }
        binding.inputName.setText(schedule_name)
        binding.inputNote.setText(note)
        binding.btnDate.text = localDateFormat.format(LocalDate.of(year, month, date))
        binding.btnDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                    year = selectedYear
                    month = selectedMonth + 1
                    date = selectedDayOfMonth
                    binding.btnDate.text =
                        localDateFormat.format(LocalDate.of(year, month, date))
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

        val spinnerLocations = binding.spinnerLocations
        spinnerLocations.adapter =
            ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, locations)
        database = FirebaseDatabase.getInstance().reference
        database.child("rooms").child(room_name).child("location")
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val location = snapshot.getValue(String::class.java)
                        spinnerLocations.setSelection(locations.indexOf(location))
                    } else {

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        spinnerLocations.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinnerRoom = binding.spinnerRooms
                val rooms = ArrayList<String>()
                database.child("rooms").orderByChild("location").equalTo(locations[position])
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            rooms.clear()
                            for (item in snapshot.children) {
                                rooms.add(item.child("room_name").value.toString())
                            }
                            spinnerRoom.adapter = ArrayAdapter(
                                this@EditScheduleActivity,
                                android.R.layout.simple_spinner_dropdown_item,
                                rooms
                            )
                            spinnerRoom.setSelection(rooms.indexOf(room_name))
                            spinnerRoom.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                    ) {
                                        room_name = rooms[position]
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

        binding.inputEquipment.setText(equipment)

        if (newDoc != "") {
            storage = Firebase.storage.getReferenceFromUrl(newDoc)
            val docName = storage.name
            binding.textDocument.text = docName

            binding.btnTrash.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Delete File")
                    .setMessage("Apakah anda ingin menghapus dokumen jadwal ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        newDoc = ""
                        Toast.makeText(this, "Berhasil menghapus file", Toast.LENGTH_SHORT).show()
                        binding.textDocument.text = "Dokumen berekstensi img"
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        } else {
            binding.btnTrash.visibility = View.GONE
        }

        binding.btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.btnCancel.setOnClickListener {
            cancel()
        }
        binding.btnEdit.setOnClickListener {
            if (binding.inputName.text!!.isEmpty()) {
                binding.inputName.error = "Nama kegiatan harus diisi!"
            } else if (timeEnd <= timeStart) {
                Toast.makeText(
                    applicationContext,
                    "Waktu kegiatan tidak valid!!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val updatedSchedule = Schedule(
                    uid = uid,
                    schedule_name = binding.inputName.text.toString(),
                    note = binding.inputNote.text.toString(),
                    date = date,
                    month = month,
                    year = year,
                    timeStart = timeStart,
                    timeEnd = timeEnd,
                    room_name = room_name,
                    equipment = binding.inputEquipment.text.toString(),
                    doc = newDoc
                )

                checkScheduleExist(updatedSchedule) { exists ->
                    if (exists) {
                        Toast.makeText(
                            applicationContext,
                            "Jadwal sudah ada!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        database.child("schedules").child(uid).setValue(updatedSchedule)
                            .addOnSuccessListener {
                                if (newDoc != doc && doc != "") {
                                    storage = Firebase.storage.getReferenceFromUrl(doc)
                                    storage.delete()
                                }
                                Toast.makeText(this, "Update berhasil", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                val errorMessage = exception.message
                                Toast.makeText(
                                    this,
                                    "Update gagal: $errorMessage",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                    }
                }
            }
        }
    }

    private fun cancel() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cancel")
            .setMessage("Apakah anda ingin menghilangkan perubahan jadwal?")
            .setPositiveButton("Ya") { _, _ ->
                if (newDoc != doc) {
                    storage = Firebase.storage.getReferenceFromUrl(newDoc)
                    storage.delete()
                }
                finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
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
                                (timeEnd < timeStart)
                            ) {
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

    @SuppressLint("RestrictedApi", "Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uriFile = data!!.data
            val uriString = uriFile.toString()
            var displayName = ""
            if (uriString.startsWith("content://")) {
                var cursor: Cursor? = null
                try {
                    cursor = ContextUtils.getActivity(this)!!.contentResolver.query(
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
                    newDoc = uri.toString()
                    Toast.makeText(this, "Berhasil Upload", Toast.LENGTH_SHORT).show()
                    binding.textDocument.text = displayName
                }
            }
        }
    }
    override fun onBackPressed() {
        cancel()
    }
}