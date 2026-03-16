/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import axios from 'axios';

const API_BASE_URL = '/api/auth';

export interface User {
  id: number;
  username: string;
  nickname?: string;
  avatar?: string;
  role?: string;
  token?: string;
}

export interface UpdateUserDTO {
  nickname?: string;
  avatar?: string;
}

export interface ChangePasswordDTO {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface LoginDTO {
  username: string;
  password: string;
}

export interface RegisterDTO {
  username: string;
  password: string;
  nickname?: string;
  avatar?: string;
}

export default {
  async login(data: LoginDTO): Promise<User> {
    const response = await axios.post(`${API_BASE_URL}/login`, data);
    return response.data.data;
  },

  async register(data: RegisterDTO): Promise<User> {
    const response = await axios.post(`${API_BASE_URL}/register`, data);
    return response.data.data;
  },

  async getUserInfo(): Promise<User> {
    const response = await axios.get(`${API_BASE_URL}/me`);
    return response.data.data;
  },

  async updateProfile(data: UpdateUserDTO): Promise<User> {
    const response = await axios.put(`${API_BASE_URL}/profile`, data);
    return response.data.data;
  },

  async changePassword(data: ChangePasswordDTO): Promise<void> {
    await axios.post(`${API_BASE_URL}/change-password`, data);
  },
};
