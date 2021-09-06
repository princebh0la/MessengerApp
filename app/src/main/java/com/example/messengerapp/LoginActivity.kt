package com.example.messengerapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar: Toolbar = findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Log In"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent= Intent(this@LoginActivity,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        login_btn.setOnClickListener {
            loginUser()
        }

    }

    private fun loginUser() {
        val email: String = email_login.text.toString()
        val password: String = password_login.text.toString()

        if(email == ""){
            Toast.makeText(this,"Enter the Email", Toast.LENGTH_LONG).show()
        }
        else if(password == ""){
            Toast.makeText(this,"Enter the Password", Toast.LENGTH_LONG).show()
        }
        else
        {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("LogIn")
            progressDialog.setMessage("Loading, Please wait")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task->
                    if(task.isSuccessful){
                        progressDialog.dismiss()

                        val intent= Intent(this@LoginActivity,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        progressDialog.dismiss()
                        Toast.makeText(this,"Error: ${task.exception!!.message.toString()}",Toast.LENGTH_LONG).show()
                    }
                }

        }
    }
}