package com.example.bookappyt

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookappyt.databinding.RowCommentBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment(private val context: Context, private val commentArrayList: ArrayList<ModelComment>) :
    RecyclerView.Adapter<AdapterComment.HolderComment>() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        val binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding)
    }


    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val modelComment = commentArrayList[position]
        val id = modelComment.id
        val bookId = modelComment.bookId
        val comment = modelComment.comment
        val uid = modelComment.uid
        val timestamp = modelComment.timestamp

        val date = MyApplication.formatTimeStamp(timestamp.toLong())

        // Set data
        holder.dateTv.text = date
        holder.commentTv.text = comment

        // We don't have user's name, profile picture, so we will load it using uid we stored in each comment
        loadUserDetails(modelComment, holder)

        // Handle click, show option to delete comment
        holder.itemView.setOnClickListener {
            /*--Requirements to delete a comment
                * 1) user must be logged in
                * 2) uid in comment (to be deleted) must be same as uid of logged in user
                --*/
            if (firebaseAuth.currentUser != null && uid == firebaseAuth.uid) {
                deleteComment(modelComment, holder)
            }
        }
    }

    private fun loadUserDetails(modelComment: ModelComment, holder: HolderComment) {
        val uid = modelComment.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get data
                val name = snapshot.child("name").value.toString()
                val profileImage = snapshot.child("profileImage").value.toString()

                // Set data
                holder.nameTv.text = name
                try {
                    Glide.with(context)
                        .load(profileImage)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(holder.profileTv)
                } catch (e: Exception) {
                    holder.profileTv.setImageResource(R.drawable.ic_person_gray)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun deleteComment(modelComment: ModelComment, holder: HolderComment) {
        AlertDialog.Builder(context)
            .setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("DELETE") { dialogInterface, _ ->
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(modelComment.bookId)
                    .child("Comments")
                    .child(modelComment.id)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("CANCEL") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }
    override fun getItemCount(): Int {
        return commentArrayList.size // trả về kích thước của danh sách, số lượng bình luận
    }



    inner class HolderComment(private val binding: RowCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // UI views of row_comment.xml
        val profileTv: ShapeableImageView = binding.profileTv
        val nameTv: TextView = binding.nameTv
        val dateTv: TextView = binding.dateTv
        val commentTv: TextView = binding.commentTv
    }
}
