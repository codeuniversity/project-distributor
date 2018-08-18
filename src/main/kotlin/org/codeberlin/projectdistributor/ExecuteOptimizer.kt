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
import org.optaplanner.core.api.solver.SolverFactory

object ExecuteOptimizer {
    private val logger = KotlinLogging.logger {}


    @JvmStatic
    fun main(args: Array<String>) {
        // Build the Solver
        val solver = SolverFactory.createFromXmlResource<ProjectAssignment>(
                "solverConfig.xml")
                .buildSolver()

        // Load the data
        val jsonStream = javaClass.getResourceAsStream("/project-applications.json")
        if (jsonStream == null) {
            logger.warn { "please put a file called project-applications.json into the src/main/resources folder" }
            return
        }
        val data = DataUtil.fromStream(jsonStream).data
        val unsolved = ProjectAssignment.convert(data)

        // Solve the problem
        val solved = solver.solve(unsolved)

        // Display the result
        logger.info {
            val (assigned, unassigned) = solved.students.partition { it.chosenApplication != null }
            "Solved assignement with score ${solved.score}. " +
                    "Students without project: ${unassigned.size}.\n" +
                    "Students with projects:\n\t" +
                    "${assigned.joinToString("\n\t") { "%-20s %s".format(it.name, it.chosenApplication) }}}"
        }
    }
}
