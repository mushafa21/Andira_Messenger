package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mushafaandira.kotlinmessenger.MainActivity
import com.mushafaandira.kotlinmessenger.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title = "Select User"

        //adapter.add(UserItem())

        FetchUser()


    }
    companion object{
        val USER_KEY = "USER_KEY"
    }

    fun FetchUser(){
        val adapter = GroupAdapter<GroupieViewHolder>()

        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    Log.d("NewMessage",it.toString())
                    val user = it.getValue(MainActivity.User::class.java)
                    if(user != null) adapter.add(UserItem(user))
                }
                adapter.setOnItemClickListener { item, view ->
                    val itemPerson = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity :: class.java)
                    //intent.putExtra(USER_KEY, itemPerson.user.username)
                    intent.putExtra(USER_KEY, itemPerson.user)
                    startActivity(intent)
                    finish()
                }
                recyclerview_newmessage.adapter = adapter
            }

        }
        )
    }
    class UserItem(val user : MainActivity.User): Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.user_row_new_message
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.username_textview_new_message.text = user.username
            Picasso.get().load(user.profileimageurl).into(viewHolder.itemView.image_imageview_new_message)

        }
    }
}