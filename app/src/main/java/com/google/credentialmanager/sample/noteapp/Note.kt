package com.google.credentialmanager.sample.noteapp

class Note {
    @JvmField
    var title: String? = null
    var content: String? = null
    var imageUri: String? = null
    var timeCreated: Long = 0
    var userID: String? = null

    constructor()
    constructor(title: String?, content: String?, timeCreated: Long, imageUri: String?, userID: String?) {
        this.title = title
        this.content = content
        this.timeCreated = timeCreated
        this.imageUri = imageUri
        this.userID = userID
    }
}
