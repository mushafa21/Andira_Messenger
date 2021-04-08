package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.models.chatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mushafaandira.kotlinmessenger.MainActivity
import com.mushafaandira.kotlinmessenger.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessagesActivity : AppCompatActivity() {
    companion object {
        var currentUser: MainActivity.User? = null
    }
    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val namaUser = FirebaseAuth.getInstance().uid
        Log.d("LatestMessages","UID = $namaUser")
        setContentView(R.layout.activity_latest_messages)
        latest_messages_recyclerview.adapter = adapter
        latest_messages_recyclerview.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        verifyUserUid()
        FetchCurrentUser()
        //TestRecycler()
        ListenLatestMessages()
        adapter.setOnItemClickListener { item, view ->
            Log.d("Latest","bisa")
            val intent = Intent(this, ChatLogActivity::class.java)
            var row = item as latestMessagesRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }
    }
    private fun ListenLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(chatMessage::class.java) ?: return
                adapter.add(latestMessagesRow(chatMessage))
                latestMessageMap[snapshot.key!!] = chatMessage
                RefreshLatestMessageRecycler()
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(chatMessage::class.java) ?: return
                adapter.add(latestMessagesRow(chatMessage))
                latestMessageMap[snapshot.key!!] = chatMessage
                RefreshLatestMessageRecycler()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

        })
    }
    private fun RefreshLatestMessageRecycler(){
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(latestMessagesRow(it))
        }
    }
    /*private fun TestRecycler(){
        val adapter = GroupAdapter<GroupieViewHolder>()
        latest_messages_recyclerview.adapter = adapter
        adapter.add(latestMessagesRow())
        adapter.add(latestMessagesRow())
        adapter.add(latestMessagesRow())
    }*/
    val latestMessageMap = HashMap<String, chatMessage>()
    class latestMessagesRow(val chatMessage: chatMessage): Item<GroupieViewHolder>(){
        var chatPartnerUser: MainActivity.User? = null
        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.messages_latest_messages_row.text = chatMessage.text
            val chatPartnerId : String
            if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            } else{
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser = snapshot.getValue(MainActivity.User::class.java)
                    viewHolder.itemView.username_latest_messages_row.text = chatPartnerUser?.username
                    Picasso.get().load(chatPartnerUser?.profileimageurl).into(viewHolder.itemView.imageview_latest_messages_row)
                }

            })
        }

    }
    private fun FetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(MainActivity.User::class.java)
                Log.d("LatestMessages","Current user = ${currentUser?.username}")
            }

        })
    }
    fun verifyUserUid(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return(super.onCreateOptionsMenu(menu))
    }
}