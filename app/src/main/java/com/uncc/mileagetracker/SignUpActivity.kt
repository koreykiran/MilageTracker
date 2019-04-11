package com.uncc.mileagetracker


import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.uncc.mileagetracker.Helpers.ImageHelper
import com.uncc.mileagetracker.Models.User
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.terms_conditions.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class SignUpActivity : BaseActivity() {

    val TAKE_PHOTO_REQUEST  = 1010;
    var mCurrentPhotoPath = ""
    var profileImageURL = ""
    var bitmap : Bitmap? = null
    private var mPopupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val actionBar = supportActionBar
        actionBar!!.setDisplayShowHomeEnabled(true)
        actionBar.setLogo(R.mipmap.ic_norm)
        actionBar.setDisplayUseLogoEnabled(true)


        btnCancel.setOnClickListener {
            finish()
        }

        btnSignUp.setOnClickListener {
            registerUser()
        }

        imgProfilePic.setOnClickListener{

            validatePermissions()
        }

        tvAccept.setOnClickListener{

            openWindow()
        }

    }

    private fun registerUser(){

        if(isvalidForm()){
            startProgressBar("Registering User...", this@SignUpActivity)

          if(bitmap != null)
              uploadProfilePic()
           else
            saveUserToDB()
        }

    }

    private fun isvalidForm() : Boolean
    {

        if(etFirstName.text.isBlank()){
            etFirstName.error = "Don't you have one?"
            return false
        }

        if(etLastName.text.isBlank()){
            etLastName.error = "Forgot to enter last name?"
            return false
        }

        if(etDescription.text.isBlank()){
            etDescription.error = "Write few words about you."
            return false
        }

        if(etEmail.text.isBlank()){
            etEmail.error = "We need one to identify you"
            return false
        }

        if(!etEmail.text.contains("@uncc.edu")){
            etEmail.error = "You should use you UNCC id"
            return false
        }

        if(etPassword.text.isBlank()){
            etPassword.error = "You should need one"
            return false
        }

        if(etPassword2.text.isBlank()){
            etPassword2.error = "Let's make sure"
            return false
        }

        if(etPassword.text.length < 6){
            etPassword.error = "Consider a little longer password. (MIN:6)"
            return false
        }

        if(etPassword2.text.toString() != etPassword.text.toString() ){
            etPassword.error = "You need only one password"
            etPassword2.error = "You need only one password"
            return false
        }

//        if(profileImageURL.isNullOrEmpty()){
//            Toast.makeText(this,"Upload Profile Pic", Toast.LENGTH_LONG).show()
//            return false
//        }

        if(!cbAcceptTerms.isChecked){
            Toast.makeText(this,"Please read the terms and Conditions", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun uploadProfilePic(){

        val baos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val dataBytes = baos.toByteArray()

        var storageRef = getFirebaseStorage();

        val path = "images/" + UUID.randomUUID() + ".png"
        var sref = storageRef.child(path)

        val upload = sref.putBytes(dataBytes)

        upload.addOnProgressListener { taskSnapshot ->
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            setProgressProgressBar("uploading " + Math.round(progress * 100.0) / 100.0 + " %")

        }.addOnSuccessListener(this@SignUpActivity) { taskSnapshot ->
            // Handle successful uploads on complete
            val downloadUrlTask = taskSnapshot.storage.downloadUrl

            setProgressProgressBar("Registering User...... " )

            downloadUrlTask.addOnSuccessListener { uri -> profileImageURL = uri.toString() }

            saveUserToDB()
        }
    }

    private fun saveUserToDB(){
        getFirebaseAuth()!!
            .createUserWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
            .addOnCompleteListener(this){ task ->

                stopProgressBar()

                if (task.isSuccessful) {

                    val userId = getFirebaseAuth().currentUser!!.uid
                    //Verify Email
                    verifyEmail()
                    //update user profile information
                    val currentUserDb = getDatabaseReference().child("Users").child(userId)


                    var gender = if(rbMale.isChecked) "Male" else if(rbFemale.isChecked ) "Female" else ""

                    val user = User(etFirstName.text.toString(),
                        etLastName.text.toString(),
                        etDescription.text.toString(),
                        etEmail.text.toString(),
                        profileImageURL,
                        gender,
                        userId,
                        false)

                    currentUserDb.setValue(user)
                    //updateUserInfoAndUI()
                } else {
                    Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun verifyEmail() {

        val mUser = getFirebaseAuth().currentUser;
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@SignUpActivity,
                        "Verification email sent to " + mUser.getEmail(),
                        Toast.LENGTH_SHORT).show()

                    finish()
                } else {
                    Toast.makeText(this@SignUpActivity,

                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validatePermissions() {


        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if(report?.isAnyPermissionPermanentlyDenied!!){

                        Toast.makeText(this@SignUpActivity, "Permission denied",
                            Toast.LENGTH_SHORT).show()
                    }
                    else{
                        launchCamera()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    token?.continuePermissionRequest();
                }


            })
            .check()
    }

    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = contentResolver
            .insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            mCurrentPhotoPath = fileUri.toString()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        if (resultCode == Activity.RESULT_OK
            && requestCode == TAKE_PHOTO_REQUEST) {
            processCapturedPhoto()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun processCapturedPhoto() {
        val cursor = contentResolver.query(
            Uri.parse(mCurrentPhotoPath),
            Array(1) {android.provider.MediaStore.Images.ImageColumns.DATA},
            null, null, null)
        cursor.moveToFirst()
        val photoPath = cursor.getString(0)
        cursor.close()
        val file = File(photoPath)
        val uri = Uri.fromFile(file)

        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        imgProfilePic.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bitmap!!,30));


    }


    fun openWindow(){

        val inflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.terms_conditions, null)

        mPopupWindow = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if(Build.VERSION.SDK_INT>=21){
            mPopupWindow!!.elevation = 5.0f
        }

        val closeButton = customView.btnClose

        customView!!.tvTerms.text = fromHtml(buildTerms())

        closeButton.setOnClickListener {
            mPopupWindow!!.dismiss()
        }

        mPopupWindow!!.showAtLocation(root, Gravity.CENTER,0,0)


    }

    private fun buildTerms() : String{

        var terms = "<ol>\n" +
                "  <li>The user must be an UNCC student or faculty or employee</li>\n" +
                "  <li>Make sure you have valid license permit.</li>\n" +
                "  <li>Maintain Integrity when entering the data into the application</li>\n" +
                "</ol> "

        return terms
    }

}
