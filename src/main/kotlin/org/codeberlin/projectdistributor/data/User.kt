package org.codeberlin.projectdistributor.data

data class User(
        val id: String,
        val name: String,
        val applications: List<Application>
)
