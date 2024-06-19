package com.example.bookappyt

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.Display.Mode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookappyt.databinding.RowPdfAdminBinding


class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    // show PdfAdminActivity
    private lateinit var binding: RowPdfAdminBinding

    private var context: Context //cung cấp quyền truy cập vào tài nguyên nào đó

    //Mảng Pdf để lưu trữ dữ liệu
    public var pdfArrayList: ArrayList<ModelPdf>

    private val filterList: ArrayList<ModelPdf>

    var filter: FilterPdfAdmin? = null

    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        // tao 1 ViewHolder cua HolderPdfAdmin, = cach nap layout row_pda_admin.xml vao ViewHolder
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        /*----Lay data, click, cai dat data,...----*/

        // lay data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        // doi gio sang format dd/MM/yy
        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        // set up data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        // du lieu cua category

        //load theo id cua category
        MyApplication.loadCategory(categoryId, holder.categoryTv)

        // load pdf tu url va dung 1 view de hien thi PDF
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar, //hien thi trang thai add pdf
            null
        )

        // load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        // co 2 option de chon la Edit va Delete
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        // neu ma cick vao 1 item trong list thi se hien thi chi tiet cua pdf
        holder.itemView.setOnClickListener{ // khi nguoi dung click vao phan nao cua Holder
            // tao intent voi book Id, khoi tai PdfDetailActivity
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent) // chuyen sang PdfDetailActivity
        }


    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin) {
        // glay 3 thong tin cua pdf
        val bookId= model.id
        val bookUrl= model.url
        val bookTitle= model.title

        // option de chon trong dialog
        val options = arrayOf("Edit", "Delete")

        // alert Dialog : thong bao cho nguoi dung 1 hanh dong sap xay ra
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){dialog, position ->
                if (position == 0) {
                    // 0 -> button Edit
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                } else if (position == 1) {
                    // 1 -> button Delete
                    // Hien thi confirm...
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }


    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) { // xem bien khoi tao hay chua
            filter = FilterPdfAdmin(filterList, this)
        }
        return filter as FilterPdfAdmin
    }


    /* tao instance cua HolderPdfAdmin va tra ve noi, hien thi UI row_pdf_admin.xml*/
    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // UI hien thi cua row_pdf_admin.xml
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }

}