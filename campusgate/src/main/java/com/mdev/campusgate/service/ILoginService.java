package com.mdev.campusgate.service;

import com.mdev.campusgate.dto.request.LoginRequestDTO;
import com.mdev.campusgate.dto.response.LoginResponseDTO;

public interface ILoginService {
    LoginResponseDTO userLogin(LoginRequestDTO dto);
}