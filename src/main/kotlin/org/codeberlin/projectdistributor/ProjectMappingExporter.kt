package org.codeberlin.projectdistributor

import org.codeberlin.projectdistributor.data.Data
import java.io.File

object ProjectMappingExporter {
    @JvmStatic
    fun main(args: Array<String>) {
        val dir = File("local/data/final/2018-08-24_002220/withgc/temp 0-0-0-10 accepted 1 heuristic CHEAPEST_INSERTION/sub0")
        val data = Optimizer.loadMainData()

        val studentIds = data.users.map { it.name to it.id }.toMap()
        val projectIds = data.projects.map { it.name to it.id }.toMap()

        val online = File(dir, "data.tsv")
                .readLines()
                .filter { it.isNotBlank() }
                .map { line -> line.split('\t').let { StudentMapping(studentIds[it.first()]!!, projectIds[it.last()]!!) } }
                .toSet().sortedBy { it.studentId }

        val offline = AssignmentPersistence()
                .read(File(dir, "withgc.json"))
                .students.map { StudentMapping(it.id, it.chosenProject!!.id) }.toSet().sortedBy { it.studentId }

        when {
            online == offline -> println("identical")
            online.size == offline.size -> println(online.zip(offline).filter { it.first != it.second }.joinToString("\n") { (a, b) ->
                "${a.toString(data)} <> ${b.toString(data)}"
            })
            else -> {
                println("online")
                println(online.joinToString("\n"))
                println("\n\n\noffline")
                println(offline.joinToString("\n"))
            }
        }

        println(DataUtil.gson.toJson(online))
    }

    data class StudentMapping(val studentId: String, val projectId: String) {
        fun toString(data: Data): String {
            return "${data.users.find { studentId == it.id }?.name}: ${data.projects.find { projectId == it.id }?.name}"
        }
    }
}
