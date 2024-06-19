package com.example.bookappyt

import android.widget.Filter

class FilterPdfUser: Filter {
    var filterList: ArrayList<ModelPdf>
    var adapterPdfUser: AdapterPdfUser

    // gán giá trị của các tham số này vào các biến tương ứng của lớp
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                // nếu tiêu đề của ModelPdf (đã chuyển đổi thành chữ hoa) chứa từ khóa tìm kiếm (cũng đã chuyển đổi thành chữ hoa) -> thêm ModelPdf vào filteredModels
                if (filterList[i].title.uppercase().contains(constraint)) {
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size // gán kích thước của filteredModels vào count trong results
            results.values = filteredModels // gán filteredModels vào values trong results

        } else {
            results.count = filterList.size // gán kích thước của filterList vào count trong results
            results.values = filterList // gán filterList vào values trong results
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        // cập nhật dữ liệu cho adapter sẽ được thực hiện trong adapter
    }
}