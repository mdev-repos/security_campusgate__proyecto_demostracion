package com.mdev.campusgate.service;

public interface ILoginAttemptService {
    void recordFailure(String email);
    void recordSuccess(String email);
}