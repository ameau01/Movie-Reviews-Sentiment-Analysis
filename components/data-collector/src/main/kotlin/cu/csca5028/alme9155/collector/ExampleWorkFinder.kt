package cu.csca5028.alme9155.collector

import cu.csca5028.alme9155.workflow.WorkFinder
import org.slf4j.LoggerFactory

class ExampleWorkFinder : WorkFinder<ExampleTask> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun findRequested(name: String): List<ExampleTask> {
        logger.info("finding work.")

        val work = ExampleTask("some info")

        return mutableListOf(work)
    }

    override fun markCompleted(info: ExampleTask) {
        logger.info("marking work complete.")
    }
}
