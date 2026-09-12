package io.github.lord1egypt.prootx.model.entities

data class Asset(
    val name: String,
    val type: String, // Either "support" or a distribution type
    val pathName: String = "$type/$name"
)