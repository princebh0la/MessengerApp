package com.example.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.PointerIconCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.FullScreenImageActivity
import com.example.messengerapp.MessageChatActivity
import com.example.messengerapp.ModelClasses.Chat
import com.example.messengerapp.ProfileVisitActivity
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class ChatsAdapter(
        mContext: Context,
        mChatList: List<Chat>,
        imageUrl: String
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>()
{
    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String

    var firebaseUser: FirebaseUser =FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList = mChatList
        this.mContext = mContext
        this.imageUrl = imageUrl
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if(position == 1){
            val view : View = LayoutInflater.from(mContext).inflate(R.layout.message_item_right,parent,false)
            ViewHolder(view)
        }
        else{
            val view : View = LayoutInflater.from(mContext).inflate(R.layout.message_item_left,parent,false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]

        Picasso.get().load(imageUrl).into(holder.profile_image)

        if(chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals(""))
        {
            if(chat.getSender().equals(firebaseUser!!.uid)){
                holder.show_text_message!!.visibility = View.GONE
                holder.show_text_time!!.visibility = View.GONE
                holder.right_image_view!!.visibility = View.VISIBLE
                holder.show_image_time!!.text = chat.getTime()
                holder.show_image_time!!.visibility = View.VISIBLE

                Picasso.get().load(chat.getUrl()).into(holder.right_image_view)

                val chatRef = FirebaseDatabase.getInstance().reference
                        .child("Chats").child(chat.getMessageId()!!)
                chatRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists() && chat.getSender().equals(firebaseUser!!.uid)){
                            holder.right_image_view!!.setOnClickListener{
                                val options = arrayOf<CharSequence>(
                                        "See Full Image",
                                        "Deleted Message",
                                        "Cancel"
                                )
                                val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
                                builder.setTitle("What do you want to do")
                                builder.setItems(options, DialogInterface.OnClickListener {
                                    dialog, which ->
                                    if(which == 0){
                                        val chat = snapshot.getValue(Chat::class.java)
                                        val intent = Intent(mContext, FullScreenImageActivity::class.java)
                                        intent.putExtra("Url", chat!!.getUrl())
                                        mContext.startActivity(intent)
                                    }

                                    if(which == 1){
                                        chatRef.removeValue()
                                        Toast.makeText(mContext, "Message Deleted",Toast.LENGTH_LONG).show()
                                    }
                                    if(which == 2){

                                    }
                                })
                                builder.show()

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }
            else if(!chat.getSender().equals(firebaseUser!!.uid)){
                holder.show_text_message!!.visibility = View.GONE
                holder.show_text_time!!.visibility = View.GONE
                holder.left_image_view!!.visibility = View.VISIBLE
                holder.show_image_time!!.text = chat.getTime()
                holder.show_image_time!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.left_image_view)

                holder.left_image_view!!.setOnClickListener{
                    val intent = Intent(mContext, FullScreenImageActivity::class.java)
                    intent.putExtra("Url", chat!!.getUrl())
                    mContext.startActivity(intent)
                }

            }
        }
        else
        {
            holder.show_text_time!!.text = chat.getTime()

            holder.show_text_message!!.text = chat.getMessage()

            val chatRef = FirebaseDatabase.getInstance().reference
                    .child("Chats").child(chat.getMessageId()!!)
            chatRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists() && chat.getSender().equals(firebaseUser!!.uid)){
                        holder.show_text_message!!.setOnClickListener{
                            val options = arrayOf<CharSequence>(
                                    "Deleted Message",
                                    "Cancel"
                            )
                            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
                            builder.setTitle("Do you want to delete this message?")
                            builder.setItems(options, DialogInterface.OnClickListener {
                                dialog, which ->
                                if(which == 0){
                                    chatRef.removeValue()
                                    Toast.makeText(mContext, "Message Deleted",Toast.LENGTH_LONG).show()
                                }
                                if(which == 1){

                                }
                            })
                            builder.show()

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        }

        if (position == mChatList.size -1){
            if(chat.isIsSeen()){
                holder.text_seen!!.text = "seen"

                if(chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")){
                    val la : RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    la!!.setMargins(0,255,10,0)
                    holder.text_seen!!.layoutParams =la
                }
            }
            else{
                holder.text_seen!!.text = "sent"

                if(chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals("")){
                    val la : RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    la!!.setMargins(0,700,10,0)
                    holder.text_seen!!.layoutParams =la
                }
            }
        }
        else
        {
            holder.text_seen!!.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var profile_image : CircleImageView? = null
        var show_text_message: TextView? = null
        var left_image_view: ImageView? = null
        var text_seen: TextView? = null
        var right_image_view: ImageView? = null
        var show_text_time: TextView? = null
        var show_image_time: TextView? = null

        init {
            profile_image =itemView.findViewById(R.id.profile_image)
            show_text_message =itemView.findViewById(R.id.show_text_message)
            left_image_view =itemView.findViewById(R.id.left_image_view)
            text_seen =itemView.findViewById(R.id.text_seen)
            right_image_view =itemView.findViewById(R.id.right_image_view)
            show_text_time =itemView.findViewById(R.id.show_text_time)
            show_image_time =itemView.findViewById(R.id.show_image_time)

        }

    }

    override fun getItemViewType(position: Int): Int {

        return if(mChatList[position].getSender().equals(firebaseUser!!.uid)){
            1
        }
            else
        {
            0
        }
    }
}