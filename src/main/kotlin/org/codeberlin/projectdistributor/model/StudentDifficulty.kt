package org.codeberlin.projectdistributor.model

class StudentDifficulty : Comparator<Student> {
    val inner = compareByDescending<Student> { it.applications.size }
    override fun compare(a: Student?, b: Student?) = inner.compare(a, b)
}
