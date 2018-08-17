package org.codeberlin.projectdistributor.data

data class Application(
        val isMastermind: Boolean,
        val priority: Int,
        val projectReference: ProjectReference
)
