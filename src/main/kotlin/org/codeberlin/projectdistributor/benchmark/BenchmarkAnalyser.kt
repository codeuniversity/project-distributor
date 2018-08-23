package org.codeberlin.projectdistributor.benchmark

import org.codeberlin.projectdistributor.Analyser
import org.codeberlin.projectdistributor.AssignmentPersistence
import org.codeberlin.projectdistributor.Visualiser
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore
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
                            val finalScore = score.readLines().last().split(",")
                            val scoreString = finalScore.last().replace("\"", "")
                            listOf(
                                    HardMediumSoftScore.parseScore(scoreString),
                                    score.parentFile.listFiles().firstOrNull { json -> json.name.endsWith(".json") },
                                    finalScore.first().toInt())
                        } catch (e: Exception) {
                            println("${file.name} -> $e")
                            null
                        }
                    } else null
                } ?: emptyList()
            } ?: emptyList()
        }

        val bestScores = scores.sortedByDescending { it[0] as HardMediumSoftScore }
        println(bestScores.joinToString("\n"))

        val bestFile = bestScores.first()[1] as File
        val excel = File(bestFile.parentFile, bestFile.name + ".xlsx")
        if (!excel.exists()) {
            println("saving excel at ${excel.absolutePath}")
            Visualiser.visualiseSolution(bestFile, true)
        } else {
            val persistence = AssignmentPersistence()
            scores.filter { it[0] as HardMediumSoftScore >= bestScores.first()[0] as HardMediumSoftScore }.forEach { row ->
                val file = row[1] as File
                println(file)
                Analyser.analyseSolution(persistence.read(file))
                println()
            }
        }
    }
}
