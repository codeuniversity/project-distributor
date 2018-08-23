package org.codeberlin.projectdistributor.benchmark

import org.codeberlin.projectdistributor.Analyser
import org.codeberlin.projectdistributor.AssignmentPersistence
import org.codeberlin.projectdistributor.Visualiser
import org.optaplanner.core.api.score.buildin.bendable.BendableScore
import java.io.File

object BenchmarkAnalyser {
    @JvmStatic
    fun main(args: Array<String>) {
        val directory = File("local/data/final").listFiles().sortedByDescending { it.name }.first()
        println("checking $directory")
        val scores = directory.listFiles().flatMap { top ->
            top.listFiles()?.flatMap { file ->
                file.listFiles()?.mapNotNull { sub ->
                    val score = File(sub, "BEST_SCORE.csv")
                    if (score.isFile) {
                        try {
                            val split = score.readLines().last().split(",")
                            val scoreString = split.last().replace("\"", "")
                            Triple(BendableScore.parseScore(scoreString),
                                    score.parentFile.listFiles().firstOrNull { json -> json.name.endsWith(".json") },
                                    split.first().toInt())
                        } catch (e: Exception) {
                            println("${file.name} -> $e")
                            null
                        }
                    } else null
                } ?: emptyList()
            } ?: emptyList()
        }

        val bestScores = scores.sortedByDescending { it.first }
        println(bestScores.joinToString("\n"))

        val bestFile = bestScores.first().second!!
        val excel = File(bestFile.parentFile, bestFile.name + ".xlsx")
        if (!excel.exists()) {
            println("saving excel at ${excel.absolutePath}")
            Visualiser.visualiseSolution(bestFile, true)
        } else {
            val persistence = AssignmentPersistence()
            scores.filter { it.first >= bestScores.first().first }.forEach { (_, file, _) ->
                println(file)
                Analyser.analyseSolution(persistence.read(file))
                println()
            }
        }
    }
}
