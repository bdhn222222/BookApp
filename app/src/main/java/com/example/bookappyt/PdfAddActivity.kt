package com.example.bookappyt

import android.app.AlertDialog
import android.app.Application.ActivityLifecycleCallbacks
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.ColorSpace.Model
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookappyt.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class   PdfAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // progress dialog hien thi khi dang tai pdf
    private lateinit var progressDialog: ProgressDialog

    // arraylist to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    // uri of picked pdf
    private var pdfUri: Uri? = null

    // TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // khoi tao fb
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        //
        binding.categoryTv.setOnClickListener {
            categoryPickDialog() // show pdf khi pick category
        }

        // link pdf
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // add pdf/book
        binding.submitBtn.setOnClickListener {
            // ste1: xac thuc data
            // step2: upload pdf vao firebase storage
            // step3: lay url cua pdf
            // step4: cap nhat pdf len fb

            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    // xac thuc du lieu
    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        // lay du lieu
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        // dieu kien xac thuc du lieu
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Enter PDF...", Toast.LENGTH_SHORT).show()

        } else {
            // upload pdf
            uploadPdfToStorage()
        }

    }

    private fun uploadPdfToStorage() {
        // step2: upload data vao firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...")

        // show progress dialog
        progressDialog.setMessage("Uploading PDF")
        progressDialog.show()

        // timestamp
        val timestamp = System.currentTimeMillis()

        // bien nay luu tru duong dan vs ten cua PDF tren FB storage
        val filePathAndName = "Books/$timestamp"


        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!).addOnSuccessListener { taskSnapshot ->
            Log.d(
                TAG, "uploadPdfToStorage: PDF uploaded now getting url..."
            )
            // step3: lay url cua pdf
            // khoi tao task de lay url cua pdf
            // taskSnapshot la ket qua cua viec tai tep len
            val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful); //chay vong lap cho den khi task lay thanh cong
            val uploadedPdfUrl = "${uriTask.result}" //co nghia task da thanh cong va duoc luu tru ket qua o day

            uploadPdfInfoToDb(uploadedPdfUrl, timestamp) // thuc thi

        }.addOnFailureListener { e ->
            Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // step4: upload pdf vao to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info...")

        // lay uid cua nguoi dung hien tai
        val uid = firebaseAuth.uid

        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl" //lay url vua tai xuong
        hashMap["timestamp"] = timestamp
        hashMap["viewCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp").setValue(hashMap) //them du lieu vao db tham chieu den nut timestam cua Book
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "uploaded...", Toast.LENGTH_SHORT)
                    .show()
                pdfUri = null
            }.addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")
        //khoi tao mang
        categoryArrayList = ArrayList()
        // tham chieu den Categories trong DB
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear truoc khi add lai category
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    // lay data
                    val model = ds.getValue(ModelCategory::class.java)

                    // them vao mang
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    // show pdf khi pick category
    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //lay mang cua category
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size) // lay kich thuoc mang cua categories
        for (i in categoryArrayList.indices) { // duyet qua tat ca cac phan tu trong mang
            categoriesArray[i] = categoryArrayList[i].category // gan gia tri category - mang nay la chuoi chua cac category
        }

        // alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                // khi click -> gan
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")

            }
            .show()
    }

    private fun pdfPickIntent() { // khoi tao intent
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent) // lay du lieu cua tai nguyen da duoc chon
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result -> // xu ly ket qua tra ve tu Intent
            if (result.resultCode == RESULT_OK) { // URL cua pdf duoc chon se duoc lay va gan cho Uri
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF Pick cancelled")  // url cua PDF do da bi huy
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()

            }
        }
    )


}