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

import ch.qos.logback.classic.LoggerContext
import mu.KotlinLogging
import org.codeberlin.projectdistributor.Analyser.analyseSolution
import org.codeberlin.projectdistributor.Visualiser.visualiseSolution
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.codeberlin.projectdistributor.model.Student
import org.optaplanner.core.api.solver.SolverFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



object ExecuteOptimizer {
    private val logger = KotlinLogging.logger {}
    private const val resultDir = "local/data/results"

    fun List<Student>.print() = sortedBy { it.name }
            .joinToString("\n\t") { "%-30s %s".format(it.name, it.chosenApplication) }

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "Starting optimizer execution (args: ${args.toList()})" }

        // Build the Solver
        val solver = SolverFactory.createFromXmlResource<ProjectAssignment>(
                "solverConfig.xml").buildSolver()

        // Load the data, can be from a previous calculation
        val unsolved =
                if (args.isEmpty()) ProjectAssignment.convert(Optimizer.loadMainData())
                else AssignmentPersistence().read(File(args[0]))

        val fmt = DateTimeFormatter.ofPattern("uuuu-MM-dd_HHmmss")
        val tstamp = fmt.format(LocalDateTime.now())
        unsolved.export("$tstamp-unsolved")

        // Solve the problem
        val solved = solver.solve(unsolved)

        val outputName = "$tstamp-solved-${fmt.format(LocalDateTime.now())}-${solved.score?.hardScore}-${solved.score?.softScore}"
        solved.export(outputName)

        analyseSolution(solved)

        val outputPath = "$resultDir/$outputName.json"
        logger.info { "saved result at $outputPath, visualizing in excel" }
        visualiseSolution(solved, outputPath)
        logger.info { "done" }

        // shutdown logback-classic if available
        (LoggerFactory.getILoggerFactory() as? LoggerContext)?.stop()
    }

    private fun ProjectAssignment.export(name: String) {
        AssignmentPersistence().write(this, File(File(resultDir).apply { mkdirs() }, "$name.json"))
    }
}
