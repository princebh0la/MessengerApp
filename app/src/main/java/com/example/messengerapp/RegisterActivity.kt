package com.example.messengerapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent= Intent(this@RegisterActivity,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        register_btn.setOnClickListener {
            registerUsers()
        }

    }

    private fun registerUsers() {

        val username: String = username_register.text.toString()
        val email: String = email_register.text.toString()
        val password: String = password_register.text.toString()

        if(username == ""){
            Toast.makeText(this,"Enter the username",Toast.LENGTH_LONG).show()
        }
        else if(email == ""){
            Toast.makeText(this,"Enter the Email",Toast.LENGTH_LONG).show()
        }
        else if(password == ""){
            Toast.makeText(this,"Enter the Password",Toast.LENGTH_LONG).show()
        }
        else{
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("SignUp")
            progressDialog.setMessage("Loading, Please wait")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        progressDialog.dismiss()

                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserID
                        userHashMap["username"] = username
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-a6dfa.appspot.com/o/profile.png?alt=media&token=0e259063-eac6-4bc5-9f48-fbe48a8c92b5"
                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/messengerapp-a6dfa.appspot.com/o/cover.jpg?alt=media&token=418677a5-c676-4fa2-834e-268366ec1dc0"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "http://m.facebook.com"
                        userHashMap["instagram"] = "http://m.instagram.com"
                        userHashMap["website"] = "http://www.google.com"

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task->
                                if(task.isSuccessful)
                                {
                                    val intent= Intent(this@RegisterActivity,MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }

                            }

                    }
                    else
                    {
                        progressDialog.dismiss()
                        Toast.makeText(this,"Error: ${task.exception!!.message.toString()}",Toast.LENGTH_LONG).show()
                    }
                }

        }

    }
}