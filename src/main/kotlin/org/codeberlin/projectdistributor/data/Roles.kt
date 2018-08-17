package org.codeberlin.projectdistributor.data

data class Roles(
        val minID: Int,
        val maxID: Int,
        val minPM: Int,
        val maxPM: Int,
        val minSE: Int,
        val maxSE: Int
) {

    /**
     * get the minimum number of students for a specific role
     */
    fun getMin(role: Role) =
            when (role) {
                Role.ID -> minID
                Role.PM -> minPM
                Role.SE -> minSE
            }

    /**
     * get the minimum number of students for a specific role
     */
    fun getMax(role: Role) =
            when (role) {
                Role.ID -> maxID
                Role.PM -> maxPM
                Role.SE -> maxSE
            }

    fun enoughApplications(applications: List<Application>): Boolean {
        val grouped = applications.groupBy { it.role }
        return Role.values().count { getMin(it) > grouped[it]?.size ?: 0 } == 0
    }

    fun missingApplications(applications: List<Application>): List<Role> {
        val grouped = applications.groupBy { it.role }
        return Role.values().filter { getMin(it) > grouped[it]?.size ?: 0 }
    }

}
