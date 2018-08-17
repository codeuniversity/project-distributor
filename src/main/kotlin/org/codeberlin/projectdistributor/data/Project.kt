package org.codeberlin.projectdistributor.data

data class Project(
        val duration: String,
        val name: String,
        val stakeholder: Stakeholder,
        val id: String,
        val roles: Roles
)
