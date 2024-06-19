package com.example.bookappyt

class ModelCategory {
    // variables, must mach as in firebase
    var id: String = ""
    var category: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    constructor()
    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }

}