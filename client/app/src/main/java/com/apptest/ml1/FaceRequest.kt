package com.apptest.ml1

data class FaceRequest(
    val user_id: String,
    val embedding : List<Float>,
)
