package org.codeberlin.projectdistributor.score

import mu.KLogging
import org.codeberlin.projectdistributor.data.Role
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.codeberlin.projectdistributor.model.Student
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator

class AssignmentScoreCalculator : IncrementalScoreCalculator<ProjectAssignment> {
    var workingSolution: ProjectAssignment? = null

    override fun resetWorkingSolution(workingSolution: ProjectAssignment?) {
        this.workingSolution = workingSolution?.apply {
            projects.forEach { it.attendance.fill(0) }
            students.forEach(::insert)
        }
    }

    override fun beforeEntityAdded(entity: Any?) {}
    override fun afterEntityAdded(entity: Any?) = insert(entity)

    override fun beforeEntityRemoved(entity: Any?) = retract(entity)
    override fun afterEntityRemoved(entity: Any?) {}

    override fun beforeVariableChanged(entity: Any?, variableName: String?) = retract(entity)
    override fun afterVariableChanged(entity: Any?, variableName: String?) = insert(entity)


    private fun insert(entity: Any?) = add(entity as Student, 1)
    private fun retract(entity: Any?) = add(entity as Student, -1)

    private fun add(student: Student, value: Int) {
        student.chosenApplication?.apply {
            project.attendance[role.ordinal] += value
        }
    }

    override fun calculateScore(): HardMediumSoftScore {
        val solution = workingSolution ?: return HardMediumSoftScore.valueOf(0, 0,0)

        // softScore is the sum of student priorities
        val softScore = solution.students.sumBy { it.chosenApplication?.priority ?: 0 }

        // mediumScore is the number of students without a project (negative)
        val mediumScore = -solution.students.count { it.chosenApplication == null }

        var hardScore = 0
        for (project in solution.projects) {
            var tooFew = false
            Role.values().forEachIndexed { i, role ->
                val attendance = project.attendance[i]
                val max = project.roles.getMax(role)
                if (attendance > max) {
                    // hard constraint: too many students
                    hardScore -= attendance - max
                } else if (!tooFew && attendance < project.roles.getMin(role)) {
                    tooFew = true
                }
            }

            // hard constraint: too few students
            if (tooFew) {
                hardScore -= project.attendance.sum()
            }
        }

        return HardMediumSoftScore.valueOf(hardScore, mediumScore, softScore)
    }

    companion object : KLogging()

}
