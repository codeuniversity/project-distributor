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
import org.codeberlin.projectdistributor.Analyser.analyseSolution
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.optaplanner.core.api.solver.SolverFactory
import java.io.File

object ExecuteDroolsOptimizer {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "Starting drools execution" }

        // Build the Solver
        val solver = SolverFactory.createFromXmlResource<ProjectAssignment>(
                "solverConfigDrools.xml").buildSolver()

        // Load the data, can be from a previous calculation
        val unsolved =
                if (args.isEmpty()) ProjectAssignment.convert(Optimizer.loadMainData())
                else AssignmentPersistence().read(File(args[0]))

        // Solve the problem
        val solved = solver.solve(unsolved)
        analyseSolution(solved)

        logger.info { solver.explainBestScore() }
    }
}
