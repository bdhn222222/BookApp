package com.example.bookappyt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.bookappyt.databinding.ActivityPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    // category id, title
    private var categoryId = ""
    private var category = ""

    //mang cua pdf
    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // tao intent va lay du lieu tu category
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        binding.subtitleTv.text = category

        // tai list cua pdf
        loadPdfList()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged( // truoc khi search
                s: CharSequence?, //ten pdf can search
                start: Int,
                count: Int,
                after: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // filter data
                try {
                    adapterPdfAdmin.filter!!.filter(s) // su dung filter de loc theo tu khoa search
                }
                catch (e:Exception) {
                    Log.d(TAG, "onTextChanged: ${e.message}")

                }
            }

                override fun afterTextChanged(s: Editable?) {
                    TODO("Not yet implemented")
                }
            })
        }

                private fun loadPdfList() {
            // khoi tao arraylist
            pdfArrayList = ArrayList()

            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // clear mang truoc khi add vao
                        pdfArrayList.clear()
                        for (ds in snapshot.children) {
                            // lay data
                            val model = ds.getValue(ModelPdf::class.java)

                            // them
                            if (model != null) {
                                pdfArrayList.add(model)
                                Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")

                            }
                        }

                        //setup adapter
                        adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                        binding.booksRv.adapter = adapterPdfAdmin
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

        }
    }