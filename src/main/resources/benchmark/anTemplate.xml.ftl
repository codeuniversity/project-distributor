<?xml version="1.0" encoding="UTF-8"?>
<plannerBenchmark>
    <benchmarkDirectory>local/data/final</benchmarkDirectory>
    <parallelBenchmarkCount>4</parallelBenchmarkCount>

    <inheritedSolverBenchmark>
        <problemBenchmarks>
            <solutionFileIOClass>org.codeberlin.projectdistributor.AssignmentPersistence</solutionFileIOClass>
            <#--<inputSolutionFileList>local/data/input/nogc.json</inputSolutionFileList>-->
            <inputSolutionFileList>local/data/input/withgc.json</inputSolutionFileList>
            <writeOutputSolutionEnabled>true</writeOutputSolutionEnabled>
        </problemBenchmarks>

        <solver>
            <solutionClass>org.codeberlin.projectdistributor.model.ProjectAssignment</solutionClass>
            <entityClass>org.codeberlin.projectdistributor.model.Student</entityClass>
            <scoreDirectorFactory>
                <incrementalScoreCalculatorClass>org.codeberlin.projectdistributor.score.FastAssignmentScoreCalculator</incrementalScoreCalculatorClass>
            </scoreDirectorFactory>
            <termination>
                <minutesSpentLimit>8</minutesSpentLimit>
            </termination>
            <environmentMode>NON_REPRODUCIBLE</environmentMode>
        </solver>
    </inheritedSolverBenchmark>

<#list [1, 4] as acceptedCountLimit>
	<#list [0, 5, 8] as medTemp>
		<#list [10, 15, 20, 40] as softTemp>
			<#list ["CHEAPEST_INSERTION"] as heuristic>
  <solverBenchmark>
      <name>temp ${medTemp}-${softTemp} accepted ${acceptedCountLimit} heuristic ${heuristic}</name>
      <solver>
          <constructionHeuristic>
              <constructionHeuristicType>${heuristic}</constructionHeuristicType>
          </constructionHeuristic>
          <localSearch>
              <acceptor>
                  <simulatedAnnealingStartingTemperature>0hard/${medTemp}medium/${softTemp}soft</simulatedAnnealingStartingTemperature>
              </acceptor>
              <forager>
                  <acceptedCountLimit>${acceptedCountLimit}</acceptedCountLimit>
              </forager>
          </localSearch>
      </solver>
  </solverBenchmark>
			</#list>
		</#list>
	</#list>
</#list>
</plannerBenchmark>
