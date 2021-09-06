@file:Suppress("DEPRECATION")

package com.example.messengerapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.AdapterClasses.ChatsAdapter
import com.example.messengerapp.ModelClasses.Chat
import com.example.messengerapp.ModelClasses.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

const val TOPIC = "/topics/myTopic"

@Suppress("DEPRECATION")
class MessageChatActivity : AppCompatActivity() {

    var userIDVisit :String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>? =null
    var reference: DatabaseReference? = null
    var currentTime: String? = null
    lateinit var recycler_view_chats: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        setSupportActionBar(toolbar_message_chat)
        supportActionBar!!.title = ""
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        toolbar_message_chat.setNavigationOnClickListener {
//            val intent  = Intent(this, WelcomeActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            finish()
//        }


        intent = intent
        userIDVisit = intent.getStringExtra("visit_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats =findViewById(R.id.recycler_view_chat)
        recycler_view_chats.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager =linearLayoutManager

        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIDVisit)
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                username_mchat.text =user!!.getUsername()
                Picasso.get().load(user.getProfile()).into(profile_image_mchat)

                retrieveMessages(firebaseUser!!.uid, userIDVisit, user.getProfile())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

//        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
//
//        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
//            FirebaseService.token = it.token
//        }
//
//        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        send_message_btn.setOnClickListener {
            val message = text_message.text.toString()


            if(message == "")
            {
                Toast.makeText(this,"Please write something",Toast.LENGTH_LONG).show()
            }
            else{
                sendMessageToUser(firebaseUser!!.uid, userIDVisit, message)

            }
            text_message.setText("")
        }

        attach_image_file_btn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Select Image"),438)
        }

        seenMessage(userIDVisit)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessageToUser(senderID: String, receiverID: String, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key!!

        var name : String? = null

        val ldt = LocalDateTime.now()
        currentTime = ldt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

        val messageHashMap = HashMap<String,Any>()
        messageHashMap["sender"] = senderID
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverID
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        messageHashMap["time"] = currentTime!!
        reference.child("Chats")
            .child(messageKey)
            .setValue(messageHashMap)
            .addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference.child("ChatsList")
                        .child(firebaseUser!!.uid)
                        .child(userIDVisit)

//                    val tokenRef =  FirebaseDatabase.getInstance()
//                        .reference.child("Token").child(firebaseUser!!.uid)
//                    tokenRef.addListenerForSingleValueEvent(object : ValueEventListener{
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if(snapshot.exists()){
//                                val token = FirebaseInstanceId.getInstance().token
//                                tokenRef.child("token").setValue(token)
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                            TODO("Not yet implemented")
//                        }
//
//                    })

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(!snapshot.exists()){
                                chatsListReference.child("id").setValue(userIDVisit)
                            }
                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference.child("ChatsList")
                                .child(userIDVisit)
                                .child(firebaseUser!!.uid)
                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })


                }
            }

//        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(senderID)
//        ref.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val user = snapshot.getValue(Users::class.java)
//                name = user!!.getUsername()!!
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//
//        })

//        val token = FirebaseInstanceId.getInstance().token
//
//        PushNotification(
//            NotificationData("MessengerApp", "$name: $message"),
//            token!!
//        ).also {
//            sendNotification(it)
//        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == RESULT_OK && data!=null && data!!.data != null){
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("image is uploading, please wait...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key!!
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if(task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val ldt = LocalDateTime.now()
                    currentTime = ldt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

                    val messageHashMap = HashMap<String, Any>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIDVisit
                    messageHashMap["isSeen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId
                    messageHashMap["time"] = currentTime!!

                    ref.child("Chats").child(messageId).setValue(messageHashMap)

                    progressBar.dismiss()
                }
            }
        }
    }

//    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val response = RetrofitInstance.api.postNotification(notification)
//            if(response.isSuccessful){
//                Log.e("MessageChatActivity", "Response: ${Gson().toJson(response)}")
//            }
//            else
//                Log.e("MessageChatActivity", response.errorBody().toString())
//        }
//        catch (e: Exception){
//            Log.e("MessageChatActivity", e.toString())
//        }
//    }

    private fun retrieveMessages(senderId: String, receiverId: String, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()

                for(snap in snapshot.children){
                    val chat = snap.getValue(Chat::class.java)

                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                            || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)){
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(this@MessageChatActivity,
                            (mChatList as ArrayList<Chat>),
                            receiverImageUrl!!)
                    recycler_view_chats.adapter =chatsAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    var seenListener: ValueEventListener? = null

    private fun seenMessage(userId: String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(dataSnapshot in snapshot.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if(chat!!.getReceiver().equals(firebaseUser!!.uid)
                            && chat!!.getSender().equals(userId)){
                        val hashMap = HashMap<String,Any>()
                        hashMap["isSeen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)
    }

    private fun updateStatus(status: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        val map = HashMap<String, Any>()
        map["status"] = status
        ref.updateChildren(map)
    }


}