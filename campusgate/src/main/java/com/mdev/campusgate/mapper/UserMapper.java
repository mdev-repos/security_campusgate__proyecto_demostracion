package com.mdev.campusgate.mapper;

import com.mdev.campusgate.dto.response.UserResponseDTO;
import com.mdev.campusgate.model.UserSec;

public class UserMapper {
    public static UserResponseDTO toUserResponseDTO(UserSec user){
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRoles(),
                user.isEnabled(),
                user.isAccountNotLocked()
        );
    }
}