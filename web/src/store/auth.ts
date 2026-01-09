"use client";

import { create } from "zustand";
import { apiFetch, USER_SERVICE_URL } from "@/lib/api";
import { User } from "@/types";

type AuthState = {
  token: string | null;
  user: User | null;
  loading: boolean;
  error: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  load: () => Promise<void>;
  updateProfile: (user: Partial<User>) => Promise<void>;
  updatePassword: (passwordData: any) => Promise<void>;
};

export const useAuthStore = create<AuthState>((set, get) => ({
  token: typeof window !== "undefined" ? localStorage.getItem("token") : null,
  user: null,
  loading: false,
  error: null,
  login: async (username, password) => {
    set({ loading: true, error: null });
    try {
      const data = await apiFetch("/api/users/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      }, undefined, USER_SERVICE_URL);
      // 获取登录token
      const token = (data as any)?.token || (data as any)?.data?.token || (data as any)?.accessToken || (data as any)?.satoken;
      if (!token) throw new Error("Missing token in response");
      
      localStorage.setItem("token", token);
      const userData = (data as any)?.user || (data as any)?.data?.user;
      if (userData) {
        set({ token, user: userData as User, loading: false });
      } else {
        set({ token, loading: false });
      }
    } catch (e: any) {
      console.error("Login error:", e);
      set({ loading: false, error: e.message || "Login failed" });
      throw e; // Re-throw so the component can handle it if needed (though component currently swallows it)
    }
  },
  logout: () => {
    localStorage.removeItem("token");
    set({ token: null, user: null });
  },
  load: async () => {
    const { token, loading } = get();
    if (!token || loading) return;

    set({ loading: true, error: null });
    try {
      const data = await apiFetch("/api/users/profile", {}, token, USER_SERVICE_URL);
      const user = (data as any)?.data || data;
      set({ user: user as User, loading: false });
    } catch (e: any) {
              console.error("Auth load error:", e);
              set({ loading: false, error: e.message });
              // Handle 401 or specific backend error for invalid token
              if (e.message.includes("401") || e.message.includes("token") || e.message.includes("无效")) {
                localStorage.removeItem("token");
                set({ token: null, user: null });
              }
            }
  },
  updateProfile: async (userData) => {
    const { token } = get();
    if (!token) return;
    set({ loading: true, error: null });
    try {
      const data = await apiFetch("/api/users/profile", {
        method: "PUT",
        body: JSON.stringify(userData),
      }, token, USER_SERVICE_URL);
      const updatedUser = (data as any)?.data || data;
      set({ user: updatedUser as User, loading: false });
    } catch (e: any) {
      console.error("Update profile error:", e);
      set({ loading: false, error: e.message });
      throw e;
    }
  },
  updatePassword: async (passwordData) => {
    const { token } = get();
    if (!token) return;
    set({ loading: true, error: null });
    try {
      await apiFetch("/api/users/password", {
        method: "PUT",
        body: JSON.stringify(passwordData),
      }, token, USER_SERVICE_URL);
      set({ loading: false });
    } catch (e: any) {
      console.error("Update password error:", e);
      set({ loading: false, error: e.message });
      throw e;
    }
  },
}));
