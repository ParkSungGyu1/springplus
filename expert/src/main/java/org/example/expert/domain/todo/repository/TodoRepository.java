package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);
    // 전체 조회 (기본 정렬)
    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> fetchAll(Pageable pageable);

    // weather 조건만 있는 경우
    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u WHERE t.weather = :weather ORDER BY t.modifiedAt DESC")
    Page<Todo> fetchByWeather(Pageable pageable, @Param("weather") String weather);

    // 기간 조건만 있는 경우
    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u WHERE t.modifiedAt BETWEEN :start AND :end ORDER BY t.modifiedAt DESC")
    Page<Todo> fetchByPeriod(Pageable pageable, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // weather + 기간 조건이 모두 있는 경우
    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u WHERE t.weather = :weather AND t.modifiedAt BETWEEN :start AND :end ORDER BY t.modifiedAt DESC")
    Page<Todo> fetchByWeatherAndPeriod(Pageable pageable, @Param("weather") String weather, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
