package com.mushafaandira.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.kotlinmessenger.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        register_button_register.setOnClickListener {
            performRegister()

        }
        have_account_textview_register.setOnClickListener {
            Log.d("MainActivity","Ke login page")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        profile_picture_button_register.setOnClickListener {
            Log.d("MainActivity","Pilih foto")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }

    }
    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null ){
            Log.d("MainActivity","Foto sudah dipilih")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            image_circleimageview_register.setImageBitmap(bitmap)
            profile_picture_button_register.alpha = 0f
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //profile_picture_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }
    private fun performRegister(){
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        val username = username_edittext_register.text.toString()
        if(email.isEmpty() || password.isEmpty() || username.isEmpty() || selectedPhotoUri == null ) {
            Toast.makeText(this, "Please enter information", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    if (!it.isSuccessful) {
                        Log.d("Main", "did not create user" + it.exception)
                        return@addOnCompleteListener
                    }
                    Log.d("Main", "Successfully created user with uid: ${it.result!!.user!!.uid}")
                    uploadImageToFirebase()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal Membuat akun : ${it.message}", Toast.LENGTH_SHORT).show()
                }

        Log.d("MainActivity","Email = $email")
        Log.d("MainActivity","Password = $password")

    }
    fun uploadImageToFirebase(){
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("MainActivity","Successfully upload image : ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                saveUserToFirebase(it.toString())
                Log.d("Main","Image Location : $it")

            }
                    .addOnFailureListener {
                        Log.d("Main","Gagal download link: ${it.message}")
                    }
        }

    }
    fun saveUserToFirebase(profileimageurl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid,username_edittext_register.text.toString(),profileimageurl)
        ref.setValue(user).addOnSuccessListener {
            Log.d("Main","Berhasil menyimpan data user")
            val intent = Intent(this,LatestMessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    @Parcelize
    class User(val uid : String, val username: String , val profileimageurl : String) : Parcelable{
        constructor() : this("","","")
    }
}