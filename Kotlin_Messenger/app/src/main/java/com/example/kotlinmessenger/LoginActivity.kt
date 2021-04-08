package com.mushafaandira.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button_login.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()
            Log.d("LoginActivity","Email = $email")
            Log.d("LoginActivity","Password = $password")
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) {
                            Log.d("Login", "Gagal Sign in" + it.exception)
                            Toast.makeText(this,"Gagal Sign in : ${it.exception}", Toast.LENGTH_LONG).show()
                            return@addOnCompleteListener
                        }
                        Log.d("Login", "Berhasil Login dengan uid: ${it.result!!.user!!.uid} dan user : ${it.result!!.user}")
                        val intent = Intent(this, LatestMessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal sign in : ${it.message}", Toast.LENGTH_SHORT).show()

                    }
        }
        back_textview_login.setOnClickListener {
            finish()
        }
    }
}