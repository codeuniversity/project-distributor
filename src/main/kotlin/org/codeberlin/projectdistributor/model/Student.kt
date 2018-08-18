package org.codeberlin.projectdistributor.model

import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.domain.variable.PlanningVariable

@PlanningEntity
data class Student(
        val id: String,
        val name: String,
        @ValueRangeProvider(id = "applicationRange")
        val applications: List<Application?>
) {
    constructor() : this("", "", emptyList())

    @PlanningVariable(valueRangeProviderRefs = ["applicationRange"])
    var chosenApplication: Application? = null

    override fun toString(): String {
        return name
    }
}
