/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codeberlin.projectdistributor

import mu.KotlinLogging
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.codeberlin.projectdistributor.model.Student
import org.codeberlin.projectdistributor.score.AssignmentScoreCalculator
import org.optaplanner.core.api.solver.SolverFactory

object ExecuteOptimizer {
    private val logger = KotlinLogging.logger {}

    private fun List<Student>.print() = sortedBy { it.name }
            .joinToString("\n\t") { "%-30s %s".format(it.name, it.chosenApplication) }

    @JvmStatic
    fun main(args: Array<String>) {
        // Build the Solver
        val solver = SolverFactory.createFromXmlResource<ProjectAssignment>(
                "solverConfig.xml")
                .buildSolver()

        // Load the data
        val data = Optimizer.loadMainData()

        val unsolved = ProjectAssignment.convert(data)
        unsolved.debugContent()

        // Solve the problem
        val solved = solver.solve(unsolved)
        solved.debugContent()

        // Display the result
        logger.info {
            val (winners, losers) = solved.students.partition { it.chosenApplication!!.priority > 0 }
            "Solved assignment with score ${solved.score}.\n" +
                    "Students winning applications: ${winners.size}\n\t" +
                    "${winners.print()}\n" +
                    "Students losing applications: ${losers.size}\n\t" +
                    "${losers.print()}\n"
        }

        AssignmentScoreCalculator.debugScore(solved)
    }
}
