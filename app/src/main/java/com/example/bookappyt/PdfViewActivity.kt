package com.example.bookappyt

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappyt.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pdftron.pdf.config.ViewerConfig
import com.pdftron.pdf.controls.DocumentActivity
import java.io.File

class PdfViewActivity : AppCompatActivity() {

    // View binding
    private lateinit var binding: ActivityPdfViewBinding

    private var bookId: String? = null

    companion object {
        private const val TAG = "PDF_VIEW_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy bookId từ intent đã được truyền vào
        val intent = intent
        bookId = intent.getStringExtra("bookId")
        Log.d(TAG, "onCreate: BookId $bookId")

        loadBookDetails()

        // Xử lý click, quay lại
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get Pdf Url from db")
        // Tham chiếu cơ sở dữ liệu để lấy chi tiết sách, ví dụ như lấy URL của sách bằng bookId
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("url").getValue(String::class.java) ?: ""
                    Log.d(TAG, "onDataChange: PDF URL $pdfUrl")

                    // Tải PDF bằng cách sử dụng URL từ Firebase Storage
                    loadBookFromUrl(pdfUrl)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi nếu cần thiết
                }
            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadBookFromUrl: Get PDF from storage")
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                // Load pdf using bytes
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false) // Set false to scroll vertical, set true to swipe horizontal
                    .onPageChange { page, pageCount ->
                        // Set current and total pages in toolbar subtitle
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "onPageChanged: $currentPage/$pageCount")
                    }
                    .onError { t ->
                        Log.d(TAG, "onError: ${t.message}")
                        Toast.makeText(this@PdfViewActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "onPageError: ${t.message}")
                        Toast.makeText(this@PdfViewActivity, "Error on page: $page ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "onFailure: ${e.message}")
                // Failed to load book
                binding.progressBar.visibility = View.GONE
            }
    }
}