package com.example.bookapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.submitBtn.setOnClickListener { validateAndAddCategory() }
    }

    private fun validateAndAddCategory() {
        val category = binding.categoryEt.text.toString().trim()

        if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else {
            addCategoryToFirebase(category)
        }
    }

    private fun addCategoryToFirebase(category: String) {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val categoryId = "$timestamp"

        val data = hashMapOf(
            "id" to categoryId,
            "category" to category,
            "timestamp" to timestamp,
            "uid" to firebaseAuth.uid
        )

        FirebaseDatabase.getInstance().getReference("Categories")
            .child(categoryId)
            .setValue(data)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}