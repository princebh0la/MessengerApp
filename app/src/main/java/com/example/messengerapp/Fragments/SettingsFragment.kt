package com.example.messengerapp.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.messengerapp.ModelClasses.Users
import com.example.messengerapp.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*


@Suppress("DEPRECATION")
class SettingsFragment : Fragment() {

    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri : Uri? = null
    private var storageRef : StorageReference? = null
    private var coverChecker :String? = null
    private var socialChecker :String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference!!.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user : Users? =snapshot.getValue(Users::class.java)

                   if(context!= null){
                       view.username_settings.text = user!!.getUsername()
                       Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                       Picasso.get().load(user.getCover()).into(view.cover_image_settings)

                   }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        view.profile_image_settings.setOnClickListener {
            pickImage()
        }

        view.cover_image_settings.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }

        view.set_facebook.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }

        view.set_instagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()
        }

        view.set_website.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()
        }

        return view
    }

    private fun setSocialLinks() {

        val builder = AlertDialog.Builder(context)

        val inflater : LayoutInflater = layoutInflater
        val view = inflater.inflate(R.layout.edittext_layout,null)

        val editText: EditText = view.findViewById(R.id.etComment)

        with(builder) {

            if (socialChecker == "website") {
                builder.setTitle("Write URL").create()
            } else if (socialChecker == "facebook") {
                builder.setTitle("Write facebook username").create()
            } else {
                builder.setTitle("Write instagram username").create()
            }



            if (socialChecker == "website") {
                editText.hint = "e.g www.google.com"
            }
            else if (socialChecker == "facebook") {
                editText.hint = "e.g prince.bhola.353"
            }
            else {
                editText.hint = "e.g princebh0la"
            }
            builder.setView(editText)

            builder.setPositiveButton("Create", DialogInterface.OnClickListener { dialog, which ->
                val str = editText.text.toString()

                if (str == "") {
                    Toast.makeText(context, "Please write something...", Toast.LENGTH_LONG).show()
                } else {
                    saveSocialLink(str)
                }
            })

            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
            })

            setView(view)
            builder.show()
        }
    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String,Any>()

        when(socialChecker){
            "facebook"->
            {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram"->
            {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
            "website"->
            {
                mapSocial["website"] = "https://$str"
            }
        }
        userReference!!.updateChildren(mapSocial).addOnCompleteListener {
            task->
            if(task.isSuccessful){
                Toast.makeText(context,"Updated Successfully", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null){
            imageUri = data.data
            Toast.makeText(context,"uploading....", Toast.LENGTH_LONG).show()
            uploadImage()
        }
    }

    private fun uploadImage() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("image is uploading, please wait...")
        progressBar.show()

        if(imageUri!= null){
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            val uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if(task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val downloadUrl =task.result
                    val url = downloadUrl.toString()

                    if(coverChecker== "cover"){
                        val mapCoverImg = HashMap<String,Any>()
                        mapCoverImg["cover"] = url
                        userReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""
                    }
                    else{
                        val mapProfileImg = HashMap<String,Any>()
                        mapProfileImg["profile"] = url
                        userReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }



    private fun updateStatus(status: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        val map = HashMap<String, Any>()
        map["status"] = status
        ref.updateChildren(map)
    }

}