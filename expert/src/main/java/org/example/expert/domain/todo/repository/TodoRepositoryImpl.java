package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponseDto;

import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public Optional<Todo> findByIdWithUserDSL(Long todoId) {
        QTodo todo = QTodo.todo;
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
    @Override
    public Page<TodoSearchResponseDto> findByDynamicQuery(int page, int size, String title, String startDate, String endDate, String nickName) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;
        QComment comment = QComment.comment;
        QManager manager = QManager.manager;

        BooleanBuilder builder = new BooleanBuilder();

        // 제목 검색
        if (title != null && !title.isEmpty()) {
            builder.and(todo.title.containsIgnoreCase(title));
        }

        // 생성일 범위 검색
        if (startDate != null && !startDate.isEmpty()) {
            builder.and(todo.createdAt.goe(LocalDate.parse(startDate).atStartOfDay()));
        }
        if (endDate != null && !endDate.isEmpty()) {
            builder.and(todo.createdAt.loe(LocalDate.parse(endDate).atTime(LocalTime.MAX)));
        }

        // 담당자 닉네임 검색
        if (nickName != null && !nickName.isEmpty()) {
            builder.and(manager.user.nickName.containsIgnoreCase(nickName));
        }

        // 페이징 정보
        Pageable pageable = PageRequest.of(page, size);

        // 본 쿼리 (데이터 조회)
        List<TodoSearchResponseDto> content = queryFactory
                .select(Projections.constructor(
                        TodoSearchResponseDto.class,
                        todo.title,
                        manager.countDistinct().as("managerCount"),
                        comment.id.countDistinct().as("commentCount")
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(builder)
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리 (성능 고려: group by, join 생략)
        Long totalCount = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(builder)
                .fetchOne();
        /**
         *             .where(
         *                 titleContains(title),
         *                 createdAfter(startDate),
         *                 createdBefore(endDate),
         *                 nickNameContains(nickName)
         *             )
         */

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression titleContains(String title) {
        return (title != null && !title.isEmpty()) ? QTodo.todo.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression createdAfter(String startDate) {
        if (startDate == null || startDate.isEmpty()) return null;
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        return QTodo.todo.createdAt.goe(start);
    }

    private BooleanExpression createdBefore(String endDate) {
        if (endDate == null || endDate.isEmpty()) return null;
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        return QTodo.todo.createdAt.loe(end);
    }

    private BooleanExpression nickNameContains(String nickName) {
        return (nickName != null && !nickName.isEmpty()) ? QManager.manager.user.nickName.containsIgnoreCase(nickName) : null;
    }

}
