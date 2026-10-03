package tech.nikelyh.rumbo.core.data.model

import tech.nikelyh.rumbo.core.database.model.WeeklyGoalEntity
import tech.nikelyh.rumbo.core.model.GoalStatus
import tech.nikelyh.rumbo.core.model.WeeklyGoal

fun WeeklyGoalEntity.asExternalModel(): WeeklyGoal = WeeklyGoal(
    id = id,
    processId = processId,
    weekIdentifier = weekIdentifier,
    description = description,
    status = runCatching { GoalStatus.valueOf(statusName) }.getOrDefault(GoalStatus.PENDING)
)

fun WeeklyGoal.asEntity(): WeeklyGoalEntity = WeeklyGoalEntity(
    id = id,
    processId = processId,
    weekIdentifier = weekIdentifier,
    description = description,
    statusName = status.name
)
