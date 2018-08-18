package org.codeberlin.projectdistributor

import org.codeberlin.projectdistributor.model.Application
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO
import java.io.File

class AssignmentPersistence : SolutionFileIO<ProjectAssignment> {
    override fun write(solution: ProjectAssignment?, outputSolutionFile: File?) {
        if (solution == null || outputSolutionFile == null) return

        // copy the data structure, but replace projects with just references
        val copy = solution.copy(students = solution.students.map { student ->
            student.copy(applications = student.applications.map(Application::toReference))
                    .apply { chosenApplication = student.chosenApplication?.toReference() }
        })
        outputSolutionFile.bufferedWriter().use { DataUtil.gson.toJson(copy, it) }
    }

    override fun read(inputSolutionFile: File?): ProjectAssignment {
        // when reloading, transform the project ids back to a reference to the project
        val solution = inputSolutionFile?.bufferedReader().use { reader ->
            DataUtil.gson.fromJson(reader, ProjectAssignment::class.java)
        }

        // since this field is transient we have to manually initialise the array
        solution.projects.forEach { it.attendance = intArrayOf(0, 0, 0, 0) }

        val projectById = solution.projects.map { it.id to it }.toMap()
        fun Application.toFull() = copy(project = projectById[project.id]!!)

        return solution.copy(students = solution.students.map { student ->
            student.copy(applications = student.applications.map(Application::toFull))
                    .apply { chosenApplication = student.chosenApplication?.toFull() }
        })
    }

    override fun getInputFileExtension() = "json"
}
