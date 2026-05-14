package com.review.agent.domain.dto.convert;

import com.review.agent.domain.dto.UserVO;
import com.review.agent.domain.entity.UserAccount;

public final class UserConverter {

    private UserConverter() {
    }

    public static UserVO toVO(UserAccount user) {
        if (user == null) {
            return null;
        }

        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
