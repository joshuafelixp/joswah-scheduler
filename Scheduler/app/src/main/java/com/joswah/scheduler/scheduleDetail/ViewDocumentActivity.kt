package com.joswah.scheduler.scheduleDetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.joswah.scheduler.databinding.ActivityViewDocumentBinding

class ViewDocumentActivity : AppCompatActivity() {
    private lateinit var binding : ActivityViewDocumentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val doc= intent.extras?.getString("doc")

        binding.btnClose.setOnClickListener{
            finish()
        }

        Glide.with(this)
            .load(doc)
            .into(binding.imageDoc)
    }
}