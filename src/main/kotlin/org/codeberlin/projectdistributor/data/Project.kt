package org.codeberlin.projectdistributor.data

data class Project(
        val duration: Duration,
        val name: String,
        val stakeholder: Stakeholder?,
        val id: String,
        val roles: Roles
)
