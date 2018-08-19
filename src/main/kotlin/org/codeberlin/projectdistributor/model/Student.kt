package org.codeberlin.projectdistributor.model

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.domain.variable.PlanningVariable

@PlanningEntity(difficultyComparatorClass = StudentDifficulty::class)
data class Student(
        val id: String,
        val name: String,
        val applications: List<Application>
) {
    constructor() : this("", "", emptyList())

    // reverse so that the first items are picked before the last
    @Transient
    val projectMap = applications.reversed().map { it.project to it }.toMap()

    // reversed so we get the original order, minus duplicates
    @Transient
    @ValueRangeProvider(id = "projects")
    val projects = projectMap.keys.reversed()

    @PlanningVariable(valueRangeProviderRefs = ["projects"])
    var chosenProject: Project? = null

    val chosenApplication: Application?
        get() = projectMap[chosenProject]

    override fun toString(): String {
        return name
    }
}

