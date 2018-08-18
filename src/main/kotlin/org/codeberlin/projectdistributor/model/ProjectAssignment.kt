package org.codeberlin.projectdistributor.model

import mu.KLogging
import org.codeberlin.projectdistributor.data.Data
import org.codeberlin.projectdistributor.data.Duration
import org.codeberlin.projectdistributor.data.Role
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore

@PlanningSolution
data class ProjectAssignment(
        val projects: List<Project>,
        @PlanningEntityCollectionProperty
        val students: List<Student>
) {
    constructor() : this(emptyList(), emptyList())

    @PlanningScore
    @Transient var score: HardSoftScore? = null

    fun debugContent() {
        logger.debug {
            "ProjectAssignment $score:" +
                    "\nProjects:\n\t" +
                    projects.joinToString("\n\t") +
                    "\nApplications:\n\t" +
                    students.flatMap { student ->
                        val (real, fallback) = student.applications.partition { it.priority > 0 }
                        real.map { "$student $it" }.plusElement(
                                "$student ${fallback.size} fallbacks with role ${fallback[0].role}"
                        )
                    }.joinToString("\n\t")
        }
    }

    companion object : KLogging() {
        // create a model from parsed json data
        fun convert(data: Data): ProjectAssignment {
            // convert to model class
            val projects = data.projects.filter { project ->
                project.duration == Duration.FULL
            }.map { Project(it.duration, it.name, it.id, it.roles) }

            // hashmap of projects by id
            val projectById = projects.asSequence().map {
                it.id to it
            }.toMap()

            val fallBackApplications = Role.values().map { role ->
                role to projects.filter { it.roles.getMin(role) > 0 }.map { Application(role, -20, it) }
            }.toMap()

            val students = data.users.map { user ->
                val masterApp = user.applications.find { app ->
                    app.isMastermind && projectById.containsKey(app.project.id)
                }

                val applications = if (masterApp == null) {
                    user.applications.mapNotNull { it.convert(projectById) } + fallBackApplications[user.applications[0].role]!!
                } else listOf(masterApp.convert(projectById)!!)

                Student(user.id, user.name, applications)
            }

            return ProjectAssignment(projects, students)
        }
    }
}
