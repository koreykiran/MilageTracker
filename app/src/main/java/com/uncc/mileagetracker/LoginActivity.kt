package com.uncc.mileagetracker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    var mAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = getFirebaseAuth()

        if(mAuth!!.currentUser != null){

            val intent = Intent(this, UserActivity::class.java).apply {
            }
            startActivity(intent)
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
    private fun performLogin(){

        if(etEmail.text.isBlank()){

            etEmail.error = "Don't you have one?"
            return
        }

        if(etPassword.text.isBlank()){


            etPassword.error = "Did you forgot something?"
            return
        }


        mAuth!!.signInWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    if( mAuth!!.currentUser!!.isEmailVerified) {

                        val intent = Intent(this, UserActivity::class.java).apply {
                        }
                        startActivity(intent)
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
