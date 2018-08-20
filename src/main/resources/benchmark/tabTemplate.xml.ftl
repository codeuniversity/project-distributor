<?xml version="1.0" encoding="UTF-8"?>
<plannerBenchmark>
  <benchmarkDirectory>local/data/template</benchmarkDirectory>
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
            <incrementalScoreCalculatorClass>org.codeberlin.projectdistributor.score.FastAssignmentScoreCalculator</incrementalScoreCalculatorClass>
        </scoreDirectorFactory>
      <termination>
        <minutesSpentLimit>1</minutesSpentLimit>
      </termination>
      <constructionHeuristic>
        <constructionHeuristicType>FIRST_FIT_DECREASING</constructionHeuristicType>
      </constructionHeuristic>
    </solver>
  </inheritedSolverBenchmark>

<#list [5, 7, 11, 13] as entityTabuSize>
<#list [500, 1000, 2000] as acceptedCountLimit>
  <solverBenchmark>
    <name>entityTabuSize ${entityTabuSize} acceptedCountLimit ${acceptedCountLimit}</name>
    <solver>
      <localSearch>
        <unionMoveSelector>
          <changeMoveSelector/>
          <swapMoveSelector/>
        </unionMoveSelector>
        <acceptor>
          <entityTabuSize>${entityTabuSize}</entityTabuSize>
        </acceptor>
        <forager>
          <acceptedCountLimit>${acceptedCountLimit}</acceptedCountLimit>
        </forager>
      </localSearch>
    </solver>
  </solverBenchmark>
</#list>
</#list>
</plannerBenchmark>
