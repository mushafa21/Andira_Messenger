package com.example.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.models.chatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mushafaandira.kotlinmessenger.MainActivity
import com.mushafaandira.kotlinmessenger.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_row2_chat_log.view.*
import kotlinx.android.synthetic.main.chat_row_chat_log.*
import kotlinx.android.synthetic.main.chat_row_chat_log.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class ChatLogActivity : AppCompatActivity() {
    var toUser: MainActivity.User? = null
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        //intent.getStringExtra(NewMessageActivity.USER_KEY)
        recyclerview_chat_log.adapter = adapter
        toUser = intent.getParcelableExtra<MainActivity.User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser!!.username
        //TestSendMessage()
        ListenForMessages()
        send_button_chat_log.setOnClickListener {
            Log.d("ChatLog","Button terklik")
            SendMessage()
        }
    }
    private fun ListenForMessages(){
        val fromid = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user_messages/$fromid/$toId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(chatMessage::class.java)
                val currentUser = LatestMessagesActivity.currentUser ?: return
                if (chatMessage != null) {
                    Log.d("ChatLog",chatMessage.text)
                    if(FirebaseAuth.getInstance().uid == chatMessage.toId){
                        adapter.add(ChatFromItem(chatMessage.text, toUser!!))
                    }
                    else{
                        adapter.add(ChatToItem(chatMessage.text,currentUser))
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
        })
    }
    private fun SendMessage(){
        val user = intent.getParcelableExtra<MainActivity.User>(NewMessageActivity.USER_KEY)
        val text = message_textview_chat_log.text.toString()
        val fromid = FirebaseAuth.getInstance().uid
        val toId = user.uid
        if (fromid == null) return
        //val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val ref = FirebaseDatabase.getInstance().getReference("/user_messages/$fromid/$toId").push()
        val ref2 = FirebaseDatabase.getInstance().getReference("/user_messages/$toId/$fromid").push()
        val reflatestMessages = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromid/$toId")
        val refLatestMessages2 = FirebaseDatabase.getInstance().getReference("/latest_messages/$toId/$fromid")
        val chatMessage = chatMessage(ref.key!!,fromid,toId,text,System.currentTimeMillis() / 1000)
        ref.setValue(chatMessage).addOnSuccessListener {
            Log.d("ChatLog","Berhasil menyimpan pesan : ${ref.key}")
            message_textview_chat_log.text.clear()
            recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
        }
        ref2.setValue(chatMessage)
        reflatestMessages.setValue(chatMessage)
        refLatestMessages2.setValue(chatMessage)

    }
    /*private fun TestSendMessage(){
        val text1 = "Assalamualaikum"
        val text2 = "Waalaikumsalam"
        val adapter = GroupAdapter<GroupieViewHolder>()
        recyclerview_chat_log.adapter = adapter
        adapter.add(ChatFromItem(text1))
        adapter.add(ChatToItem(text2))
        adapter.add(ChatToItem("Halo gan"))
        adapter.add(ChatFromItem("Gimana gan?"))
        adapter.add(ChatFromItem("lagi sibuk nih"))
    }*/

    class ChatFromItem(val text: String, val user: MainActivity.User): Item<GroupieViewHolder>() {


        override fun getLayout(): Int {
            return R.layout.chat_row_chat_log
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textview_chat_log.text = text
            val uri = user.profileimageurl
            val target = viewHolder.itemView.image_imageview_chat_log
            Picasso.get().load(uri).into(target)
        }

    }
    class ChatToItem(val text : String, val user: MainActivity.User): Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.chat_row2_chat_log
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textview_chat_log2.text = text
            val uri = user.profileimageurl
            val target = viewHolder.itemView.image_imageview_chat_log2
            Picasso.get().load(uri).into(target)
        }

    }
}