package com.example.firebasemessenger.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User(
    val uid: String,
    val uname: String,
    val profileImage: String,
    val type: Int
    ): Parcelable {
    constructor() : this("", "", "", -1)
}