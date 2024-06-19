package com.example.bookappyt

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookappyt.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    private companion object{
        private const val TAG = "PDf_EDIT_TAG"
    }


    // view binding
    private lateinit var binding: ActivityPdfEditBinding
    private var bookId = "" // lay tu intent AdapterPdfAdmin
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryTitleArrayList:ArrayList<String>
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_edit)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // lay idBook
        bookId = intent.getStringExtra("bookId")!!


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories() // hien thi list Category
        loadBookInfo()


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        binding.categoryTv.setOnClickListener {
            categoryDialog() // hien thi ra hop thoai category de chon category cua book do
        }

        // click submit
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG, "lookBookInfo: Loading book info")
        val ref = FirebaseDatabase.getInstance().getReference("Books") // tham chieu den books
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString() // khi duoc click thi tham chieu den id cua category duoc chon
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    // lay ten the loai cua sach do tu id category
                    //de hien thi ten category thong qua id category
                    Log.d(TAG, "onDataChange: Loading book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories") // tham chieu den categories
                    refBookCategory.child(selectedCategoryId) // tham chieu toi category id
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // lay gia tri category
                                val category = snapshot.child("category").value
                                // hien thi category
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    private var title = ""
    private var description = ""
    private fun validateData() { // xac thuc du lieu
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf, Starting updating pdf info...")
        // show progress
        progressDialog.setMessage("Updating book info")
        progressDialog.show()

        // thiet lap data trong db
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        // update
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap) // update lai hashMap
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Updated successfully...")
                Toast.makeText(this, "Updated successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "updatePdf: Failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {

        // Khoi tao List category
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) { // duyet qua tung phan tu trong mang
            categoriesArray[i] = categoryTitleArrayList[i] // gan gia tri
        }

        // alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray) {dialog, position ->
                // click vao category -> save lai ten va id cua category do
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                // thiet lap textView
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()

    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories...")
        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list truoc khi add
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id = "" + ds.child("id").value
                    val category = "" + ds.child("category").value

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category title $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}