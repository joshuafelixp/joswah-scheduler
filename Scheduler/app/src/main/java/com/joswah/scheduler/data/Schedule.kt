package com.joswah.scheduler.data

data class Schedule(
    val uid: String = "",
    val schedule_name: String = "",
    val note: String = "",
    val date: Int = 0,
    val month: Int = 0,
    val year: Int = 0,
    val timeStart: String = "",
    val timeEnd: String = "",
    val room_name: String = "",
    val equipment: String = "",
    val doc: String = ""
)