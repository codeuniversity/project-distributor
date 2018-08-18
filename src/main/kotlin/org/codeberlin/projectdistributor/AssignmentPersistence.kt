package org.codeberlin.projectdistributor

import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO
import java.io.File

class AssignmentPersistence : SolutionFileIO<ProjectAssignment> {
    override fun write(solution: ProjectAssignment?, outputSolutionFile: File?) {
        outputSolutionFile?.bufferedWriter().use { DataUtil.gson.toJson(it) }
    }

    override fun read(inputSolutionFile: File?): ProjectAssignment =
            inputSolutionFile?.bufferedReader().use { DataUtil.gson.fromJson(it, ProjectAssignment::class.java) }

    override fun getInputFileExtension() = "json"
}
