package org.codeberlin.projectdistributor.score

import mu.KLogging
import org.codeberlin.projectdistributor.data.Role
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.codeberlin.projectdistributor.model.Student
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
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

    override fun calculateScore(): HardSoftScore {
        val solution = workingSolution ?: return HardSoftScore.valueOf(0, 0)

        // softScore is the sum of student priorities
        val softScore = solution.students.sumBy { it.chosenApplication?.priority ?: 0 }

        var hardScore = 0
        for (project in solution.projects) {
            val totalAttendance = project.attendance.sum()
            Role.values().forEachIndexed { i, role ->
                val attendance = project.attendance[i]
                val min = project.roles.getMin(role)
                val max = project.roles.getMax(role)
                if (attendance > max) {
                    // hard constraint: too many students
                    hardScore -= attendance - max
                } else if (totalAttendance > 0 && attendance < min) {
                    // hard constraint: too few students
                    hardScore -= min - attendance
                }
            }
        }

        return HardSoftScore.valueOf(hardScore, softScore)
    }

    companion object : KLogging() {
        fun debugScore(solution: ProjectAssignment) {
            AssignmentScoreCalculator().apply {
                resetWorkingSolution(solution)
            }

            for (project in solution.projects) {
                val totalAttendance = project.attendance.sum()
                Role.values().forEachIndexed { i, role ->
                    val attendance = project.attendance[i]
                    val max = project.roles.getMax(role)
                    val min = project.roles.getMin(role)
                    if (attendance > max) {
                        // hard constraint: too many students
                        logger.warn { "${attendance - max} too many students in $role for $project: $attendance > $max" }
                    } else if (totalAttendance > 0 && attendance < min) {
                        logger.warn { "${min - attendance} too few students in $role for $project: $attendance < $min, totalAttendance = $totalAttendance" }
                    }
                }
            }
        }
    }

}
