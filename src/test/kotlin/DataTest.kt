package org.codeberlin.projectdistributor.test

import org.codeberlin.projectdistributor.DataUtil
import org.junit.Assert.*
import org.junit.Test

class DataTest {
    @Test
    fun testEmpty() {
        val result = DataUtil.fromJson("{}")
        assertNull(result.data)
    }


    @Test
    fun testParsing() {
        val result = DataUtil.fromStream(javaClass.getResourceAsStream("/example-project-applications.json"))
        val data = result.data
        assertNotNull(data)
        assertFalse(data.projects.isEmpty())
        assertFalse(data.users.isEmpty())

        assertEquals(2, data.projects.size)
        assertEquals(2, data.users.size)

        assertNotNull(data.projects[0])

        println("Projects:")
        data.projects.forEach { project ->
            println("   $project")
        }

        println("Users:")
        data.users.forEach { user ->
            println("   ${user.name}'s applications:")
            user.applications.forEach { application ->
                println("      $application")
            }
        }
    }
}
