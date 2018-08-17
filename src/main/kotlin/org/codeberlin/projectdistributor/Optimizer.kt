package org.codeberlin.projectdistributor

import mu.KotlinLogging
import org.codeberlin.projectdistributor.data.Data
import org.codeberlin.projectdistributor.data.Project


object Optimizer {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val jsonStream = javaClass.getResourceAsStream("/project-applications.json")
        if (jsonStream == null) {
            logger.warn { "please put a file called project-applications.json into the src/main/resources folder" }
            return
        }
        val data = DataUtil.fromStream(jsonStream).data
        logger.debug { "data loaded: ${data.projects.size} projects and ${data.users.size} users" }

        checkDataConsistency(data)
    }

    private fun checkDataConsistency(data: Data) {
        // hashmap of projects by id
        val projects = data.projects.asSequence().map {
            it.id to it
        }.toMap()

        if (projects.size != data.projects.size) {
            logger.warn {
                "duplicate id(s) detected: map contains only ${projects.size} instead of ${data.projects.size} projects: " +
                        "${data.projects.filter { it != projects[it.id] }}"
            }
        }

        for (user in data.users) {
            for (application in user.applications) {
                application.project.also { ref ->
                    val projectName = projects[ref.id]?.name
                    if (projectName == null) {
                        logger.warn {
                            "user ${user.name} has an application for ${ref.id} " +
                                    "that is not part of the projects list"
                        }
                    }
                }
            }

            val prioSum = user.applications.asSequence().filterNot { it.isMastermind }.sumBy { it.priority }
            if (prioSum > 10) {
                logger.warn { "user has given out $prioSum priorities which is > 10: ${user.applications}" }
            }
        }

        logger.info { "checked ${data.users.size} users for invalid project references" }

        projectStats(data, projects)
    }

    private fun projectStats(data: Data, projects: Map<String, Project>) {
        logger.debug("applications by project:\n${
        data.users.asSequence()
                .flatMap { it.applications.asSequence() }
                .groupBy { it.project.id }.asSequence()
                .map { project -> projects[project.key] to project.value.groupBy { it.role }.map { it.key to it.value.size } }
                .sortedByDescending { entry -> entry.second.sumBy { it.second } }
                .joinToString("\n") { entry -> "${entry.first} ${entry.second.sortedBy { it.first }}" }
        }")

        logger.debug {
            "stakeholders:\n${
            data.projects.asSequence()
                    .filter { it.stakeholder.organization.isNotEmpty() }
                    .groupBy { it.stakeholder.organization }.asSequence()
                    .sortedByDescending { it.value.size }
                    .joinToString("\n") { entry -> "${entry.key}: ${entry.value.map { it.name }}" }
            }"
        }
    }
}
