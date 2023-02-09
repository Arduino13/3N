package com.example.vocab.scanner

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.vocab.R
import com.example.vocab.databinding.ActivityScannerBinding
import com.example.vocab.thirdParty.Translator

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.toolbar.setOnClickListener{
            val navHost = supportFragmentManager.findFragmentById(R.id.host_nav_scanner) as NavHostFragment
            navHost.navController.popBackStack()

            val fragmentModel: ScannerModel by viewModels()
            if(fragmentModel.process == null && fragmentModel.mode == null) finish()
        }
    }
}