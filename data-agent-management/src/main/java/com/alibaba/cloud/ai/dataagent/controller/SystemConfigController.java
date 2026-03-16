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

import com.alibaba.cloud.ai.dataagent.service.system.SystemConfigService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system-config")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping("/all")
    public ApiResponse<Map<String, String>> getAllConfigs() {
        return ApiResponse.success("Success", systemConfigService.getAllConfigs());
    }

    @GetMapping("/{key}")
    public ApiResponse<String> getConfig(@PathVariable String key) {
        return ApiResponse.success("Success", systemConfigService.getConfigValue(key));
    }

    @PostMapping("/update")
    public ApiResponse<Void> updateConfig(@RequestBody Map<String, String> configs) {
        systemConfigService.batchSetConfigs(configs);
        return ApiResponse.success("Configuration updated successfully");
    }
}
