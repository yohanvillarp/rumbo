package tech.nikelyh.rumbo.core.navigation

object OnboardingDestination : RumboNavigationDestination {
    override val route = "onboarding_route"
    override val destination = "onboarding_destination"
}

// Primary Destinations (4)
object HomeDestination : RumboNavigationDestination {
    override val route = "home_route"
    override val destination = "home_destination"
}

object ProcessesDestination : RumboNavigationDestination {
    override val route = "processes_route"
    override val destination = "processes_destination"
}

object TasksDestination : RumboNavigationDestination {
    override val route = "tasks_route"
    override val destination = "tasks_destination"
}

object ProgressDestination : RumboNavigationDestination {
    override val route = "progress_route"
    override val destination = "progress_destination"
}

// Secondary Destination
object SettingsDestination : RumboNavigationDestination {
    override val route = "settings_route"
    override val destination = "settings_destination"
}

// Action & Detail Destinations
object ProcessDetailDestination : RumboNavigationDestination {
    override val route = "process_detail/{processId}"
    override val destination = "process_detail_destination"
    fun createRoute(processId: String) = "process_detail/$processId"
}

object TaskDetailDestination : RumboNavigationDestination {
    override val route = "task_detail/{taskId}"
    override val destination = "task_detail_destination"
    fun createRoute(taskId: String) = "task_detail/$taskId"
}

object CreateProcessDestination : RumboNavigationDestination {
    override val route = "create_process"
    override val destination = "create_process_destination"
}

object EditProcessDestination : RumboNavigationDestination {
    override val route = "edit_process/{processId}"
    override val destination = "edit_process_destination"
    fun createRoute(processId: String) = "edit_process/$processId"
}

object CreateTaskDestination : RumboNavigationDestination {
    override val route = "create_task?processId={processId}"
    override val destination = "create_task_destination"
    fun createRoute(processId: String? = null): String {
        return if (processId != null) "create_task?processId=$processId" else "create_task"
    }
}

object LogProgressDestination : RumboNavigationDestination {
    override val route = "log_progress/{processId}"
    override val destination = "log_progress_destination"
    fun createRoute(processId: String) = "log_progress/$processId"
}

object StartSessionDestination : RumboNavigationDestination {
    override val route = "start_session?processId={processId}&taskId={taskId}"
    override val destination = "start_session_destination"
    fun createRoute(processId: String? = null, taskId: String? = null): String {
        val params = mutableListOf<String>()
        if (processId != null) params.add("processId=$processId")
        if (taskId != null) params.add("taskId=$taskId")
        return if (params.isNotEmpty()) "start_session?${params.joinToString("&")}" else "start_session"
    }
}
