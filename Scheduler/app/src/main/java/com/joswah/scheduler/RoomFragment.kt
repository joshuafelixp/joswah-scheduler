package com.joswah.scheduler

import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.joswah.scheduler.data.Room
import com.joswah.scheduler.data.RoomAdapter
import com.joswah.scheduler.databinding.FragmentRoomBinding

class RoomFragment : Fragment() {
    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!
    private lateinit var database : DatabaseReference
    private lateinit var recyclerView : RecyclerView
    private lateinit var roomList : ArrayList<Room>
    private lateinit var spinnerLocations: Spinner
    private val locations = arrayOf("Malang","Kepanjen")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerLocations = binding.spinnerLocations
        val adapterSpinner = ArrayAdapter(this.requireContext(),  R.layout.simple_spinner_dropdown_item, locations)
        spinnerLocations.adapter = adapterSpinner
        spinnerLocations.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                recyclerView = binding.recycleViewRoom
                recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                roomList = arrayListOf()
                database = FirebaseDatabase.getInstance().reference.child("rooms")
                database.orderByChild("location").equalTo(locations[position]).addValueEventListener(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            roomList.clear()
                            for (data in snapshot.children){
                                val room = data.getValue(Room::class.java)
                                roomList.add(room!!)
                            }
                            recyclerView.adapter = RoomAdapter(requireContext(),roomList)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}