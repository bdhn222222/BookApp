package com.example.bookappyt

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Adapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.bookappyt.databinding.ActivityPdfDetailBinding
import com.example.bookappyt.databinding.DialogCommentAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream
import com.example.bookappyt.AdapterComment;
import com.google.firebase.database.DatabaseReference

class PdfDetailActivity : AppCompatActivity() {
    // view binding
    private lateinit var binding:ActivityPdfDetailBinding

    private companion object {
        const val TAG = "BOOK_DETAILS_TAG"
    }
    private val TAG_DOWNLOAD: String = "DOWNLOAD_TAG"
    private lateinit var firebaseAuth: FirebaseAuth

    private val pdfArrayList=ArrayList<ModelPdf>()
    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    private var bookId= ""
    private var bookTitle = ""
    private var bookUrl = ""
    private var isInMyFavorite = false

    private lateinit var progressDialog: ProgressDialog
    private var commentArrayList: ArrayList<ModelComment> = ArrayList()
    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // lay id book tu intent
        bookId = intent.getStringExtra("bookId")!!


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        firebaseAuth = FirebaseAuth.getInstance() // Lấy instance của FirebaseAuth
        if (firebaseAuth.currentUser != null) { // Kiểm tra xem người dùng hiện tại có đăng nhập không
            checkIsFavorite() // Gọi hàm kiểm tra sách có nằm trong danh sách yêu thích hay không
        }

        // hien thi luot doc cua book do
        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()
        loadComments();

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java) // khoi tao intent de giao tiep
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }


        binding.downloadBookBtn.setOnClickListener {
            // kiem tra quyen WES duoc cap hay chua
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook() // neu cap quyen thi tai xuong
            }
            else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted, request it")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) // neu chua thi phai yeu cau cap quyen tu nguoi dung
            }
        }
        binding.favoriteBtn.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                // here toast is not working? with favorite ones
                Toast.makeText(this@PdfDetailActivity, "You're not logged in", Toast.LENGTH_SHORT).show()
            } else {
                if (isInMyFavorite) {
                    // in favorite, remove from favorite
                    MyApplication.removeFromFavorite(this@PdfDetailActivity, bookId)
                } else {
                    // not in favorite, add to favorite
                    MyApplication.addToFavorite(this@PdfDetailActivity, bookId)
                }
            }
        }
        binding.addCommentBtn.setOnClickListener {
            /*Yêu cầu: Người dùng phải đăng nhập để thêm bình luận*/
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this@PdfDetailActivity, "You're not logged in...", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }

    }

    private var comment = ""

    private fun addCommentDialog() {
        // Inflate và bind view cho dialog
        val commentAddBinding = DialogCommentAddBinding.inflate(layoutInflater)

        // Thiết lập AlertDialog.Builder
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.backBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        commentAddBinding.submitBtn.setOnClickListener {
            // Get data
            comment = commentAddBinding.commentEt.text.toString().trim()
            // Validate data
            if (comment.isEmpty()) {
                Toast.makeText(this@PdfDetailActivity, "Enter your comment...", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }
    private fun loadComments() {
        // Khởi tạo ArrayList trước khi thêm dữ liệu vào
        commentArrayList = ArrayList()

        // Đường dẫn DB để tải nhận xét
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear ArrayList trước khi thêm dữ liệu vào
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelComment::class.java)
                        // Thêm vào ArrayList
                        model?.let { commentArrayList.add(it) }
                    }
                    // Thiết lập adapter
                    adapterComment = AdapterComment(this@PdfDetailActivity, commentArrayList)

                    // Đặt adapter cho RecyclerView
                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    private fun addComment() {
        progressDialog.setMessage("Adding comment")
        progressDialog.show()

        // Timestamp cho id của bình luận và thời gian bình luận
        val timestamp = System.currentTimeMillis().toString()

        // Thiết lập dữ liệu để thêm vào db cho bình luận
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["id"] = timestamp
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp
        hashMap["comment"] = comment
        hashMap["uid"] = firebaseAuth.uid!!

        // books > bookId > comments > commentId > commentData
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this@PdfDetailActivity, "Bình luận đã được thêm...", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this@PdfDetailActivity, "Không thể thêm bình luận do ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) { // kiem tra quyen duoc cap hay chua
            Log.d(TAG, "onCreate: STORAGE PERMISSION is granted")
            downloadBook()
        }
        else {
            Log.d(TAG, "onCreate: STORAGE PERMISSON is denied")
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadBook() {
        Log.d(TAG, "downloadBook: Downloading Book")
        progressDialog.setMessage("Downloading Book")
        progressDialog.show()

        // download book tu url trong db
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF) // tai max byte cua book
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Book Downloaded")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to download due to ${e.message}")
                Toast.makeText(this, "Failed to download due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadsFolder: Saving Downloaded Book")

        val namWithExtention = "${System.currentTimeMillis()}-$bookTitle.pdf" // su dung thoi gian hien tai + ten sach

        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) // lay thu muc de tai sach ve - neu chua co se khoi tao
            downloadsFolder.mkdirs()

            val filePath = downloadsFolder.path + "/" + namWithExtention

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this, "Saved to Downloads Folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownloadsFolder: Saved to Downloads Folder")
            progressDialog.dismiss()
            incrementDownloadCount() // so luot tai ve cua sach do
        }
        catch(e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadsFolder: Failed to Save due to ${e.message}")
            Toast.makeText(this, "Failed to Save due to ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        Log.d(TAG, "incrementDownloadCount: ")

        val ref = FirebaseDatabase.getInstance().getReference("Books") // tham chieu den Books trong DB
        ref.child(bookId) // chuyen den nut con BookId
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}" // lay so luot tai ve cua sach do
                    Log.d(TAG, "onDataChange: Current Downloads Count: $downloadsCount") // thay doi so luot tai ve

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }

                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New Downloads Count: $newDownloadCount")

                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap) // update them so luot downloads cua Book
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Downloads Count Incremented")
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "onDataChange: Failed to Increment due to ${e.message}")
                        }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books") // tham chieu den Book
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // lay du lieu cua Book
                    val categoryId = "" + snapshot.child("categoryId").value
                    val description = "" + snapshot.child("description").value
                    val downloadsCount = "" + snapshot.child("downloadsCount").value
                    val timestamp = "" + snapshot.child("timestamp").value
                    bookTitle = "" + snapshot.child("title").value
                    val uid = "" + snapshot.child("uid").value
                    bookUrl = "" + snapshot.child("url").value
                    val viewsCount = "" + snapshot.child("viewsCount").value

                    // format lai ngay thang
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    // lay du lieu de hien thi Category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    // lay du lieu de hien thi pdf thumbnail, so trang
                    MyApplication.loadPdfFromUrlSinglePage("$bookUrl", "$bookTitle", binding.pdfView, binding.progressBar, binding.pagesTv)
                    // lay du lieu de hien thi pdf size
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }
    private fun checkIsFavorite() {
        // Logged in, check if it's in the favorite list or not
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists() // true: if exists, false if not exists
                    if (isInMyFavorite) {
                        // Exists in favorite
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0)
                        binding.favoriteBtn.text = "Remove Favorite"
                    } else {
                        // Not exists in favorite
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0)
                        binding.favoriteBtn.text = "Add Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

}