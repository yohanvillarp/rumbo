package tech.nikelyh.rumbo.core.data.testing

import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus

/**
 * Enterprise test data fixture builder for [Process] entities.
 * Decouples unit tests from constructor changes and provides fluent DSL semantics.
 */
class ProcessFixtureBuilder(
    var id: String = "test-process-1",
    var name: String = "Test Process",
    var description: String? = "Test process description",
    var status: ProcessStatus = ProcessStatus.ACTIVE,
    var createdAtEpochMillis: Long = 1000L,
    var finishedAtEpochMillis: Long? = null,
    var colorOrVisualId: String = "teal",
    var accumulatedDirectCost: Double = 0.0,
    var isStarred: Boolean = false,
    var parentProcessId: String? = null
) {
    fun withId(id: String) = apply { this.id = id }
    fun withName(name: String) = apply { this.name = name }
    fun withDescription(desc: String?) = apply { this.description = desc }
    fun withStatus(status: ProcessStatus) = apply { this.status = status }
    fun withStarred(starred: Boolean) = apply { this.isStarred = starred }
    fun withParent(parentId: String?) = apply { this.parentProcessId = parentId }
    fun withCost(cost: Double) = apply { this.accumulatedDirectCost = cost }

    fun build(): Process = Process(
        id = id,
        name = name,
        description = description,
        status = status,
        createdAtEpochMillis = createdAtEpochMillis,
        finishedAtEpochMillis = finishedAtEpochMillis,
        colorOrVisualId = colorOrVisualId,
        accumulatedDirectCost = accumulatedDirectCost,
        isStarred = isStarred,
        parentProcessId = parentProcessId
    )
}

fun aProcess(block: ProcessFixtureBuilder.() -> Unit = {}): Process =
    ProcessFixtureBuilder().apply(block).build()
