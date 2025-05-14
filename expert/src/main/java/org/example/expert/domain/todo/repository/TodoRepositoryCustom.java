package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoSearchResponseDto;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface TodoRepositoryCustom {
    Optional<Todo> findByIdWithUserDSL(Long todoId);
    Page<TodoSearchResponseDto> findByDynamicQuery(int page, int size, String title, String startDate, String endDate, String nickName);
}
