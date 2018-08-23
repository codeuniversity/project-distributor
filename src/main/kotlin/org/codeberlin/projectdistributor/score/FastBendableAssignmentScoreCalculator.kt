package org.codeberlin.projectdistributor.score

import org.codeberlin.projectdistributor.data.Role
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.codeberlin.projectdistributor.model.Student
import org.optaplanner.core.api.score.buildin.bendable.BendableScore
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator
import java.lang.Math.min

class FastBendableAssignmentScoreCalculator : IncrementalScoreCalculator<ProjectAssignment> {
    private var bounds = emptyArray<IntArray>()
    private val position = HashMap<String, Int>()

    private var hardScores: IntArray = intArrayOf(0, 0, 0)
    private var softScores: IntArray = intArrayOf(0)

    override fun resetWorkingSolution(workingSolution: ProjectAssignment?) {
        position.clear()
        hardScores.fill(0)
        softScores.fill(0)

        workingSolution?.apply {
            if (bounds.size != projects.size)
                bounds = Array(projects.size) { IntArray(roleCount * 3 + 1) }
            else
                bounds.forEach { it.fill(0) }

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
            softScores[0] += value * priority

            val pos = position[project.id]!!
            val limits = bounds[pos]

            val beforeMedium = calcMedium(limits)

            limits[0] += value
            if (priority < 10) {
                val i = role.ordinal * 3 + 3
                val beforeHard = min(0, limits[i - 1] - limits[i])
                limits[i] += value
                val afterHard = min(0, limits[i - 1] - limits[i])
                hardScores[0] += afterHard - beforeHard
            }

            hardScores[1] += calcMedium(limits) - beforeMedium

            if (priority < 0) hardScores[2] -= value
        }
    }

    private fun calcMedium(limits: IntArray): Int {
        if (limits[0] == 0) return 0
        return (0 until roleCount).sumBy { i ->
            min(0, limits[3 * i + 3] - limits[3 * i + 1])
        }
    }

    override fun calculateScore(): BendableScore =
            BendableScore.valueOf(hardScores.clone(), softScores.clone())

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
