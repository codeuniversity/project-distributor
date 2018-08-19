package org.codeberlin.projectdistributor.score

import mu.KLogging
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.optaplanner.core.api.score.Score
import org.optaplanner.core.impl.score.director.incremental.IncrementalScoreCalculator

// For debugging: Check if a list of ScoreCalculators leads to the same result every time
class ComparingAssignmentScoreCalculator : IncrementalScoreCalculator<ProjectAssignment> {
    private val inner = arrayOf(AssignmentScoreCalculator(), FastAssignmentScoreCalculator())

    override fun calculateScore(): Score<out Score<*>>? {
        val scores = inner.map { it.calculateScore() }

        if (scores.distinct().count() != 1) {
            logger.error { "Results are not the same: $scores" }
        }

        return scores[0]
    }

    override fun resetWorkingSolution(workingSolution: ProjectAssignment?) =
            inner.forEach { it.resetWorkingSolution(workingSolution) }

    override fun beforeEntityAdded(entity: Any?) =
            inner.forEach { it.beforeEntityAdded(entity) }

    override fun afterEntityAdded(entity: Any?) =
            inner.forEach { it.afterEntityAdded(entity) }

    override fun beforeEntityRemoved(entity: Any?) =
            inner.forEach { it.beforeEntityRemoved(entity) }

    override fun afterEntityRemoved(entity: Any?) =
            inner.forEach { it.afterEntityRemoved(entity) }

    override fun beforeVariableChanged(entity: Any?, variableName: String?) =
            inner.forEach { it.beforeVariableChanged(entity, variableName) }

    override fun afterVariableChanged(entity: Any?, variableName: String?)=
            inner.forEach { it.afterVariableChanged(entity, variableName) }

    companion object : KLogging()
}
