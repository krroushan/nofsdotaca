package com.serqfix.partner.data.api

data class MetaData(
    val currentPage: Int? = null,
    val totalPages: Int? = null,
    val totalItems: Int? = null,
    val hasNextPage: Boolean? = null,
    val hasPreviousPage: Boolean? = null
)
