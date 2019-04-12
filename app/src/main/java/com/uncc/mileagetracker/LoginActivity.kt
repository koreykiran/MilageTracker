package com.uncc.mileagetracker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.uncc.mileagetracker.Models.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    var mAuth : FirebaseAuth? = null
    var userRef : DatabaseReference? = null
    var currentUser : FirebaseUser? = null
    var userProfile : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = getFirebaseAuth()

        currentUser = mAuth!!.currentUser

        if(currentUser != null){
            whichUser(currentUser!!.uid)
        }


        setContentView(R.layout.activity_login)

        val actionBar = supportActionBar
        actionBar!!.setDisplayShowHomeEnabled(true)
        actionBar.setLogo(R.mipmap.ic_norm)
        actionBar.setDisplayUseLogoEnabled(true)

        txtSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java).apply {
            }
            startActivity(intent)

        }

        btnLogin.setOnClickListener {

            performLogin()
        }
    }

    private fun whichUser(userId:String){

        userRef = getDatabaseReference().child("Users").child(userId)
        if (currentUser != null) {

            userRef!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userProfile = dataSnapshot.getValue<User>(User::class.java)
                    if(userProfile!=null)
                        loadActivity()
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }else{
            Toast.makeText(this@LoginActivity, "Something went wrong", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadActivity(){
        if(userProfile!!.isAdmin!!){
            Toast.makeText(this@LoginActivity, "Admin Activity", Toast.LENGTH_LONG).show()
            val intent = Intent(this, AdminActivity::class.java).apply {
            }
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this@LoginActivity, "User Activity", Toast.LENGTH_LONG).show()
            val intent = Intent(this, UserActivity::class.java).apply {
            }
            startActivity(intent)
            finish()
        }
    }

    private fun performLogin(){

        if(etEmail.text.isBlank()){

            etEmail.error = "Don't you have one?"
            return
        }

        if(etPassword.text.isBlank()){


            etPassword.error = "Did you forgot something?"
            return
        }


        mAuth!!.signInWithEmailAndPassword(etEmail.text.toString().trim(), etPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    if( mAuth!!.currentUser!!.isEmailVerified) {
                        currentUser=mAuth!!.currentUser
                        whichUser(currentUser!!.uid)
                    }
                    else

                        Toast.makeText(this@LoginActivity, "Please Verify your Email address",
                            Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this@LoginActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
