package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final String nickName; //nickName 추가로 인함
    private final UserRole userRole;

    public AuthUser(Long id, String email,String nickName, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.nickName = nickName; //nickName 추가로 인함
        this.userRole = userRole;
    }
}
