package com.example.bookappyt

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

import java.util.*
import kotlin.collections.HashMap

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        // tao 1 phuong thuc convert Time cho luon ca project
        const val TAG_DOWNLOAD = "DOWNLOAD_TAG"
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            // format dd/mm/yy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        // function de lay Pdf Size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            // su dung url tu db
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata.addOnSuccessListener { storageMetaData ->
                Log.d(TAG, "loadPdfSize: got metadata") // lay duoc thanh cong
                val bytes = storageMetaData.sizeBytes.toDouble()
                Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                // chuyen doi bytes to KB/MB
                val kb = bytes / 1024
                val mb = kb / 1024
                if (mb >= 1) { // neu pdf co kich thuoc lon hon 1MB
                    sizeTv.text = "${String.format("%.2f", mb)} MB" // hien thi voi kich thuoc MB
                } else if (kb >= 1) { // 1MB > PDF size >= 1KB
                    sizeTv.text = "${String.format("%.2f", kb)} KB" // hien thi voi kich thuoc KB
                } else {
                    sizeTv.text = "${
                        String.format("%.2f", bytes)
                    } bytes"

                }

            }.addOnFailureListener { e ->
                // failed to get metadata
                Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
            }

        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            // sử dụng mục category từ db
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // lấy giá trị tên danh mục category
                    val category = "${snapshot.child("category").value}"

                    // đặt tên danh mục vào textView
                    categoryTv.text = category
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {
            // context : ngu canh - hien thi hop thoai thong bao
            // bookId : id cua sach can xoa
            // bookUrl : url cua sach can xoa
            // bookTitle : ten cua sach can xoa

            val TAG = "DELETE_BOOK_TAG"
            Log.d(TAG, "deleteBook: deleting...")

            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
            Log.d(TAG, "deleteBook: Deleting from storage...")

            // xoa pdf tu db
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl) // lay url cua pdf can xoa
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleted from storage")
                    Log.d(TAG, "deleteBook: Deleting from db now")

                    val ref = FirebaseDatabase.getInstance().getReference("Books") // tham chieu den Book
                    ref.child(bookId) // lay id Book can xoa
                        .removeValue() // xoa
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context, "Successfully deleted...", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: deleted from db too...")
                        }
                        .addOnFailureListener {e ->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Failed to delete from db due to ${e.message}")
                            Toast.makeText(context, "deleteBook: Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                }
                .addOnFailureListener {e ->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context, "deleteBook: Failed to delete from storage due to ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }


        fun incrementBookViewCount(bookId: String) {
            //1) hien thi so luot doc cua cuon sach do
            val ref = FirebaseDatabase.getInstance().getReference("Books") // vao thuoc tinh Books
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // tham chieu den nut viewCount
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == "" || viewsCount == "null") {
                            viewsCount = "0"
                        }

                        // 2. Tang so luot doc
                        val newViewsCount = viewsCount.toLong() + 1

                        // thiet lap du lieu trong co so du lieu
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        // set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap) // cap nhat them vao db
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }


        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView? // hien thi so trang
        ) {
            val TAG = "PDF_THUMBNAIL_TAG"

            // su dung url va da tu db
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes ->

                Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                // thiet lap du lieu PDF vao pdfView
                pdfView.fromBytes(bytes)
                    .pages(0) // hien thi trang dau tien
                    .spacing(0)
                    .swipeHorizontal(false) // tat tinh nang vuot ngang
                    .enableSwipe(false)
                    .onError { t ->
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "loadPdfFromSinglePage: ${t.message}")
                    }.onPageError { page, t ->
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "loadPdfFromSinglePage: ${t.message}")
                    }
                    .onLoad { nbPages ->
                        Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")
                        // pdf loaded, we can set page count, pdf thumbnail
                        progressBar.visibility = View.INVISIBLE

                        // hien thi so trang
                        if (pagesTv != null) {
                            pagesTv.text = "$nbPages"
                        }
                    }
                    .load()
            }.addOnFailureListener { e ->
                Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
            }
        }
        private fun saveDownloadedBook(context: Context, progressDialog: ProgressDialog, bytes: ByteArray, nameWithExtension: String, bookId: String) {
            Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saving downloaded book")
            try {
                val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) // lay thu muc tai ve tren thiet bi
                downloadsFolder.mkdirs() // tao folder neu chua ton tai

                val filePath = "${downloadsFolder.path}/$nameWithExtension" // duong dan

                val out = FileOutputStream(filePath) // tao 1 luong ghi du lieu ra file
                out.write(bytes) // ghi du lieu vao file
                out.close()

                Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show() // luu thanh cong
                Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Saved to Download Folder")
                progressDialog.dismiss()

                incrementBookDownloadCount(bookId) // tang so luong tai ve
            } catch (e: Exception) {
                Log.d(TAG_DOWNLOAD, "saveDownloadedBook: Failed saving to Download Folder due to ${e.message}")
                Toast.makeText(context, "Failed saving to Download Folder due to ${e.message}", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }

        private fun incrementBookDownloadCount(bookId: String) {
            Log.d(TAG_DOWNLOAD, "incrementBookDownloadCount: Incrementing Book Download count")

            // Bước 1: Lấy số lần tải trước đó
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var downloadsCount = snapshot.child("downloadsCount").value.toString()
                        Log.d(TAG_DOWNLOAD, "onDataChange: Downloads Count: $downloadsCount")

                        if (downloadsCount == "" || downloadsCount == "null") {
                            downloadsCount = "0"
                        }


                        val newDownloadsCount = downloadsCount.toLong() + 1
                        Log.d(TAG_DOWNLOAD, "onDataChange: New Download Count: $newDownloadsCount")

                        // Thiết lập dữ liệu để cập nhật
                        val hashMap = HashMap<String, Any>()
                        hashMap["downloadsCount"] = newDownloadsCount

                        // Bước 2: Cập nhật số lượt tải mới tăng lên vào cơ sở dữ liệu
                        val reference = FirebaseDatabase.getInstance().getReference("Books")
                        reference.child(bookId).updateChildren(hashMap)
                            .addOnSuccessListener {
                                Log.d(TAG_DOWNLOAD, "onSuccess: Downloads Count updated")
                            }
                            .addOnFailureListener { e ->
                                Log.d(TAG_DOWNLOAD, "onFailure: Failed to update Downloads Count due to ${e.message}")
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        fun addToFavorite(context: Context, bookId: String) {
            // Chúng ta chỉ có thể thêm vào nếu người dùng đã đăng nhập
            // 1) Kiểm tra xem người dùng có đăng nhập không
            val firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser == null) {
                // Không đăng nhập, không thể thêm vào mục yêu thích
                Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show()
            } else {
                val timestamp = System.currentTimeMillis()

                // Thiết lập dữ liệu để thêm vào cơ sở dữ liệu firebase của người dùng hiện tại cho sách yêu thích
                val hashMap = hashMapOf(
                    "bookId" to bookId,
                    "timestamp" to timestamp.toString()
                )

                // Lưu vào cơ sở dữ liệu
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Added to your favorites list", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to add to favorite due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        fun removeFromFavorite(context: Context, bookId: String) {
            // Chúng ta chỉ có thể xóa nếu người dùng đã đăng nhập
            // 1) Kiểm tra xem người dùng có đăng nhập không
            val firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth.currentUser == null) {
                // Không đăng nhập, không thể xóa khỏi mục yêu thích
                Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show()
            } else {
                // Xóa khỏi cơ sở dữ liệu
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Removed from your favorites list", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to remove from favorite due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }


    }



}