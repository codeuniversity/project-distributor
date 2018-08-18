package org.codeberlin.projectdistributor.model

class ApplicationStrength : Comparator<Application> {
    private val inner = compareBy<Application> { it.priority }
    override fun compare(a: Application?, b: Application?): Int = inner.compare(a, b)
}
