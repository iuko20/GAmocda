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
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.activity_user.iv_profileImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private var fileUri: Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        secondActivityBtn.setOnClickListener {
            val intent = Intent(this, NewActivity::class.java)
            startActivity(intent)
        }


        auth=FirebaseAuth.getInstance()
        setUserInfo()
        btnClicks()
    }
    private fun btnClicks(){
        tv_profile_signOut.setOnClickListener {
            signOutUser()
        }
        btn_profileSaveInfo.setOnClickListener {
            saveUserInfo()
        }
        iv_profileImage.setOnClickListener {
            selectImage()
        }
    }
    private fun setUserInfo(){
        et_profileEmail.setText(auth.currentUser?.email)
        et_profileUsername.setText(auth.currentUser?.displayName)
        iv_profileImage.setImageURI(auth.currentUser?.photoUrl)

        fileUri=auth.currentUser?.photoUrl

    }

    private fun saveUserInfo(){
        auth.currentUser?.let {
            val username:String = et_profileUsername.text.toString()
            val userProfilePicture:Uri?=fileUri
            val userEmail:String=et_profileEmail.text.toString()

            val update = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(userProfilePicture)
                .build()
            GlobalScope.launch(Dispatchers.IO) {
                try{
                    it.updateProfile(update).await()
                    it.updateEmail(userEmail)

                    withContext(Dispatchers.Main){
                        setUserInfo()
                        Toast.makeText(this@UserActivity, "Profile successfully updated!", Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@UserActivity,e.message,Toast.LENGTH_SHORT).show()
                }
            }
            }

        }


    }
    private fun signOutUser(){
        auth.signOut()
        val i =Intent(this,MainActivity::class.java)
        startActivity(i)
        Toast.makeText(this,"Succesfully Signed Out!",Toast.LENGTH_SHORT).show()
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