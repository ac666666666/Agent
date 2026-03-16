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
package com.alibaba.cloud.ai.dataagent.service.system.impl;

import com.alibaba.cloud.ai.dataagent.entity.SystemConfig;
import com.alibaba.cloud.ai.dataagent.mapper.SystemConfigMapper;
import com.alibaba.cloud.ai.dataagent.service.system.SystemConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public String getConfigValue(String key) {
        SystemConfig config = systemConfigMapper.selectOne(new QueryWrapper<SystemConfig>().eq("config_key", key));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    @Transactional
    public void setConfig(String key, String value, String description) {
        SystemConfig existing = systemConfigMapper.selectOne(new QueryWrapper<SystemConfig>().eq("config_key", key));
        if (existing != null) {
            existing.setConfigValue(value);
            if (description != null) {
                existing.setDescription(description);
            }
            existing.setUpdateTime(LocalDateTime.now());
            systemConfigMapper.updateById(existing);
        } else {
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(key);
            newConfig.setConfigValue(value);
            newConfig.setDescription(description);
            newConfig.setCreateTime(LocalDateTime.now());
            newConfig.setUpdateTime(LocalDateTime.now());
            systemConfigMapper.insert(newConfig);
        }
    }

    @Override
    @Transactional
    public void batchSetConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            setConfig(entry.getKey(), entry.getValue(), null);
        }
    }
}
