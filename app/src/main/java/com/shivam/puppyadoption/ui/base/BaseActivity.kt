package com.shivam.puppyadoption.ui.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shivam.puppyadoption.R

class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }
}