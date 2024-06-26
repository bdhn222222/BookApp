package com.example.bookappyt

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.bookappyt.databinding.FragmentBooksUserBinding
import java.lang.Exception

class BooksUserFragment: Fragment {
    private lateinit var binding: FragmentBooksUserBinding

    public companion object {
        private const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment {
            val fragment = BooksUserFragment()
            val args = Bundle()
            // lưu các tham số vào đối tượng "Bundle"
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args   // gán đối tượng "Bundle" vào thuộc tính "arguments" của đối tượng "fragment"
            return fragment // trả về đối tượng "fragment"
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        // kiểm tra xem "args" có null hay không
        if (args != null) {
            // gán giá trị của các tham số từ đối tượng "args" vào các biến tương ứng
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        // in ra một thông báo debug với thẻ "BOOKS_USER_TAG" và giá trị của biến "category"
        Log.d(TAG, "onCreateView: Category: $category")

        /// kiểm tra giá trị của biến "category" và gọi các hàm tương ứng để tải dữ liệu sách
        if (category == "All") {
            loadAllBooks()
        }
        else if (category == "Most Viewed") {
            loadMostViewedDownloadedBooks("viewsCount")
        }
        else if (category == "Most Downloaded") {
            loadMostViewedDownloadedBooks("downloadsCount")
        }
        else {
            loadCategorizedBooks()
        }

        // thêm một TextWatcher vào view "searchEt" của "binding" để lọc dữ liệu khi người dùng nhập từ khóa tìm kiếm
        binding.searchEt.addTextChangedListener { object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }
                catch(e: Exception) {
                    Log.d(TAG, "onTextChanged: SEARCH_EXCEPTION: ${e.message}")
                }
            }
            override fun afterTextChanged(p0: Editable?) {

            }
        } }

        return binding.root
    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        // lấy một tham chiếu đến nút "Books" trong cơ sở dữ liệu Firebase
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()    // xóa dữ liệu trong "pdfArrayList"
                for (ds in snapshot.children) {
                    // lặp qua các nút con của "Books" và thêm các đối tượng "ModelPdf" vào "pdfArrayList"
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(model!!)
                }
                // tạo một "AdapterPdfUser" mới và gán nó vào adapter của "booksRv" trong "binding"
                adapterPdfUser=AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser=AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadCategorizedBooks() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelPdf::class.java)
                    pdfArrayList.add(model!!)
                }
                adapterPdfUser=AdapterPdfUser(context!!, pdfArrayList)
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}
