<?xml version="1.0" encoding="UTF-8"?>
<plannerBenchmark>
  <benchmarkDirectory>local/data/template-an</benchmarkDirectory>
  <parallelBenchmarkCount>AUTO</parallelBenchmarkCount>

  <inheritedSolverBenchmark>
      <problemBenchmarks>
          <solutionFileIOClass>org.codeberlin.projectdistributor.AssignmentPersistence</solutionFileIOClass>
          <inputSolutionFileList>local/data/input/unsolved-t.json</inputSolutionFileList>
          <writeOutputSolutionEnabled>true</writeOutputSolutionEnabled>
      </problemBenchmarks>

    <solver>
        <solutionClass>org.codeberlin.projectdistributor.model.ProjectAssignment</solutionClass>
        <entityClass>org.codeberlin.projectdistributor.model.Student</entityClass>
        <scoreDirectorFactory>
            <incrementalScoreCalculatorClass>org.codeberlin.projectdistributor.score.AssignmentScoreCalculator</incrementalScoreCalculatorClass>
        </scoreDirectorFactory>
      <termination>
        <minutesSpentLimit>2</minutesSpentLimit>
      </termination>
    </solver>
  </inheritedSolverBenchmark>

<#list [1, 4, 100] as acceptedCountLimit>
<#list [1, 15, 20, 40, 60, 10000] as temperature>
<#list ["CHEAPEST_INSERTION", "FIRST_FIT_DECREASING"] as heuristic>
  <solverBenchmark>
    <name>temp ${temperature} accepted ${acceptedCountLimit} heuristic ${heuristic}</name>
    <solver>
        <constructionHeuristic>
            <constructionHeuristicType>${heuristic}</constructionHeuristicType>
        </constructionHeuristic>
      <localSearch>
          <acceptor>
              <simulatedAnnealingStartingTemperature>0hard/0medium/${temperature}soft</simulatedAnnealingStartingTemperature>
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
</plannerBenchmark>
