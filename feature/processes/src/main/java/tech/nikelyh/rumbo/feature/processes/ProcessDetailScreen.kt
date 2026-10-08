package tech.nikelyh.rumbo.feature.processes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tech.nikelyh.rumbo.core.designsystem.component.CelebrationCinematicDialog
import tech.nikelyh.rumbo.core.designsystem.component.RumboButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboProcessCard
import tech.nikelyh.rumbo.core.designsystem.component.RumboEmptyState
import tech.nikelyh.rumbo.core.designsystem.component.RumboLoadingState
import tech.nikelyh.rumbo.core.designsystem.component.RumboOutlinedButton
import tech.nikelyh.rumbo.core.designsystem.component.RumboSectionHeader
import tech.nikelyh.rumbo.core.designsystem.component.RumboTaskItem
import tech.nikelyh.rumbo.core.designsystem.component.RumboTonalButton
import tech.nikelyh.rumbo.core.designsystem.component.TaskCompletionCelebration
import tech.nikelyh.rumbo.core.designsystem.component.TaskCompletionDurationDialog
import tech.nikelyh.rumbo.core.designsystem.theme.RumboTheme
import tech.nikelyh.rumbo.core.model.Priority
import tech.nikelyh.rumbo.core.model.Process
import tech.nikelyh.rumbo.core.model.ProcessStatus
import tech.nikelyh.rumbo.core.model.Task
import tech.nikelyh.rumbo.core.model.TaskSortOrder
import tech.nikelyh.rumbo.core.model.TaskStatus
import tech.nikelyh.rumbo.core.model.WeeklyGoal

@Composable
fun ProcessDetailRoute(
    onNavigateToEditProcess: (String) -> Unit,
    onNavigateToCreateTask: (String) -> Unit,
    onNavigateToStartSession: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCreateProcess: (String?) -> Unit = {},
    onNavigateToProcessDetail: (String) -> Unit = {},
    onNavigateToLogProgress: ((String) -> Unit)? = null,
    onNavigateToTask: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: ProcessDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isDeleted = uiState is ProcessDetailUiState.Deleted || (uiState as? ProcessDetailUiState.Content)?.isDeleted == true
    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            onNavigateBack()
        }
    }

    ProcessDetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToEditProcess = { onNavigateToEditProcess(viewModel.processId) },
        onNavigateToCreateTask = { onNavigateToCreateTask(viewModel.processId) },
        onNavigateToStartSession = { taskId -> onNavigateToStartSession(viewModel.processId, taskId) },
        onNavigateToCreateProcess = onNavigateToCreateProcess,
        onNavigateToProcessDetail = onNavigateToProcessDetail,
        onNavigateToTask = onNavigateToTask,
        modifier = modifier
    )
}

@Composable
internal fun ProcessDetailScreen(
    uiState: ProcessDetailUiState,
    onEvent: (ProcessDetailUiEvent) -> Unit,
    onNavigateToEditProcess: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToStartSession: (String?) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCreateProcess: (String?) -> Unit = {},
    onNavigateToProcessDetail: (String) -> Unit = {},
    onNavigateToTask: (String) -> Unit = {}
) {
    when (uiState) {
        ProcessDetailUiState.Loading -> {
            RumboLoadingState(isLoading = true, modifier = modifier)
        }
        ProcessDetailUiState.Deleted -> {
            Box(modifier = modifier.fillMaxSize())
        }
        is ProcessDetailUiState.Error -> {
            RumboEmptyState(message = uiState.message, modifier = modifier)
        }
        is ProcessDetailUiState.Content -> {
            ProcessDetailContent(
                uiState = uiState,
                onEvent = onEvent,
                onNavigateToEditProcess = onNavigateToEditProcess,
                onNavigateToCreateTask = onNavigateToCreateTask,
                onNavigateToStartSession = onNavigateToStartSession,
                onNavigateToCreateProcess = onNavigateToCreateProcess,
                onNavigateToProcessDetail = onNavigateToProcessDetail,
                onNavigateToTask = onNavigateToTask,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ProcessDetailContent(
    uiState: ProcessDetailUiState.Content,
    onEvent: (ProcessDetailUiEvent) -> Unit,
    onNavigateToEditProcess: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToStartSession: (String?) -> Unit,
    onNavigateToCreateProcess: (String?) -> Unit,
    onNavigateToProcessDetail: (String) -> Unit,
    onNavigateToTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val process = uiState.process
    var taskToCompleteWithDuration by remember { mutableStateOf<Task?>(null) }
    var showProcessCelebration by remember { mutableStateOf(false) }
    var celebratingTaskTitle by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Header
        item {
            RumboCard(modifier = Modifier.fillMaxWidth()) {
                val processStatusLabel = stringResource(
                    when (process.status) {
                        ProcessStatus.ACTIVE -> tech.nikelyh.rumbo.core.designsystem.R.string.status_active
                        ProcessStatus.PAUSED -> tech.nikelyh.rumbo.core.designsystem.R.string.status_paused
                        ProcessStatus.COMPLETED -> tech.nikelyh.rumbo.core.designsystem.R.string.status_completed
                        ProcessStatus.ARCHIVED -> tech.nikelyh.rumbo.core.designsystem.R.string.status_archived
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = process.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = processStatusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (!process.isSystem) {
                            IconButton(onClick = { onEvent(ProcessDetailUiEvent.ToggleStar) }) {
                                Icon(
                                    imageVector = if (process.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (process.isStarred) {
                                        stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_action_unstar)
                                    } else {
                                        stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_action_star)
                                    },
                                    tint = if (process.isStarred) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                process.description?.let { desc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                process.dueDateEpochMillis?.let { dueEpoch ->
                    Spacer(modifier = Modifier.height(8.dp))
                    val dateText = remember(dueEpoch) {
                        val instant = java.time.Instant.ofEpochMilli(dueEpoch)
                        val zone = java.time.ZoneId.systemDefault()
                        instant.atZone(zone).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm a"))
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_card_due_date_prefix, dateText),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val timeHours = uiState.totalTimeInvestedMillis / (1000 * 60 * 60)
                val timeMinutes = (uiState.totalTimeInvestedMillis / (1000 * 60)) % 60

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${timeHours}h ${timeMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${process.accumulatedDirectCost}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF1976D2)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                 text = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_sessions_count, uiState.workSessions.size),
                                 style = MaterialTheme.typography.labelSmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant
                             )
                        }
                    }
                }

                if (uiState.parentProcess != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onNavigateToProcessDetail(uiState.parentProcess.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountTree,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_part_of, uiState.parentProcess.name),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                if (process.isSystem) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_general_info),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Primary Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!process.isFinished) {
                    if (!process.isSystem) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RumboButton(
                                onClick = onNavigateToCreateTask,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddTask, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_new_task))
                            }

                            RumboTonalButton(
                                onClick = { onNavigateToCreateProcess(process.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AccountTree, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_new_subprocess))
                            }
                        }
                    } else {
                        RumboButton(
                            onClick = onNavigateToCreateTask,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_new_task))
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_completed_banner),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    RumboButton(
                        onClick = { onEvent(ProcessDetailUiEvent.ReopenProcess) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_reopen))
                    }
                }

                if (!process.isSystem) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RumboTonalButton(
                            onClick = onNavigateToEditProcess,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.action_edit))
                        }

                        if (process.isActive || process.status == ProcessStatus.PAUSED) {
                            RumboTonalButton(
                                onClick = {
                                    if (uiState.completionBlockedReason == null && !process.isSystem) {
                                        showProcessCelebration = true
                                    }
                                    onEvent(ProcessDetailUiEvent.FinishProcess)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_finish))
                            }
                        }
                    }

                    if (uiState.completionBlockedReason != null && (process.isActive || process.status == ProcessStatus.PAUSED)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.completionBlockedReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    if (!process.isSystem && process.id != tech.nikelyh.rumbo.core.model.Process.GENERAL_PROCESS_ID) {
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        Spacer(modifier = Modifier.height(4.dp))
                        RumboTonalButton(
                            onClick = { showDeleteDialog = true },
                            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.action_delete_process),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.dialog_delete_process_title)) },
                                text = { Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.dialog_delete_process_message)) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showDeleteDialog = false
                                            onEvent(ProcessDetailUiEvent.DeleteProcess)
                                        }
                                    ) {
                                        Text(
                                            text = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.action_delete),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.action_cancel))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section: Subprocesses (shown when sub-processes exist)
        if (uiState.subProcesses.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RumboSectionHeader(
                        title = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_subprocesses_title, uiState.subProcesses.size),
                        modifier = Modifier.weight(1f)
                    )
                    if (!process.isFinished && !process.isSystem) {
                        TextButton(onClick = { onNavigateToCreateProcess(process.id) }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_new_subprocess))
                        }
                    }
                }
            }

            items(uiState.subProcesses, key = { it.id }) { subProcess ->
                RumboProcessCard(
                    process = subProcess,
                    onClick = { onNavigateToProcessDetail(subProcess.id) }
                )
            }
        }

        // Section: Milestones (shown when milestones exist)
        if (uiState.milestones.isNotEmpty()) {
            item {
                RumboSectionHeader(title = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_milestones_title))
            }

            items(uiState.milestones, key = { it.id }) { milestone ->
                RumboCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = milestone.isCompleted,
                                onCheckedChange = { onEvent(ProcessDetailUiEvent.ToggleMilestoneStatus(milestone)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Section: Pending Tasks
        item {
            RumboSectionHeader(title = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_pending_tasks_title))
        }

        if (uiState.pendingTasks.isNotEmpty()) {
            item {
                PendingTasksSortRow(
                    taskSortOrder = uiState.taskSortOrder,
                    onSortOrderSelected = { order -> onEvent(ProcessDetailUiEvent.ChangeTaskSortOrder(order)) }
                )
            }
        }

        if (uiState.pendingTasks.isEmpty()) {
            item {
                Text(
                    text = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_pending_tasks_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            items(uiState.pendingTasks, key = { it.id }) { taskItem ->
                RumboTaskItem(
                    task = taskItem,
                    processColorOrVisualId = process.colorOrVisualId,
                    processName = process.name,
                    onToggleStatus = { task ->
                        if (!task.isCompleted) {
                            taskToCompleteWithDuration = task
                        } else {
                            onEvent(ProcessDetailUiEvent.ToggleTaskStatus(task))
                        }
                    },
                    onClick = { onNavigateToTask(taskItem.id) },
                    onStartSession = { task -> onNavigateToStartSession(task.id) }
                )
            }
        }

        // Section: Completed Tasks (Preserved & Visible)
        if (uiState.completedTasks.isNotEmpty()) {
            item {
                RumboSectionHeader(title = stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_completed_tasks_title))
            }

            items(uiState.completedTasks, key = { it.id }) { taskItem ->
                RumboTaskItem(
                    task = taskItem,
                    processColorOrVisualId = process.colorOrVisualId,
                    processName = process.name,
                    onToggleStatus = { onEvent(ProcessDetailUiEvent.ToggleTaskStatus(it)) },
                    onClick = { onNavigateToTask(taskItem.id) }
                )
            }
        }
    }

    if (uiState.userMessage != null) {
        AlertDialog(
            onDismissRequest = { onEvent(ProcessDetailUiEvent.DismissUserMessage) },
            title = { Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_cannot_finish_title)) },
            text = { Text(uiState.userMessage) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProcessDetailUiEvent.DismissUserMessage) }) {
                    Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.process_detail_understood))
                }
            }
        )
    }

    if (taskToCompleteWithDuration != null) {
        val task = taskToCompleteWithDuration!!
        val sessionMinutes = if (task.timeWorkedMillis > 0L) {
            (task.timeWorkedMillis + 59_999L) / 60_000L
        } else 0L
        val minMinutes = if (sessionMinutes > 0L) sessionMinutes else 1L
        TaskCompletionDurationDialog(
            taskTitle = task.title,
            initialMinutes = if (sessionMinutes > 0L) sessionMinutes else 0L,
            minMinutes = minMinutes,
            onConfirm = { minutes ->
                celebratingTaskTitle = task.title
                onEvent(ProcessDetailUiEvent.CompleteTaskWithDuration(task, minutes))
                taskToCompleteWithDuration = null
            },
            onDismiss = { taskToCompleteWithDuration = null }
        )
    }

    if (showProcessCelebration) {
        val timeHours = uiState.totalTimeInvestedMillis / (1000 * 60 * 60)
        val timeMinutes = (uiState.totalTimeInvestedMillis / (1000 * 60)) % 60
        val timeStr = if (timeHours > 0) "${timeHours}h ${timeMinutes}m" else "${timeMinutes}m"
        val costStr = "S/ ${"%.2f".format(java.util.Locale.US, process.accumulatedDirectCost)}"

        CelebrationCinematicDialog(
            processName = process.name,
            totalTimeFormatted = timeStr,
            totalCostFormatted = costStr,
            completedTasksCount = uiState.completedTasks.size,
            onDismiss = { showProcessCelebration = false }
        )
    }

    if (celebratingTaskTitle != null) {
        TaskCompletionCelebration(
            taskTitle = celebratingTaskTitle!!,
            onDismiss = { celebratingTaskTitle = null }
        )
    }
}
}

/**
 * Horizontal filter chip row allowing the user to sort pending tasks inside a process.
 *
 * @param taskSortOrder Current [TaskSortOrder] applied.
 * @param onSortOrderSelected Callback invoked when a sort order chip is clicked.
 * @param modifier Optional [Modifier] for layout adjustments.
 */
@Composable
private fun PendingTasksSortRow(
    taskSortOrder: TaskSortOrder,
    onSortOrderSelected: (TaskSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        FilterChip(
            selected = taskSortOrder == TaskSortOrder.DUE_DATE,
            onClick = { onSortOrderSelected(TaskSortOrder.DUE_DATE) },
            label = { Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.filter_due_date)) }
        )
        FilterChip(
            selected = taskSortOrder == TaskSortOrder.RECENT,
            onClick = { onSortOrderSelected(TaskSortOrder.RECENT) },
            label = { Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.filter_recent)) }
        )
        FilterChip(
            selected = taskSortOrder == TaskSortOrder.PRIORITY,
            onClick = { onSortOrderSelected(TaskSortOrder.PRIORITY) },
            label = { Text(stringResource(tech.nikelyh.rumbo.core.designsystem.R.string.filter_priority)) }
        )
    }
}

@Preview(name = "Process Detail Light", showBackground = true)
@Composable
private fun ProcessDetailScreenPreviewLight() {
    RumboTheme(darkTheme = false) {
        ProcessDetailScreen(
            uiState = ProcessDetailUiState.Content(
                process = Process(
                    id = "p1",
                    name = "Desarrollo de Rumbo",
                    description = "Implementar la capa de persistencia y features principales.",
                    status = ProcessStatus.ACTIVE,
                    createdAtEpochMillis = 1000L,
                    colorOrVisualId = "teal",
                    accumulatedDirectCost = 250.0
                ),
                pendingTasks = listOf(
                    Task(
                        id = "t1",
                        processId = "p1",
                        title = "Implementar ProcessDetailScreen",
                        status = TaskStatus.PENDING,
                        priority = Priority.HIGH,
                        createdAtEpochMillis = 1000L
                    )
                ),
                completedTasks = emptyList(),
                milestones = emptyList(),
                workSessions = emptyList(),
                totalTimeInvestedMillis = 3600000L * 3,
                progressEntries = emptyList()
            ),
            onEvent = {},
            onNavigateToEditProcess = {},
            onNavigateToCreateTask = {},
            onNavigateToStartSession = {}
        )
    }
}
