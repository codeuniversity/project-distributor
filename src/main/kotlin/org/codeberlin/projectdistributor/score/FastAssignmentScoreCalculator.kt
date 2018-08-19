package org.codeberlin.projectdistributor.score

import org.codeberlin.projectdistributor.data.Role
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.codeberlin.projectdistributor.model.Student
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator
import java.lang.Math.min

class FastAssignmentScoreCalculator : IncrementalScoreCalculator<ProjectAssignment> {
    private var bounds = emptyArray<IntArray>()
    private val position = HashMap<String, Int>()

    private var hardScore: Int = 0
    private var mediumScore: Int = 0
    private var softScore: Int = 0

    override fun resetWorkingSolution(workingSolution: ProjectAssignment?) {
        position.clear()
        hardScore = 0
        mediumScore = 0
        softScore = 0
        workingSolution?.apply {
            bounds = Array(projects.size) { IntArray(roleCount * 3 + 1) }
            projects.forEachIndexed { i, project ->
                position[project.id] = i
                val limits = bounds[i]
                project.roles?.apply {
                    Role.values().forEachIndexed { j, role ->
                        limits[3 * j + 1] = getMin(role)
                        limits[3 * j + 2] = getMax(role)
                    }
                }
            }
            students.forEach(::insert)
        }
    }

    private fun add(student: Student, value: Int) {
        student.chosenApplication?.apply {
            softScore += value * priority

            val pos = position[project.id]!!
            val limits = bounds[pos]

            val beforeMedium = calcMedium(limits)

            limits[0] += value
            if (priority < 10) {
                val i = role.ordinal * 3 + 3
                val beforeHard = min(0, limits[i - 1] - limits[i])
                limits[i] += value
                val afterHard = min(0, limits[i - 1] - limits[i])
                hardScore += afterHard - beforeHard
            }

            mediumScore += calcMedium(limits) - beforeMedium
        }
    }

    private fun calcMedium(limits: IntArray): Int {
        if (limits[0] == 0) return 0
        return (0 until roleCount).sumBy { i ->
            min(0, limits[3 * i + 3] - limits[3 * i + 1])
        }
    }

    override fun calculateScore(): HardMediumSoftScore =
            HardMediumSoftScore.valueOf(hardScore, mediumScore, softScore)

    override fun beforeEntityAdded(entity: Any?) {}
    override fun afterEntityAdded(entity: Any?) = insert(entity)

    override fun beforeEntityRemoved(entity: Any?) = retract(entity)
    override fun afterEntityRemoved(entity: Any?) {}

    override fun beforeVariableChanged(entity: Any?, variableName: String?) = retract(entity)
    override fun afterVariableChanged(entity: Any?, variableName: String?) = insert(entity)

    private fun insert(entity: Any?) = add(entity as Student, 1)
    private fun retract(entity: Any?) = add(entity as Student, -1)

    companion object {
        private const val roleCount = 3
    }
}
