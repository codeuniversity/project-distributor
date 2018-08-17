package org.codeberlin.projectdistributor

import mu.KotlinLogging


object Optimizer {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val data = DataUtil.fromStream(javaClass.getResourceAsStream("/project-applications.json")).data
        logger.debug { "data loaded: $data" }
    }
}
