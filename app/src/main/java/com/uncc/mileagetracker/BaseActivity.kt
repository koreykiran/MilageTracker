package com.uncc.mileagetracker

import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.Spanned
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

open class BaseActivity: AppCompatActivity() {

    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var mStorage: StorageReference? = null

    protected var mProgressBar : ProgressDialog? = null

    protected fun getFirebaseDatabase() : FirebaseDatabase {

        if(mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance()

        return mDatabase!!
    }

    protected fun getDatabaseReference() : DatabaseReference {

        if(mDatabaseReference == null)
            mDatabaseReference = getFirebaseDatabase().reference!!

        return mDatabaseReference!!
    }

    protected fun getFirebaseAuth() : FirebaseAuth {

        if(mAuth == null)
            mAuth = FirebaseAuth.getInstance()

        return mAuth!!
    }

    protected fun getFirebaseStorage() : StorageReference {

        if(mStorage == null)
            mStorage = FirebaseStorage.getInstance().reference

        return mStorage!!
    }

    protected fun startProgressBar(msg : String, context : Context){

        mProgressBar = ProgressDialog(context);

        mProgressBar!!.setMessage(msg)
        mProgressBar!!.setCancelable(false)
        mProgressBar!!.show()

    }

    protected fun setProgressProgressBar(msg : String){

        mProgressBar!!.setMessage(msg)

    }

    protected fun stopProgressBar(){

        mProgressBar!!.cancel()

    }

    protected fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }
}