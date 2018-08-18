/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.codeberlin.projectdistributor.benchmark

import mu.KotlinLogging
import org.codeberlin.projectdistributor.Optimizer
import org.codeberlin.projectdistributor.model.ProjectAssignment
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory

object BasicBenchmark {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val data = Optimizer.loadMainData()

        // Build the PlannerBenchmark
        val benchmark = args.getOrNull(0) ?: "benchmark/basic.xml"
        logger.info { "benchmarking $benchmark" }

        PlannerBenchmarkFactory.createFromXmlResource(benchmark)
                .buildPlannerBenchmark(ProjectAssignment.convert(data))
                .benchmarkAndShowReportInBrowser()
    }

}
