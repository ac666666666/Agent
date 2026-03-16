import { defineStore } from 'pinia';
import SystemConfigService from '@/services/systemConfig';

export const useSystemStore = defineStore('system', {
  state: () => ({
    systemName: 'Data Agent',
    description: '企业级数据智能体平台',
    language: 'zh-CN',
    theme: 'light',
  }),
  actions: {
    async loadSettings() {
      const configs = await SystemConfigService.getAllConfigs();

      if (configs['system.name']) this.systemName = configs['system.name'];
      if (configs['system.description']) this.description = configs['system.description'];
      if (configs['system.language']) this.language = configs['system.language'];

      if (configs['appearance.theme']) this.theme = configs['appearance.theme'];

      // Apply settings
      document.title = this.systemName;
      this.applyTheme(this.theme);

      // Listen for system theme changes
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
        if (this.theme !== 'dark' && this.theme !== 'light') {
          const html = document.documentElement;
          if (e.matches) {
            html.classList.add('dark');
          } else {
            html.classList.remove('dark');
          }
        }
      });
    },

    updateSystemSettings(name: string, desc: string, lang: string) {
      this.systemName = name;
      this.description = desc;
      this.language = lang;
      document.title = name;
    },

    updateTheme(theme: string) {
      this.theme = theme;
      this.applyTheme(theme);
    },

    applyTheme(theme: string) {
      // Basic theme implementation - can be expanded
      const html = document.documentElement;
      if (theme === 'dark') {
        html.classList.add('dark');
      } else if (theme === 'light') {
        html.classList.remove('dark');
      } else {
        // Auto - follow system
        if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
          html.classList.add('dark');
        } else {
          html.classList.remove('dark');
        }
      }
    },
  },
});
