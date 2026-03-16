import { defineStore } from 'pinia';
import AuthService, { User, LoginDTO, RegisterDTO } from '@/services/auth';

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null as User | null,
    isAuthenticated: false,
  }),
  
  getters: {
    username: (state) => state.currentUser?.nickname || state.currentUser?.username || 'Guest',
    userRole: (state) => state.currentUser?.role || 'Visitor',
    userAvatar: (state) => state.currentUser?.avatar || '',
  },

  actions: {
    async login(data: LoginDTO) {
      try {
        const user = await AuthService.login(data);
        this.setUser(user);
        return true;
      } catch (error) {
        console.error('Login failed:', error);
        return false;
      }
    },

    async register(data: RegisterDTO) {
      try {
        const user = await AuthService.register(data);
        this.setUser(user);
        return true;
      } catch (error) {
        console.error('Registration failed:', error);
        return false;
      }
    },

    setUser(user: User) {
      this.currentUser = user;
      this.isAuthenticated = true;
      // Persist to localStorage if needed
      if (user.token) {
        localStorage.setItem('auth_token', user.token);
        localStorage.setItem('user_id', user.id.toString());
      }
    },

    logout() {
      this.currentUser = null;
      this.isAuthenticated = false;
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_id');
    },

    async checkAuth() {
      const token = localStorage.getItem('auth_token');
      if (token) {
        try {
          const user = await AuthService.getUserInfo();
          this.currentUser = user;
          this.isAuthenticated = true;
        } catch (error) {
          this.logout();
        }
      }
    }
  }
});
