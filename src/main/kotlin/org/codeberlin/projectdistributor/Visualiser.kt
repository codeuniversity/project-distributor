package org.codeberlin.projectdistributor

import mu.KotlinLogging
import org.apache.poi.xssf.usermodel.*
import org.codeberlin.projectdistributor.data.Role
import org.codeberlin.projectdistributor.model.ProjectAssignment
import java.awt.Desktop
import java.io.File

object Visualiser {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        for (path in args) {
            logger.info { "Analysing $path" }
            visualiseSolution(AssignmentPersistence()
                    .read(File(path)), path)
        }
    }

    private fun XSSFCell.write(value: Any, style: XSSFCellStyle? = null) {
        when (value) {
            is Number -> setCellValue(value.toDouble())
            else -> setCellValue(value.toString())
        }
        if (style != null) cellStyle = style
    }

    private fun XSSFRow.writeValues(vararg values: Any?, offset: Int = 0, style: XSSFCellStyle? = null) {
        values.forEachIndexed { col, value ->
            if (value != null) createCell(col + offset).write(value, style)
        }
    }

    private fun XSSFSheet.writeRow(rownum: Int, vararg values: Any?, style: XSSFCellStyle? = null) = createRow(rownum).apply { writeValues(*values, style = style) }!!

    fun visualiseSolution(solved: ProjectAssignment, path: String) {
        val out = File("$path.xlsx")

        val book = XSSFWorkbook()
        val bold = book.createCellStyle().apply {
            setFont(book.createFont().also { it.bold = true })
        }

        fun XSSFSheet.writeHeader(vararg values: Any?) = writeRow(0, *values, style = bold)

        // Overview Students
        // name, number of applications, number of fallbacks, chosen project name, chosen role, chosen priority
        val studentSheet = book.createSheet("students")
        studentSheet.writeHeader("Name", "# Applications", "# Fallbacks", "Chosen Project", "Chosen Role", "Chosen Priority")

        // Overview Applications (excluding fallbacks)
        // student name, project name, role, priority
        val appSheet = book.createSheet("applications")
        appSheet.writeHeader("Student", "Project", "Role", "Priority")
        var appRowCount = 1

        solved.students.sortedBy { it.name }.sortedByDescending { it.chosenApplication?.priority ?: 0 }.forEachIndexed { i, student ->
            val (apps, fallbacks) = student.applications.partition { it.priority > 0 }
            studentSheet.writeRow(i + 1,
                    student.name,
                    apps.size,
                    fallbacks.size,
                    student.chosenApplication?.project?.name,
                    student.chosenApplication?.role,
                    student.chosenApplication?.priority)

            apps.forEach {
                appSheet.writeRow(appRowCount,
                        student.name,
                        it.project.name,
                        it.role,
                        it.priority)
                appRowCount += 1
            }
        }

        // Overview Projects
        // name, for each role: { min, max, number of applications, chosen-non owners, owners }
        val projectSheet = book.createSheet("projects")
        val header = projectSheet.writeHeader("Name", "Students")
        Role.values().forEachIndexed { i, role ->
            header.writeValues("$role min", "$role max", "$role apps", "$role chosen", "$role owners",
                    offset = 2 + i * 5, style = bold)
        }

        val fullApps = solved.students.asSequence().flatMap {
            it.applications.asSequence().filter { it.priority > 0 }
        }.groupBy { it.project }

        val chosen = solved.students.asSequence().mapNotNull {
            it.chosenApplication
        }.groupBy { it.project }

        solved.projects.forEachIndexed { i, project ->
            val row = projectSheet.writeRow(i + 1, project.name, chosen[project]?.size)
            val apps = (fullApps[project] ?: emptyList()).groupBy { it.role }
            val choices = (chosen[project] ?: emptyList()).groupBy { it.role }

            Role.values().forEachIndexed { col, role ->
                row.writeValues(
                        project.roles?.getMin(role),
                        project.roles?.getMax(role),
                        apps[role]?.size,
                        choices[role]?.count { it.priority < 10 },
                        choices[role]?.count { it.priority == 10 },
                        offset = 2 + col * 5)
            }
        }

        for (i in 0..6) studentSheet.autoSizeColumn(i)
        for (i in 0..4) appSheet.autoSizeColumn(i)
        for (i in 0..(2 + Role.values().size * 5)) projectSheet.autoSizeColumn(i)

        // Display the result
        out.outputStream().use { book.write(it) }
        Desktop.getDesktop().browse(out.toURI())
    }
}
