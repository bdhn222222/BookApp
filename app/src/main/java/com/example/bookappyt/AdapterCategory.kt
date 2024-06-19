package com.example.bookappyt

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.bookappyt.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context: Context // hoạt động liên quan đến người dùng
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory> //bản sao của categoryList -> sử dụng cho mục đích lọc
    private var filter: FilterCategory? = null //-> triển khai bộ lọc một cách tùy chỉnh
    private lateinit var binding: RowCategoryBinding // liên kết với các phẩn tử giao diện

    // hàm khởi tạo
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }


    //tạo view holder cho RecycleView (hiển thị các mục có thể tái sử dụng các view được tạo ra trước đó -> tối ưu)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        // tạo ra 1 đối tượng RCB từ tệp layout row_category
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)

    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        //hàm này được gọi khi Recycle View cần thiết lập dữ liệu và xử lý sự kiện cho 1 ViewHolder

        // lấy dữ liệu
        var model = categoryArrayList[position]
        var id = model.id
        var category = model.category
        val timestamp = model.timestamp
        val uid = model.uid

        // thiết lập dữ liệu cho các View Component
        holder.categoryTv.text = category

        // nút xóa
        holder.deleteBtn.setOnClickListener {
            // confirm trước khi xóa
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this category")
                .setPositiveButton("Confirm") { a, d ->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }.setNegativeButton("Cancel") { a, d ->
                    a.dismiss()
                }
                .show()
        }

        // khi người dùng nhấn vào 1 mục trong các danh mục đó -> show PDF
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfListAdminActivity::class.java)
            //2 thông tin được truyền vào là ID và tên của thể loại
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent) // khởi chạy PdfListAdminActivity và chuyển các thông tin đã được truyền vào Intent.
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        // lấy id của thể loại cần xóa
        val id = model.id

        // tham chiếu đến nút "Categories' trong FRD
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        //dùng hàm RemoveValue để xóa dữ liệu
        ref.child(id).removeValue().addOnSuccessListener {
            Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Unable to delete due to ${e.message}...", Toast.LENGTH_SHORT)
                .show()

        }

    }


    override fun getItemCount(): Int {
        return categoryArrayList.size // số lượng thể loại trong danh sách
    }


    // quản lý các view cho mỗi mục item trong recycleView -  layout row_category.xml
    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // khởi tạo các view
        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn

    }

    //lọc dữ liệu
    override fun getFilter(): Filter {
        if (filter == null) { // kiểm tra xem có null hay không
            //khởi tạo một đối tượng FilterCategory mới, truyền vào đó danh sách filterList và chính adapter này
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }


}