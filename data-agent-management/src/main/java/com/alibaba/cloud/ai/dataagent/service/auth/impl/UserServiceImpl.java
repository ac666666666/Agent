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
package com.alibaba.cloud.ai.dataagent.service.auth.impl;

import com.alibaba.cloud.ai.dataagent.dto.auth.ChangePasswordDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.LoginDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.RegisterDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.UpdateUserDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.UserVO;
import com.alibaba.cloud.ai.dataagent.entity.SysUser;
import com.alibaba.cloud.ai.dataagent.exception.InvalidInputException;
import com.alibaba.cloud.ai.dataagent.mapper.SysUserMapper;
import com.alibaba.cloud.ai.dataagent.service.auth.UserService;
import com.alibaba.cloud.ai.dataagent.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

	private final JwtUtil jwtUtil;

	@Override
	public UserVO login(LoginDTO loginDTO) {
		if (!StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
			throw new InvalidInputException("Username and password are required");
		}
		SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, loginDTO.getUsername()));
		if (user == null || !user.getPassword().equals(loginDTO.getPassword())) {
			throw new InvalidInputException("Invalid username or password");
		}
		String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
		return toVO(user, token);
	}

	@Override
	public UserVO register(RegisterDTO registerDTO) {
		if (!StringUtils.hasText(registerDTO.getUsername()) || !StringUtils.hasText(registerDTO.getPassword())) {
			throw new InvalidInputException("Username and password are required");
		}
		long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, registerDTO.getUsername()));
		if (count > 0) {
			throw new InvalidInputException("Username already exists");
		}
		SysUser user = SysUser.builder()
			.username(registerDTO.getUsername())
			.password(registerDTO.getPassword())
			.nickname(registerDTO.getNickname())
			.avatar(registerDTO.getAvatar())
			.role("user")
			.createTime(LocalDateTime.now())
			.updateTime(LocalDateTime.now())
			.build();
		this.save(user);
		String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
		return toVO(user, token);
	}

	@Override
	public UserVO getUserInfo(Long userId) {
		SysUser user = this.getById(userId);
		if (user == null) {
			throw new InvalidInputException("User not found");
		}
		return toVO(user, null);
	}

	@Override
	public UserVO updateUserInfo(Long userId, UpdateUserDTO dto) {
		SysUser user = this.getById(userId);
		if (user == null) {
			throw new InvalidInputException("User not found");
		}
		if (StringUtils.hasText(dto.getNickname())) {
			user.setNickname(dto.getNickname());
		}
		if (StringUtils.hasText(dto.getAvatar())) {
			user.setAvatar(dto.getAvatar());
		}
		user.setUpdateTime(LocalDateTime.now());
		this.updateById(user);
		return toVO(user, null);
	}

	@Override
	public void changePassword(Long userId, ChangePasswordDTO dto) {
		SysUser user = this.getById(userId);
		if (user == null) {
			throw new InvalidInputException("User not found");
		}
		if (!user.getPassword().equals(dto.getCurrentPassword())) {
			throw new InvalidInputException("Current password is incorrect");
		}
		if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
			throw new InvalidInputException("New password and confirmation do not match");
		}
		user.setPassword(dto.getNewPassword());
		user.setUpdateTime(LocalDateTime.now());
		this.updateById(user);
	}

	private UserVO toVO(SysUser user, String token) {
		return UserVO.builder()
			.id(user.getId())
			.username(user.getUsername())
			.nickname(user.getNickname())
			.avatar(user.getAvatar())
			.role(user.getRole())
			.token(token)
			.build();
	}

}
