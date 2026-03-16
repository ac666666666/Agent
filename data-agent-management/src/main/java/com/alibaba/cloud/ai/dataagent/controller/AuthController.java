/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.controller;

import com.alibaba.cloud.ai.dataagent.dto.auth.LoginDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.RegisterDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.UpdateUserDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.ChangePasswordDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.UserVO;
import com.alibaba.cloud.ai.dataagent.service.auth.UserService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<UserVO> login(@RequestBody LoginDTO loginDTO) {
        return ApiResponse.success("Login successful", userService.login(loginDTO));
    }

    @PostMapping("/register")
    public ApiResponse<UserVO> register(@RequestBody RegisterDTO registerDTO) {
        return ApiResponse.success("Registration successful", userService.register(registerDTO));
    }

    @GetMapping("/me")
    public ApiResponse<UserVO> getUserInfo(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        return ApiResponse.success("Success", userService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@RequestAttribute(value = "userId", required = false) Long userId,
                                           @RequestBody UpdateUserDTO updateUserDTO) {
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        UserVO updatedUser = userService.updateUserInfo(userId, updateUserDTO);
        return ApiResponse.success("Profile updated successfully", updatedUser);
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestAttribute(value = "userId", required = false) Long userId,
                                            @RequestBody ChangePasswordDTO changePasswordDTO) {
        if (userId == null) {
            return ApiResponse.error("User not authenticated");
        }
        userService.changePassword(userId, changePasswordDTO);
        return ApiResponse.success("Password changed successfully");
    }
}
