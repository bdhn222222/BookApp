package com.example.bookapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            checkUser()
        },2000) //means 2 seconds
    }
    /*giu cho nguoi dung dang nhap
    1) check nguoi dung co dang log in hay khong
    2) xem nguoi dung do la user hay admin
    */
    private fun checkUser(){
        //xem co dang login hay khong
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            //neu khong log in -> di den Main Activity
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        else{
            //neu co log in, check type user (user hay admin)
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //xem user loai nao admin hay user
                        val userType = snapshot.child("userType").value
                        if(userType == "user"){
                            //di den user dashboard
                            startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                            finish()
                        }
                        else if (userType=="admin"){
                            //di den admin dashboard
                            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                            finish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}