package com.example.bookappyt


class ModelComment {
    // Variables
    var id: String = ""
    var bookId: String = ""
    var timestamp: String = ""
    var comment: String = ""
    var uid: String = ""

    // Empty constructor required by Firebase
    constructor()

    // Constructor with all params
    constructor(id: String, bookId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }

    /*-- Getter Setters --*/
}
