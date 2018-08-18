package org.codeberlin.projectdistributor.model

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.domain.variable.PlanningVariable

@PlanningEntity(difficultyComparatorClass = StudentDifficulty::class)
data class Student(
        val id: String,
        val name: String,
        @ValueRangeProvider(id = "applicationRange")
        val applications: List<Application>,
        @PlanningVariable(valueRangeProviderRefs = ["applicationRange"], strengthComparatorClass = ApplicationStrength::class)
        var chosenApplication: Application? = null
) {
    constructor() : this("", "", emptyList())

    override fun toString(): String {
        return name
    }
}

