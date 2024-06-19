package com.example.bookappyt

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookappyt.R
import com.example.bookappyt.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityProfileEditBinding

    //firebase auth, get/update user data using uid
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    private val TAG = "PROFILE_EDIT_TAG"

    private var imageUri: Uri? = null

    private var name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileTv.setOnClickListener {
            showImageAttachMenu()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }
    private fun loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user info of user ${firebaseAuth.uid}")

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Lay tat ca du lieu cua nguoi dung o day tu snapshot
                    val email = snapshot.child("email").value.toString()
                    val name = snapshot.child("name").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()
                    val timestamp = snapshot.child("timestamp").value.toString()
                    val uid = snapshot.child("uid").value.toString()
                    val userType = snapshot.child("userType").value.toString()

                    binding.nameEt.setText(name)

                    Glide.with(this@ProfileEditActivity)
                        .load(profileImage)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileTv)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
    private fun validateData() { // xac thuc du lieu
        name = binding.nameEt.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter name...", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                // Need to update without image
                updateProfile("")
            } else {
                // Need to update with image
                uploadImage()
            }
        }
    }
    private fun uploadImage() {
        Log.d(TAG, "uploadImage: Uploading profile image...")
        progressDialog.setMessage("Updating profile image")
        progressDialog.show()

        // Đường dẫn và tên tệp ảnh, sử dụng uid để thay thế ảnh trước đó
        val filePathAndName = "ProfileImages/${firebaseAuth.uid}"

        // Tham chiếu đến Firebase Storage
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        imageUri?.let {
            reference.putFile(it)
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(TAG, "onSuccess: Profile image uploaded")
                    Log.d(TAG, "onSuccess: Getting url of uploaded image")
                    val uriTask = taskSnapshot.storage.downloadUrl
                    uriTask.addOnSuccessListener { uri ->
                        val uploadImageUrl = uri.toString()
                        Log.d(TAG, "onSuccess: Uploaded Image URL :$uploadImageUrl")
                        updateProfile(uploadImageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "onFailure: Failed to upload image due to ${e.message}")
                    progressDialog.dismiss()
                    Toast.makeText(this@ProfileEditActivity, "Failed to upload image due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun updateProfile(imageUrl: String?) {
        Log.d(TAG, "updateProfile: Updating user profile")
        progressDialog.setMessage("Updateing user profile...")
        progressDialog.show()

        // Chuan bi du lieu de cap nhat
        val hashMap = HashMap<String, Any>()
        hashMap["name"] = name
        imageUrl?.let { hashMap["profileImage"] = it }

        // Cap nhat vao db
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "onSuccess: Profile updated...")
                progressDialog.dismiss()
                Toast.makeText(this@ProfileEditActivity, "Profile updated...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "onFailure: Failed to update db due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this@ProfileEditActivity, "Failed to update db dur to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showImageAttachMenu() {
        // Init/setup popup menu
        val popupMenu = PopupMenu(this, binding.profileTv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")

        popupMenu.show()

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            // Get id of item clicked
            val which = menuItem.itemId
            when (which) {
                0 -> {
                    // Camera clicked
                    pickImageCamera()
                }
                1 -> {
                    // Gallery clicked
                    pickImageGallery()
                }
            }
            false
        }
    }
    private fun pickImageCamera() {
        // Trien khai logic de mo may anh va chup anh
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Pick")
            put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        // Mo album trong thu vien dien thoai
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }
    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Xử lý kết quả của intent máy ảnh
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult Picked Form camera: $imageUri")
                val data = result.data
                binding.profileTv.setImageURI(imageUri)
            } else {
                Toast.makeText(this@ProfileEditActivity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Xử lý kết quả của intent thư viện ảnh
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data?.data
                Log.d(TAG, "onActivityResult: Picked From Gallery $imageUri")
                binding.profileTv.setImageURI(imageUri)
            } else {
                Toast.makeText(this@ProfileEditActivity, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }






}
