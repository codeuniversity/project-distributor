package org.codeberlin.projectdistributor.model

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
    var score: HardSoftScore? = null


    companion object {
        // create a model from parsed json data
        fun convert(data: Data): ProjectAssignment {
            // convert to model class
            val projects = data.projects.map { Project(it.duration, it.name, it.id, it.roles) }

            // hashmap of projects by id
            val projectById = projects.asSequence().map {
                it.id to it
            }.toMap()

            val fallBackApplications = Role.values().map { role ->
                role to projects.filter { it.duration == Duration.FULL && it.roles.getMin(role) > 0 }.map { Application(role, -20, it) }
            }.toMap()

            val filteredUsers = data.users.filter { it.applications.find { it.isMastermind } == null }
            val students = filteredUsers.map {
                val allApps = it.applications.map {
                    Application(it.role, it.priority, projectById[it.project.id]!!)
                }
                val applications = allApps.filter { it.project.duration == Duration.FULL }
                Student(it.id, it.name, applications.plus(fallBackApplications[it.applications[0].role]!!))
            }
            return ProjectAssignment(projects, students)
        }
    }
}
