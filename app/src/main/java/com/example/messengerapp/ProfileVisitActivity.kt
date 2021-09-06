package com.example.messengerapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.messengerapp.AdapterClasses.UserAdapter
import com.example.messengerapp.ModelClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.view.*
import kotlinx.android.synthetic.main.activity_profile_visit.*

class ProfileVisitActivity : AppCompatActivity() {

    private lateinit var profileID: String
    private lateinit var facebookLink: String
    private lateinit var instagramLink: String
    private lateinit var websiteLink: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_visit)

        profileID = intent.getStringExtra("profile_id").toString()

        getUserDetails(profileID)

        send_message.setOnClickListener {
            val intent= Intent(this, MessageChatActivity::class.java)
            intent.putExtra("visit_id",profileID)
            startActivity(intent)
        }

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileID)
        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                facebookLink = user!!.getFacebook()!!
                instagramLink = user.getInstagram()!!
                websiteLink = user.getWebsite()!!
                profile_image_profile_visit.setOnClickListener {
                    val intent = Intent(this@ProfileVisitActivity, FullScreenImageActivity::class.java)
                    intent.putExtra("Url", user!!.getProfile())
                    startActivity(intent)
                }

                cover_image_profile_visit.setOnClickListener {
                    val intent = Intent(this@ProfileVisitActivity, FullScreenImageActivity::class.java)
                    intent.putExtra("Url", user!!.getCover())
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        get_facebook.setOnClickListener {
            gotoUrl(facebookLink)
        }

        get_instagram.setOnClickListener {
            gotoUrl(instagramLink)
        }

        get_website.setOnClickListener {
            gotoUrl(websiteLink)
        }
    }

    private fun gotoUrl(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(intent)
    }

    private fun getUserDetails(profileID: String) {
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(profileID)
        refUsers.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                Picasso.get().load(user!!.getCover()).into(cover_image_profile_visit)
                Picasso.get().load(user!!.getProfile()).into(profile_image_profile_visit)
                username_profile_visit.text = user.getUsername()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}