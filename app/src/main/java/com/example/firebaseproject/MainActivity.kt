package com.example.firebaseproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth

    private var firsTimeUser = true

    private var fileUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        buttonClicks()
    }
    private fun buttonClicks() {
        btn_login.setOnClickListener {
            firsTimeUser=false
            createOrLogInUser()
        }
        btn_register.setOnClickListener {
            firsTimeUser=true
            createOrLogInUser()
        }
        iv_profileImage.setOnClickListener {
            selectImage()
        }
    }
    private fun createOrLogInUser(){
        val email:String = et_emailLogin.text.toString()
        val password:String = et_passwordLogin.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()){
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    if(firsTimeUser){
                        auth.createUserWithEmailAndPassword(email,password).await()
                        auth.currentUser.let {
                            val update:UserProfileChangeRequest=UserProfileChangeRequest.Builder()
                                .setPhotoUri(fileUri)
                                .build()
                            it?.updateProfile(update)

                        }?.await()
                    }else{
                        auth.signInWithEmailAndPassword(email,password).await()
                    }
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity,"You are now Logged in!",Toast.LENGTH_SHORT).show()
                        val i = Intent(this@MainActivity, UserActivity::class.java)
                        startActivity(i)
                        finish()
                    }

                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_SHORT)

                    }
                }
            }
        }

    }
    private fun checkIfUserIsLoggedIn(){
        if(auth.currentUser != null){
            val i = Intent(this@MainActivity, UserActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        checkIfUserIsLoggedIn()
    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode){
            Activity.RESULT_OK->{
                fileUri=data?.data
                iv_profileImage.setImageURI(fileUri)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else->{
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }




}



