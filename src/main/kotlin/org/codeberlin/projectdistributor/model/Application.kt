package org.codeberlin.projectdistributor.model

import org.codeberlin.projectdistributor.data.Role

data class Application(
        val role: Role,
        val priority: Int,
        val project: Project
) {
    override fun toString(): String {
        return "[%3d] %s @ %s".format(priority, role, project)
    }
}
