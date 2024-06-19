package com.example.bookappyt

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.NonNull
import com.example.bookappyt.databinding.RowPdfFavoriteBinding
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite(
    private val context: Context,
    private val pdfArrayList: ArrayList<ModelPdf>
) : RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite>() {

    // View binding for row_pdf_favorite.xml
    private lateinit var binding: RowPdfFavoriteBinding

    companion object {
        private const val TAG = "FAV_BOOK_TAG"
    }

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        // khai bao cac thanh phan giao dien trong row_pdf_favorite.xml
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfFavorite(binding.root)
    }

    override fun onBindViewHolder(@NonNull holder: HolderPdfFavorite, position: Int) {
        val model = pdfArrayList[position]

        loadBookDetails(model, holder)

        // Handle click, open PDF details page
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }

        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
        }
    }

    private fun loadBookDetails(model: ModelPdf, holder: HolderPdfFavorite) {
        val bookId = model.id
        Log.d(TAG, "loadBookDetails: Book Details of Book ID $bookId")

        val ref = FirebaseDatabase.getInstance().getReference("Books") // tham chieu den Books
        ref.child(bookId) // tham chieu nut con bookId
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Lay du lieu
                    val bookTitle = snapshot.child("title").getValue(String::class.java) ?: ""
                    val description = snapshot.child("description").getValue(String::class.java) ?: ""
                    val categoryId = snapshot.child("categoryId").getValue(String::class.java) ?: ""
                    val bookUrl = snapshot.child("url").getValue(String::class.java) ?: ""
                    val timestamp = snapshot.child("timestamp").getValue(String::class.java) ?: ""
                    val uid = snapshot.child("uid").getValue(String::class.java) ?: ""
                    val viewsCount = snapshot.child("viewsCount").getValue(String::class.java) ?: ""
                    val downloadsCount = snapshot.child("downloadsCount").getValue(String::class.java) ?: ""

                    model.favorite = true
                    model.title = bookTitle
                    model.description = description
                    model.timestamp = timestamp.toLongOrNull() ?: 0L
                    model.categoryId = categoryId
                    model.uid = uid
                    model.url = bookUrl

                    val date = MyApplication.formatTimeStamp(timestamp.toLongOrNull() ?: 0L)

                    MyApplication.loadCategory(categoryId, holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage(bookUrl, bookTitle, holder.pdfView, holder.progressBar, null)
                    MyApplication.loadPdfSize(bookUrl, bookTitle, holder.sizeTv)


                    holder.titleTv.text = bookTitle
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }


    override fun getItemCount(): Int {
        return pdfArrayList.size // tra ve so luong phan tu trong pdfArrayList
    }

    // ViewHolder class
    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Khoi tao UI views cua row_pdf_favorite
        var pdfView: PDFView
        var progressBar: ProgressBar
        var titleTv: TextView
        var descriptionTv: TextView
        var categoryTv: TextView
        var sizeTv: TextView
        var dateTv: TextView
        var removeFavBtn: ImageButton

        init {
            pdfView = binding.pdfView
            progressBar = binding.progressBar
            titleTv = binding.titleTv
            removeFavBtn = binding.removeFavBtn
            descriptionTv = binding.descriptionTv
            categoryTv = binding.categoryTv
            sizeTv = binding.sizeTv
            dateTv = binding.dateTv
        }
    }


}
