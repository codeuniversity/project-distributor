package org.codeberlin.projectdistributor.model

import mu.KLogging
import org.codeberlin.projectdistributor.data.Data
import org.codeberlin.projectdistributor.data.Duration
import org.codeberlin.projectdistributor.data.Role
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty
import org.optaplanner.core.api.score.buildin.bendable.BendableScore

@PlanningSolution
data class ProjectAssignment(
        @ProblemFactCollectionProperty
        val projects: List<Project>,
        @PlanningEntityCollectionProperty
        val students: List<Student>
) {
    constructor() : this(emptyList(), emptyList())

    @PlanningScore(bendableHardLevelsSize = 3, bendableSoftLevelsSize = 1)
    @Transient
    var score: BendableScore? = null

    @ProblemFactCollectionProperty
    @Transient
    val roles = Role.values().toList()

    @Transient
    val scoreFilename = score?.toString()?.replace("[\\[\\]]".toRegex(), "")?.replace('/', '-')

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
                role to projects.filter { it.roles?.getMin(role) ?: 0 > 0 }.map { Application(role, -20, it) }
            }.toMap()

            val students = data.users.map { user ->
                val masterApp = user.applications.find { app ->
                    app.isMastermind && projectById.containsKey(app.project.id)
                }

                // normal users get a list of their applications plus a list of fallbacks
                // owners get a single choice: the project they signed up for
                val applications = if (masterApp == null) {
                    val all = user.applications.mapNotNull { it.convert(projectById) } + fallBackApplications[user.applications[0].role]!!
                    if (user.excluded != null && user.excluded.isNotEmpty()) all.filterNot { user.excluded.contains(it.project.id) }
                    else all
                } else listOf(masterApp.convert(projectById)!!)

                Student(user.id, user.name, applications)
            }

            return ProjectAssignment(projects, students)
        }


        // create a model from parsed json data
        fun convertHalf(data: Data): ProjectAssignment {
            // convert to model class
            val projects = data.projects.filter { project ->
                project.duration == Duration.HALF
            }.map { Project(it.duration, it.name, it.id, it.roles) }

            // hashmap of projects by id
            val projectById = projects.asSequence().map {
                it.id to it
            }.toMap()

            val students = data.users.mapNotNull { user ->
                val masterApp = user.applications.find { app -> app.isMastermind }

                val applications = if (masterApp == null) {
                    user.applications.mapNotNull { it.convert(projectById) }
                } else {
                    masterApp.convert(projectById)?.let { listOf(it) } ?: emptyList()
                }

                if (applications.isEmpty()) null
                else Student(user.id, user.name, applications)
            }

            return ProjectAssignment(projects, students)
        }
    }
}
