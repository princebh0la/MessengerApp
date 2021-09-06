package com.example.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.icu.number.NumberFormatter.with
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.Fragments.ChatsFragment
import com.example.messengerapp.Fragments.SearchFragment
import com.example.messengerapp.Fragments.SettingsFragment
import com.example.messengerapp.MainActivity
import com.example.messengerapp.MessageChatActivity
import com.example.messengerapp.ModelClasses.Chat
import com.example.messengerapp.ModelClasses.Users
import com.example.messengerapp.ProfileVisitActivity
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_message_chat.*
import kotlinx.android.synthetic.main.user_search_item_layout.view.*

class UserAdapter(
    private val mContext: Context,
    private val mUsers: List<Users>
    ): RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view : View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout,parent,false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, i: Int) {
        val user: Users = mUsers[i]
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        holder.userNameTxt.text = user!!.getUsername()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile).into(holder.profileImageView)

        if(user.getStatus() == "online"){
            holder.onlineImageView.visibility = View.VISIBLE
        }
        else if(user.getStatus() == "offline"){
            holder.offlineImageView.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options,DialogInterface.OnClickListener {
                    dialog, which ->
                if(which == 0){
                    val intent= Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id",user.getUID())
                    mContext.startActivity(intent)
                }
                if(which == 1){
                    val intent= Intent(mContext, ProfileVisitActivity::class.java)
                    intent.putExtra("profile_id",user.getUID())
                    mContext.startActivity(intent)
                }
            })
            builder.show()
        }

        val reference = FirebaseDatabase.getInstance().reference
                .child("Users").child(user.getUID()!!)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users: Users? = snapshot.getValue(Users::class.java)
                retrieveMessage(firebaseUser!!.uid, user.getUID()!!, holder.lastMessageTxt)
                retrieveMessageCount(firebaseUser!!.uid, user.getUID()!!, holder.messageCountTxt)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    private fun retrieveMessageCount(senderId: String, receiverId: String, messageCountTxt: TextView) {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var countUnreadMessages = 0

                for(dataSnapshot in snapshot.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(senderId)
                            && !chat.isIsSeen()
                            && chat.getSender().equals(receiverId)){
                        countUnreadMessages += 1

                    }
                }
                if(countUnreadMessages != 0){
                    messageCountTxt.text = "($countUnreadMessages)"
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun retrieveMessage(senderId: String, receiverId: String, lastMessageTxt: TextView) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for(snap in snapshot.children){
                    val chat = snap.getValue(Chat::class.java)

                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                            || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)){
                        lastMessageTxt.text = chat.getMessage()

                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun getItemCount(): Int {
        return  mUsers.size   }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var userNameTxt: TextView = itemView.findViewById(R.id.username)
        var profileImageView: CircleImageView = itemView.findViewById(R.id.profile_image)
        var onlineImageView: CircleImageView = itemView.findViewById(R.id.image_online)
        var offlineImageView: CircleImageView = itemView.findViewById(R.id.image_offline)
        var lastMessageTxt: TextView = itemView.findViewById(R.id.message_last)
        var messageCountTxt: TextView = itemView.findViewById(R.id.message_count)

    }

}