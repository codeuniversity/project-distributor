package org.codeberlin.projectdistributor

import mu.KotlinLogging
import org.codeberlin.projectdistributor.ExecuteOptimizer.print
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.codeberlin.projectdistributor.score.AssignmentScoreCalculator
import java.io.File

object Analyser {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        for (path in args) {
            logger.info { "Analysing $path" }
            analyseSolution(AssignmentPersistence()
                    .read(File(path)))
        }
    }

    fun analyseSolution(solved: ProjectAssignment) {
        // if any hard constrainst are mismatched, print them:
        AssignmentScoreCalculator.debugScore(solved)

        // Display the result
        logger.info {
            val (winners, losers) = solved.students.partition { it.chosenApplication!!.priority > 0 }
            val (running, cancelled) = solved.projects.partition { it.attendance.sum() > 0 }
            "Solved assignment with score ${solved.score}.\n" +
                    "Students winning applications: ${winners.size}\n" +
                    // "\t${winners.print()}\n" +
                    "Students losing applications: ${losers.size}\n" +
                    "\t${losers.print()}\n\n" +
                    "Projects with students: ${running.size}\n" +
                    // "${running.joinToString()}\n" +
                    "Projects without students: ${cancelled.size}\n"
                    // + "${cancelled.joinToString()}\n"
        }
    }
}
