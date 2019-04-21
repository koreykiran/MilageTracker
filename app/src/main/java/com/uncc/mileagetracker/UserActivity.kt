package com.uncc.mileagetracker

import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.PopupWindow
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
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.ride_details.view.*

class UserActivity : BaseActivity() {

    var userRef : DatabaseReference? = null
    var currentUser : FirebaseUser? = null
    var userProfile : User? = null
    var mAuth : FirebaseAuth? = null
    var ridesAdapter : RidesAdapter? = null
    var rides = ArrayList<Ride>()
    private var mPopupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = getFirebaseAuth()

        currentUser = mAuth!!.currentUser

        if(currentUser == null){
            finish()
            return
        }
        setContentView(R.layout.activity_user)

        val userId = getFirebaseAuth().currentUser!!.uid
        userRef = getDatabaseReference().child("Users").child(userId)

        ridesAdapter = RidesAdapter(this, rides!!)
        lvRides.adapter = ridesAdapter


        lvRides.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            openWindow(rides.get(position),position)
        }
    }

    override fun onResume() {
        super.onResume()

        if (currentUser != null) {

            userRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userProfile = dataSnapshot.getValue<User>(User::class.java)
                    loadActionBar()
                    loadRides()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        } else {
            Toast.makeText(this@UserActivity, "Something Went Wrong", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    fun loadRides(){
        rides.clear()
        userRef!!.child("rides").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot in snapshot.getChildren()) {
                    val ride = postSnapshot.getValue(Ride::class.java)
                    rides.add(ride!!)
                }

                ridesAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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
            Toast.makeText(this@UserActivity, "User Logged out", Toast.LENGTH_LONG).show()
            mAuth!!.signOut()
            finish()
        }
    }

    fun openWindow(ride: Ride,pos:Int){

        val inflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.ride_details, null)

        mPopupWindow = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        if(Build.VERSION.SDK_INT>=21){
            mPopupWindow!!.setElevation(5.0f);
        }

        val closeButton = customView.btnCancel

        closeButton.setOnClickListener {
            mPopupWindow!!.dismiss();
        }

        customView!!.tv_det_rideId.text = ride.id

        if(ride!!.startReading!=null){
            customView!!.et_Start.setText(ride!!.startReading!!.toString())
        }

        if(ride!!.endReading!=null){
            customView.et_end.setText(ride!!.endReading!!.toString())
        }

        if(ride!!.total!=null){
            customView.tv_det_totalVal.setText(ride!!.total!!.toString())
        }

        customView.btnSubmit.setOnClickListener{
            var end :String?=null
            if(isValidForm(customView)) {
                if (!customView.et_end.text.isBlank()) {
                    if(isValidEnd(customView)){
                        end = customView.et_end.text.toString()
                    }
                }
                calculateAndSubmit(pos, customView.et_Start.text.toString(), end)
            }
        }
        mPopupWindow!!.showAtLocation(userAc, Gravity.CENTER,0,0);
    }


    private fun isValidEnd(view : View) : Boolean{

        val start= Integer.parseInt(view.et_Start.text.toString())

        val end= Integer.parseInt(view.et_end.text.toString())

        if(end<start){
            view.et_end.error = "Reading cannot be smaller than Start reading"
            return false
        }
        return true
    }

    private fun isValidForm(view : View) : Boolean{

        if(view.et_Start.text.isBlank()){
            view.et_Start.error = "Please enter Start Reading"
            return false
        }
        return true
    }
    fun calculateAndSubmit(pos:Int,start:String,end:String?){
        Toast.makeText(this@UserActivity, "Ride details Submitted", Toast.LENGTH_LONG).show()

        var ride= rides[pos]
        ride.startReading=start.toInt()
        if(end!=null){
            ride.endReading=  end.toInt()
            ride.total = ride.endReading!! - ride.startReading!!
        }
        userRef!!.child("rides").child(""+pos).setValue(ride)
    }

}
