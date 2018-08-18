package org.codeberlin.projectdistributor.data

import org.codeberlin.projectdistributor.model.Project

data class Application(
        val role: Role,
        val isMastermind: Boolean,
        val priority: Int,
        val project: ProjectReference
) {
    fun convert(projectById: Map<String, Project>) =
            projectById[project.id]?.let { project -> org.codeberlin.projectdistributor.model.Application(role, priority, project) }

}
