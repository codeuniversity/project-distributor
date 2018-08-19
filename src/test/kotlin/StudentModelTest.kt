package org.codeberlin.projectdistributor.test

import org.codeberlin.projectdistributor.data.Duration
import org.codeberlin.projectdistributor.data.Role
import org.codeberlin.projectdistributor.data.Roles
import org.codeberlin.projectdistributor.model.Application
import org.codeberlin.projectdistributor.model.Project
import org.codeberlin.projectdistributor.model.Student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentModelTest {
    @Test
    fun testPriority() {
        val project = Project(Duration.FULL, "test name", "test", Roles(0, 0, 0, 0, 0, 0))
        val high = Application(Role.SE, 8, project)
        val low = Application(Role.SE, -20, project)
        val student = Student("", "", listOf(
                high,
                low
        ))

        student.chosenProject = project
        assertEquals(high, student.chosenApplication)

        assertEquals(listOf(project), student.projects)
        assertEquals(mapOf(project to high), student.projectMap)
    }

    @Test
    fun testOrder() {
        val projects = IntRange(0, 10).map {
            Project(Duration.FULL, "p$it", "id-$it", Roles(0, 0, 0, 0, 0, 0))
        }

        val student = Student("", "", (projects + projects).mapIndexed { i, project ->
            Application(Role.SE, 10 - i, project)
        })

        student.chosenProject = projects[2]
        assertTrue(student.chosenApplication?.priority ?: 0 > 0)

        assertEquals(projects, student.projects)
    }
}
