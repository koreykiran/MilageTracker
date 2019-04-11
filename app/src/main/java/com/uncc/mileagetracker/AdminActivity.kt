package com.uncc.mileagetracker

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.hendraanggrian.pikasso.circle
import com.hendraanggrian.pikasso.picasso
import com.uncc.mileagetracker.Models.User
import kotlinx.android.synthetic.main.actionbar_layout.view.*

class AdminActivity : BaseActivity() {

    var userRef : DatabaseReference? = null
    var currentUser : FirebaseUser? = null
    var userProfile : User? = null
    var mAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mAuth = getFirebaseAuth()

        currentUser = mAuth!!.currentUser

        if(currentUser == null){
            finish()
            return
        }
        setContentView(R.layout.activity_admin)

        val userId = getFirebaseAuth().currentUser!!.uid
        userRef = getDatabaseReference().child("Users").child(userId)
    }

    override fun onResume() {
        super.onResume()

        if (currentUser != null) {

            userRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userProfile = dataSnapshot.getValue<User>(User::class.java)
                    loadActionBar()

                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        } else {
            Toast.makeText(this@AdminActivity, "Something Went Wrong", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    fun loadActionBar(){

        val actionBar = supportActionBar

        val actionBarLayout = getLayoutInflater().inflate(R.layout.actionbar_layout, null)

        actionBar!!.setDisplayShowTitleEnabled(false);
        actionBar!!.setDisplayShowCustomEnabled(true);
        actionBar!!.setDisplayHomeAsUpEnabled(false);
        actionBar!!.setCustomView(actionBarLayout);

        actionBarLayout!!.tvName.text = userProfile!!.firstName!! + " " + userProfile!!.lastName!!

        if(!userProfile!!.photo.isNullOrEmpty())
            picasso.load(userProfile!!.photo!!).circle().into(actionBarLayout.imgProfilePic)
        else
            actionBarLayout.imgProfilePic.setImageResource(R.mipmap.ic_norm)

        actionBarLayout!!.logout.setOnClickListener{
            Toast.makeText(this@AdminActivity, "Admin Logged out", Toast.LENGTH_LONG).show()
            mAuth!!.signOut()
            finish()
        }
    }
}
