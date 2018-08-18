package org.codeberlin.projectdistributor

import mu.KotlinLogging
import org.codeberlin.projectdistributor.data.Data

object Exporter {
    private val logger = KotlinLogging.logger {}

    fun Data.exportApplications() {
        // hashmap of projects by id
        val projects = projects.asSequence().map {
            it.id to it
        }.toMap()

        logger.info {
            users.flatMap { user ->
                user.applications.map {
                    val project = projects[it.project.id]!!
                    listOf(user.name, it.role, it.isMastermind, it.priority, project.name, project.duration).joinToString("\t")
                }
            }.joinToString("\n", "\n")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Optimizer.loadMainData().exportApplications()
    }
}
