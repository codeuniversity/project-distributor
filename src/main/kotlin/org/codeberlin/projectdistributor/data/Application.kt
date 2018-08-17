package org.codeberlin.projectdistributor.data

data class Application(
        val role: Role,
        val isMastermind: Boolean,
        val priority: Int,
        val project: ProjectReference
)
