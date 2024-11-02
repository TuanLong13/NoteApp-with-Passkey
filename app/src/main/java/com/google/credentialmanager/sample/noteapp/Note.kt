package com.google.credentialmanager.sample.noteapp

class Note {
    @JvmField
    var title: String? = null
    var content: String? = null
    var imageUri: String? = null
    var timeCreated: Long = 0

    constructor()
    constructor(title: String?, content: String?, timeCreated: Long, imageUri: String?) {
        this.title = title
        this.content = content
        this.timeCreated = timeCreated
        this.imageUri = imageUri
    }
}
