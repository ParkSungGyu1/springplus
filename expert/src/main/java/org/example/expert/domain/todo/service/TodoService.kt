package org.example.expert.domain.todo.service

import org.example.expert.client.WeatherClient
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.common.exception.InvalidRequestException
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.dto.response.TodoResponse
import org.example.expert.domain.todo.dto.response.TodoSaveResponse
import org.example.expert.domain.todo.dto.response.TodoSearchResponseDto
import org.example.expert.domain.todo.entity.Todo

import org.example.expert.domain.todo.repository.TodoRepository
import org.example.expert.domain.user.dto.response.UserResponse
import org.example.expert.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TodoService(
    private val todoRepository: TodoRepository,
    private val weatherClient: WeatherClient
) {

    @Transactional
    fun saveTodo(authUser: AuthUser, todoSaveRequest: TodoSaveRequest): TodoSaveResponse {
        val user = User.fromAuthUser(authUser)
        val weather = weatherClient.todayWeather

        val newTodo = Todo(
            todoSaveRequest.title,
            todoSaveRequest.contents,
            weather,
            user
        )

        val savedTodo = todoRepository.save(newTodo)

        return TodoSaveResponse(
            savedTodo.id,
            savedTodo.title,
            savedTodo.contents,
            weather,
            UserResponse(user.id, user.email)
        )
    }

    fun getTodos(
        page: Int,
        size: Int,
        weather: String?,
        start: LocalDateTime?,
        end: LocalDateTime?
    ): Page<TodoResponse> {
        val pageable: Pageable = PageRequest.of(page - 1, size)

        val todos: Page<Todo> = when {
            !weather.isNullOrEmpty() && start != null && end != null ->
                todoRepository.fetchByWeatherAndPeriod(pageable, weather, start, end)

            !weather.isNullOrEmpty() ->
                todoRepository.fetchByWeather(pageable, weather)

            start != null && end != null ->
                todoRepository.fetchByPeriod(pageable, start, end)

            else ->
                todoRepository.fetchAll(pageable)
        }

        return todos.map { todo ->
            TodoResponse(
                todo.id,
                todo.title,
                todo.contents,
                todo.weather,
                UserResponse(todo.user.id, todo.user.email),
                todo.createdAt,
                todo.modifiedAt
            )
        }
    }

    fun getTodo(todoId: Long): TodoResponse {
        val todo = todoRepository.findByIdWithUserDSL(todoId)
            .orElseThrow { InvalidRequestException("Todo not found") }

        val user = todo.user

        return TodoResponse(
            todo.id,
            todo.title,
            todo.contents,
            todo.weather,
            UserResponse(user.id, user.email),
            todo.createdAt,
            todo.modifiedAt
        )
    }

    fun search(
        title: String?,
        startDate: String?,
        endDate: String?,
        nickName: String?,
        page: Int,
        size: Int
    ): Page<TodoSearchResponseDto> {
        return todoRepository.findByDynamicQuery(page, size, title, startDate, endDate, nickName)
    }
}
