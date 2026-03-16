import axios from 'axios';

const API_BASE_URL = '/api/system-config';

export interface SystemConfig {
  [key: string]: string;
}

class SystemConfigService {
  /**
   * 获取所有系统配置
   */
  async getAllConfigs(): Promise<SystemConfig> {
    try {
      const response = await axios.get<{ success: boolean; data: SystemConfig }>(
        `${API_BASE_URL}/all`
      );
      return response.data.data || {};
    } catch (error) {
      console.error('Failed to get system configs:', error);
      return {};
    }
  }

  /**
   * 更新系统配置
   */
  async updateConfigs(configs: SystemConfig): Promise<boolean> {
    try {
      const response = await axios.post<{ success: boolean }>(
        `${API_BASE_URL}/update`,
        configs
      );
      return response.data.success;
    } catch (error) {
      console.error('Failed to update system configs:', error);
      return false;
    }
  }
}

export default new SystemConfigService();
