package com.example.bookapp

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
import com.example.bookapp.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class PdfAddActivity : AppCompatActivity() {

    // Thiết lập view binding activity_pdf_add --> ActivityPdfAddBinding
    private lateinit var binding: ActivityPdfAddBinding

    // Xác thực Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    // Tiến trình tiến trình (hiển thị trong quá trình tải lên PDF)
    private lateinit var progressDialog: ProgressDialog

    // ArrayList để chứa các loại PDF
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    // URI của PDF đã chọn
    private var pdfUri: Uri? = null

    // Thẻ để đánh dấu (sử dụng để ghi nhật ký)
    private val TAG = "PDF_ADD_TAG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo xác thực Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        // Tải danh mục PDF
        loadPdfCategories()

        // Thiết lập tiến trình tiến trình
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Vui lòng đợi")
        progressDialog.setCanceledOnTouchOutside(false)

        // Xử lý khi click nút "Quay lại"
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Xử lý khi click vào textview danh mục
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        // Xử lý khi click chọn PDF
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        // Xử lý khi click nút "Quay lại"
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Xử lý khi click nút "Tải lên"
        binding.submitBtn.setOnClickListener {
            // Bước 1: Xác thực dữ liệu
            // Bước 2: Tải lên PDF lên kho lưu trữ Firebase
            // Bước 3: Lấy URL của PDF đã tải lên
            // Bước 4: Tải thông tin PDF lên cơ sở dữ liệu Firebase

            validateData()
        }
    }


    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        // Xác thực dữ liệu
        Log.d(TAG, "validateData: validating data")

        // Lấy dữ liệu
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        // Xác thực dữ liệu
        if (title.isEmpty()) { // nếu 1 trong các thông tin trống
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Enter PDF...", Toast.LENGTH_SHORT).show()


        } else {
            // Dữ liệu đã được xác thực, bắt đầu tải lên
            uploadPdfToStorage()
        }

    }

    private fun uploadPdfToStorage() {
        // Bước 2: tải dữ liệu lên kho lưu trữ Firebase
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...")

        // Hiển thị hộp thoại tiến trình
        progressDialog.setMessage("Uploading PDF")
        progressDialog.show()

        // timestamp
        val timestamp = System.currentTimeMillis()

        // Đường dẫn của tập tin PDF trong kho lưu trữ Firebase
        val filePathAndName = "Books/$timestamp"

        // Tham chiếu lưu trữ đến vị trí được chỉ định
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        // Đưa tập tin PDF lên lưu trữ Firebase
        storageReference.putFile(pdfUri!!).addOnSuccessListener { taskSnapshot ->
            // Bước 3: Tải tập tin PDF lên thành công, tiến hành lấy URL
            Log.d(
                TAG, "uploadPdfToStorage: PDF uploaded now getting url..."
            )
            // Tạo tác vụ để lấy URL của tập tin vừa tải lên
            val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val uploadedPdfUrl = "${uriTask.result}"

            // Sau khi có URL, tiến hành tải thông tin PDF lên cơ sở dữ liệu Firebase
            uploadPdfInfoToDb(uploadedPdfUrl, timestamp)

        }.addOnFailureListener { e ->
            // Xảy ra lỗi trong quá trình tải lên
            Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
            // Tắt hộp thoại tiến trình và hiển thị thông báo lỗi
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // Bước 4: Tải thông tin PDF lên cơ sở dữ liệu Firebase
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        // Hiển thị hộp thoại tiến trình
        progressDialog.setMessage("Uploading pdf info...")

        // UID của người dùng hiện tại
        val uid = firebaseAuth.uid

        // Thiết lập dữ liệu cần tải lên
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewCount"] = 0
        hashMap["downloadsCount"] = 0

        // Tham chiếu đến cơ sở dữ liệu "Books"
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp").setValue(hashMap)
            .addOnSuccessListener {
                // Tải lên thành công
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT)
                    .show()
                pdfUri = null

            }.addOnFailureListener { e ->
                // Xảy ra lỗi trong quá trình tải lên
                Log.d(TAG, "uploadPdfInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun loadPdfCategories() {
        // Tải danh mục PDF từ cơ sở dữ liệu Firebase
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")

        // Khởi tạo danh sách arraylist
        categoryArrayList = ArrayList()
        // db reference to load categories DF > Categories

        // Tham chiếu đến cơ sở dữ liệu để tải danh mục
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Xóa danh sách trước khi thêm dữ liệu mới
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    // get data
                    val model = ds.getValue(ModelCategory::class.java)

                    // Thêm vào danh sách arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    // Khai báo biến để lưu trữ ID và tiêu đề của danh mục được chọn
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    // Hàm này hiển thị hộp thoại cho phép người dùng chọn một danh mục từ danh sách có sẵn
    private fun categoryPickDialog() {
        // Ghi log để theo dõi quá trình hiển thị hộp thoại
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        // Tạo một mảng chuỗi để lưu trữ tên của các danh mục
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        // Tạo hộp thoại lựa chọn danh mục
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            // Đặt danh sách lựa chọn bằng các danh mục từ mảng
            .setItems(categoriesArray) { dialog, which ->
                // Xử lý sự kiện khi một mục được chọn
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                // Hiển thị tiêu đề của danh mục được chọn lên TextView
                binding.categoryTv.text = selectedCategoryTitle

                // Ghi log để theo dõi ID và tiêu đề của danh mục được chọn
                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")

            }
            .show()
    }

    // Hàm này bắt đầu một Intent để chọn một tệp PDF từ bộ nhớ thiết bị
    private fun pdfPickIntent() {
        // Ghi log để theo dõi quá trình bắt đầu Intent
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        // Tạo một Intent
        val intent = Intent()

        // Đặt loại tệp cần chọn là PDF
        intent.type = "application/pdf"

        // Đặt hành động của Intent để lấy nội dung
        intent.action = Intent.ACTION_GET_CONTENT

        // Khởi chạy Intent để chọn tệp PDF và chờ kết quả trả về
        pdfActivityResultLauncher.launch(intent)
    }


    // Đăng ký một Activity Result Launcher để xử lý kết quả trả về từ Intent khi chọn tệp PDF
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            // Kiểm tra xem kết quả trả về có thành công không
            if (result.resultCode == RESULT_OK) {
                // Nếu thành công, ghi log và lấy đường dẫn của tệp PDF được chọn
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            } else {
                // Nếu không thành công, ghi log và hiển thị thông báo cho người dùng
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )



}