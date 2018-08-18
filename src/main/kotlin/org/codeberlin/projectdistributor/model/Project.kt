package org.codeberlin.projectdistributor.model

import org.codeberlin.projectdistributor.data.Duration
import org.codeberlin.projectdistributor.data.Roles

data class Project(
        val duration: Duration,
        val name: String,
        val id: String,
        val roles: Roles
) {
    // for keeping track of enrolled students during modelling
    val attendance: IntArray = intArrayOf(0, 0, 0)

    override fun toString(): String {
        return if (duration == Duration.HALF) "HALF $name" else name
    }
}
