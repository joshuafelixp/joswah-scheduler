package com.joswah.scheduler.scheduleDetail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.joswah.scheduler.admin.EditScheduleActivity
import com.joswah.scheduler.data.SharedPreferences
import com.joswah.scheduler.databinding.ActivityScheduleDetailBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ScheduleDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleDetailBinding
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var sf: SharedPreferences
    private val c = Calendar.getInstance()
    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleDetailBinding.inflate(layoutInflater)
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

        val dateText = LocalDate.of(year, month, date)
        val time = "$timeStart - $timeEnd"

        sf = SharedPreferences(this)
        database = FirebaseDatabase.getInstance().reference

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.textName.text = schedule_name
        binding.textNote.text = note
        binding.textDate.text = dateFormat.format(dateText)
        binding.textTime.text = time

        database.child("rooms").child(room_name).child("location")
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val location = snapshot.getValue(String::class.java)
                        binding.textLocation.text = location
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        binding.textRoom.text = room_name
        binding.textEquipment.text = equipment

        if (doc == "") {
            binding.btnDocView.visibility = View.GONE
        } else {
            storage = Firebase.storage.getReferenceFromUrl(doc)
            val docName = storage.name
            binding.textDoc.text = docName
            binding.btnDocView.setOnClickListener {
                val intent =
                    Intent(this@ScheduleDetailActivity, ViewDocumentActivity::class.java)
                intent.putExtra("doc", doc)
                startActivity(intent)
            }
        }


        if (sf.getStatusLogin()) {
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE

            binding.btnEdit.setOnClickListener {
                val intent =
                    Intent(this@ScheduleDetailActivity, EditScheduleActivity::class.java)
                intent.putExtra("uid", uid)
                intent.putExtra("schedule_name", schedule_name)
                intent.putExtra("note", note)
                intent.putExtra("date", date)
                intent.putExtra("month", month)
                intent.putExtra("year", year)
                intent.putExtra("timeStart", timeStart)
                intent.putExtra("timeEnd", timeEnd)
                intent.putExtra("room_name", room_name)
                intent.putExtra("equipment", equipment)
                intent.putExtra("doc", doc)
                startActivity(intent)
                finish()
            }

            binding.btnDelete.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Delete Schedule")
                    .setMessage("Apakah anda ingin menghapus jadwal ini?")
                    .setPositiveButton("Hapus") { _, _ ->
                        deleteSchedule()
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        } else {
            binding.btnEdit.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
        }
    }

    private fun deleteSchedule() {
        database = FirebaseDatabase.getInstance().reference
        database.child("schedules").child(uid).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil menghapus data", Toast.LENGTH_SHORT)
                    .show()
                if (doc == "") {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    storage = Firebase.storage.getReferenceFromUrl(doc)
                    storage.delete()
                    setResult(RESULT_OK)
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus data", Toast.LENGTH_SHORT)
                    .show()
            }

    }
}