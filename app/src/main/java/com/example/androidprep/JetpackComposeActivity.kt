package com.example.androidprep

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class JetpackComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            LoginPage()
        }
    }
    @Composable()
    fun LoginPage(){
        var email by remember {
            mutableStateOf("")
        }
        var password by remember { mutableStateOf("") }
        Column() {
            Text(
                text="Login Screen"
            )
            TextField(value = email, onValueChange = {
                it -> email
            })
            TextField(value=password, onValueChange = {
                it -> password
            })
            Button(onClick={}) { }
            Text( text ="Submit")

        }
    }


}