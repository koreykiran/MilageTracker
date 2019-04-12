package com.uncc.mileagetracker

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
import com.uncc.mileagetracker.Models.Ride
import com.uncc.mileagetracker.Models.User
import kotlinx.android.synthetic.main.actionbar_layout.view.*
import kotlinx.android.synthetic.main.activity_admin.*

class AdminActivity : BaseActivity() {

    var userRef : DatabaseReference? = null
    var currentUser : FirebaseUser? = null
    var userProfile : User? = null
    var mAuth : FirebaseAuth? = null
    var users= ArrayList<User>()

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


        addRide.setOnClickListener{
            val email= userEmail.text.toString()
            val rideId= rideId.text.toString()
            if( isValidForm() && email!=null && rideId!=null)
                addRideForUser(email.trim(),rideId)
        }
    }

    private fun isValidForm() : Boolean{
        if(userEmail.text.isBlank()){
            userEmail.error = "Please enter EmailId of the User"
            return false
        }

        if(rideId.text.isBlank()){
            rideId.error = "Please enter Id for the Ride"
            return false
        }

        return true
    }

    private fun addRideForUser(email:String, id:String){
        var userFound :Boolean?=false

        for(user in users!!){
            if(user!!.email.equals(email)){
                userFound=true
                var ride =  Ride()
                ride.id=id

                if(user.rides==null)
                    user.rides=ArrayList()
                user!!.rides!!.add(ride!!)
                val currentUserDb = getDatabaseReference().child("Users").child(user!!.id!!)
                currentUserDb.setValue(user);
                Toast.makeText(this@AdminActivity, "Ride Added Successfully", Toast.LENGTH_LONG).show()
                userEmail.setText("")
                rideId.setText("")
            }
        }

        if(!userFound!!)
            Toast.makeText(this@AdminActivity, "User Not Found", Toast.LENGTH_LONG).show()
    }

    fun loadAllUsers(){

        getDatabaseReference().child("Users").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot in snapshot.getChildren()) {
                    val user = postSnapshot.getValue(User::class.java)
                    users!!.add(user!!)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

    }

    override fun onResume() {
        super.onResume()

        if (currentUser != null) {

            userRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userProfile = dataSnapshot.getValue<User>(User::class.java)
                    loadActionBar()
                    loadAllUsers()
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
