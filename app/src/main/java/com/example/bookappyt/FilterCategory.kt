package com.example.bookappyt

import android.widget.Filter

class FilterCategory : Filter {
    // mảng mà chúng ta muốn tìm kiếm và lọc ra - dữ liệu của Category
    private var filterList: ArrayList<ModelCategory>

    // hiển thị dữ liệu
    private var adapterCategory: AdapterCategory

    //constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    //lọc dữ liệu trên 1 từ khóa để tìm kiếm
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint // từ khóa
        val results = FilterResults()

        // kiểm tra từ khóa không null và không rỗng
        if (constraint != null && constraint.isNotEmpty()) {

            // chuyển đổi sang chữ hoa hết để không bị nhập nhằng giữa chữ hoa và chữ thường
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<ModelCategory> = ArrayList()
            // mảng này để lưu trữ các mục phù hợp với từ khóa tìm kiếm đó
            for (i in 0 until filterList.size) {
                // nó lặp qua từng filterList - danh sách dữ liệu gốc
                if (filterList[i].category.uppercase().contains(constraint)) {
                    // kiểm tra xem có chứa từ khoá tìm kiếm hay không
                    //nếu có sẽ add vào mảng phù hợp với từ khóa đó
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size // đếm lại số mục phù hợp
            results.values = filteredModels // giá trị phù hợp
        }
        else{
            // nếu rỗng hoặc trống
            results.count = filterList.size
            results.values = filterList
        }
        return results // trả về kết quả
    }

    //giao diện sau khi lọc dữ liệu - tìm kiếm
    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        // cập nhật bằng cách gán giá trị results vào mảnh
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        // thông báo cho adapter đã cập nhật dữ liệu và giao diện sẽ dược cập nhật
        adapterCategory.notifyDataSetChanged()

    }


}