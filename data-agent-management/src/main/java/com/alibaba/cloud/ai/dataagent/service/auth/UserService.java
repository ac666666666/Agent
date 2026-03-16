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
package com.alibaba.cloud.ai.dataagent.service.auth;

import com.alibaba.cloud.ai.dataagent.dto.auth.LoginDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.RegisterDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.UpdateUserDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.ChangePasswordDTO;
import com.alibaba.cloud.ai.dataagent.dto.auth.UserVO;
import com.alibaba.cloud.ai.dataagent.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<SysUser> {
    UserVO login(LoginDTO loginDTO);
    UserVO register(RegisterDTO registerDTO);
    UserVO getUserInfo(Long userId);
    UserVO updateUserInfo(Long userId, UpdateUserDTO updateUserDTO);
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}
