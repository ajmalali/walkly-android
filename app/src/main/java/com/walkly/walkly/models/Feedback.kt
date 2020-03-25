package com.walkly.walkly.models

import com.google.firebase.Timestamp

data class Feedback(
    var userID: String,
    var feedbackContent: String,
    var timestamp: Timestamp,
    var closed: Boolean = false
) {
}