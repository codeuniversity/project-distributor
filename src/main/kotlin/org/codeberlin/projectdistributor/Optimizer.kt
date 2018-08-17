package org.codeberlin.projectdistributor

import mu.KotlinLogging
import org.codeberlin.projectdistributor.data.Data
import org.codeberlin.projectdistributor.data.Project
import org.codeberlin.projectdistributor.data.Role


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
                    val project = projects[ref.id]
                    if (project == null) {
                        logger.warn {
                            "student ${user.name} is applying for a project with id \"${ref.id}\" " +
                                    "which was not found in the projects list"
                        }
                    } else {
                        if (project.roles.getMax(application.role) == 0) {
                            logger.warn {
                                "student ${user.name}, role ${application.role}, ${if (application.isMastermind) "ProjectOwner" else "Priorität ${application.priority}"}, " +
                                        "project \"${project.name}\": ${project.roles}"
                            }
                        }
                    }
                }
            }

            val masterMindCount = user.applications.count { it.isMastermind }
            if (masterMindCount > 1) {
                logger.warn { "user has is a multi masterMind: ${user.name} => ${user.applications}" }
            }
            if (masterMindCount != 1 && user.applications.size != 3) {
                logger.warn { "user does not have exactly 3 applications: ${user.name} => ${user.applications}" }
            }

            val prioSum = user.applications.asSequence().filterNot { it.isMastermind }.sumBy { it.priority }
            if (prioSum > 10) {
                logger.warn { "user has given out $prioSum priorities which is > 10: ${user.applications}" }
            }

            val tooLow = user.applications.filter { it.priority < 1 }
            if (tooLow.isNotEmpty()) {
                logger.warn { "user has has prio < 1: ${user.name} => ${user.applications}" }
            }
        }

        logger.info { "checked ${data.users.size} users for invalid applications" }

        projectStats(data, projects)
    }

    private fun projectStats(data: Data, projects: Map<String, Project>) {
        Role.values().forEach { role ->
            logger.info {
                val subset = projects.values.filter { it.roles.getMin(role) != it.roles.getMax(role) }
                "applications where min$role != max$role: ${subset.size}\n${subset.joinToString("\n\t", "\t")}"
            }
        }

        logger.debug {
            "applications by project:\n${
            data.users.asSequence()
                    .flatMap { it.applications.asSequence() }
                    .groupBy { it.project.id }.asSequence()
                    .joinToString("\n") { (id, apps) ->
                        val partition = apps.partition { it.isMastermind }
                        val project = projects[id]!!
                        listOf(
                                project.name,
                                project.roles.minID,
                                project.roles.maxID,
                                project.roles.minPM,
                                project.roles.maxPM,
                                project.roles.minSE,
                                project.roles.maxSE,
                                partition.toList().joinToString("\t") { a -> a.groupBy { it.role }.map { "${it.key}=${it.value.size}" }.sorted().joinToString() },
                                if (project.roles.enoughApplications(partition.second)) "" else "not enough applications for ${project.roles.missingApplications(partition.second)}"
                        ).joinToString("\t")
                    }
            }"
        }

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
