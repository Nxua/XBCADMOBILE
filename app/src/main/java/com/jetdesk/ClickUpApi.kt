package com.jetdesk

import retrofit2.Call
import retrofit2.http.*

interface ClickUpApi {
    // Existing endpoint to get spaces
    @GET("team/{team_id}/space")
    fun getSpaces(
        @Header("Authorization") authToken: String,
        @Path("team_id") teamId: String
    ): Call<ClickUpResponse>

    // Endpoint to create a new space
    @POST("team/{team_id}/space")
    fun createSpace(
        @Header("Authorization") authToken: String,
        @Path("team_id") teamId: String,
        @Body newSpace: SpaceRequest
    ): Call<SpaceResponse>

    // Endpoint to create a new folder within a space
    @POST("space/{space_id}/folder")
    fun createFolder(
        @Header("Authorization") authToken: String,
        @Path("space_id") spaceId: String,
        @Body newFolder: FolderRequest
    ): Call<FolderResponse>

    // Endpoint to get folders within a space
    @GET("space/{space_id}/folder")
    fun getFolders(
        @Header("Authorization") authToken: String,
        @Path("space_id") spaceId: String
    ): Call<FoldersResponse>

    // Endpoint to create a new list within a folder
    @POST("folder/{folder_id}/list")
    fun createList(
        @Header("Authorization") authToken: String,
        @Path("folder_id") folderId: String,
        @Body newList: ListRequest
    ): Call<ListResponse>

    // Endpoint to get lists within a folder
    @GET("folder/{folder_id}/list")
    fun getLists(
        @Header("Authorization") authToken: String,
        @Path("folder_id") folderId: String
    ): Call<ListsResponse>

    // Endpoint to create a new task within a list
    @POST("list/{list_id}/task")
    fun createTask(
        @Header("Authorization") authToken: String,
        @Path("list_id") listId: String,
        @Body newTask: TaskRequest
    ): Call<TaskResponse>

    // Endpoint to get tasks within a list
    @GET("list/{list_id}/task")
    fun getTasks(
        @Header("Authorization") authToken: String,
        @Path("list_id") listId: String
    ): Call<TasksResponse>

    @DELETE("task/{task_id}")
    fun deleteTask(
        @Header("Authorization") authToken: String,
        @Path("task_id") taskId: String
    ): Call<Void>

}

// Data classes for request and response payloads

data class ClickUpResponse(
    val spaces: List<Space>
)

data class SpaceRequest(
    val name: String,
    val multiple_assignees: Boolean = true
)

data class SpaceResponse(
    val id: String,
    val name: String
)

data class FolderRequest(
    val name: String
)

data class FolderResponse(
    val id: String,
    val name: String
)

data class FoldersResponse(
    val folders: List<Folder>
)

data class ListRequest(
    val name: String,
    val due_date: Long? = null
)

data class ListResponse(
    val id: String,
    val name: String
)

data class ListsResponse(
    val lists: List<ClickUpList>
)

data class TaskRequest(
    val name: String,
    val description: String? = null,
    val assignees: List<String>? = null,
    val status: String = "to do",
    val start_date: Long? = null,
    val due_date: Long? = null,
    val priority: Int? = null
)

data class TaskResponse(
    val id: String,
    val name: String,
    val status: Status
)

data class TasksResponse(
    val tasks: List<Task>
)

data class Folder(
    val id: String,
    val name: String
)

data class ClickUpList(
    val id: String,
    val name: String
)

data class Task(
    val id: String,
    val name: String,
    val description: String? = null,
    val priority: Priority? = null,  // Update priority to be of type Priority object
    val status: Status
)

data class Priority(
    val id: Int?,  // Priority ID (could be 1, 2, 3, etc.)
    val color: String?,  // Color of the priority (e.g., "#ff0000")
    val orderindex: Int?,  // The order of priority
    val priority: String?  // The textual representation (e.g., "urgent")
)

data class Status(
    val status: String
)
