package org.codeberlin.projectdistributor.data

data class Project(
        val duration: Duration,
        val name: String,
        // don't care about the stakeholder for now:
        // val stakeholder: Stakeholder,
        val id: String,
        val roles: Roles
)
