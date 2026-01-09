"use client";

import { create } from "zustand";
import { apiFetch, USER_SERVICE_URL } from "@/lib/api";
import { User, Role, Permission } from "@/types";
import { useAuthStore } from "./auth";

interface AdminState {
  users: User[];
  roles: Role[];
  permissions: Permission[];
  loading: boolean;
  error: string | null;
  totalUsers: number;
  
  // User actions
  fetchUsers: (page?: number, pageSize?: number, params?: any) => Promise<void>;
  createUser: (user: Partial<User>) => Promise<void>;
  getUserById: (id: number) => Promise<User | null>;
  updateUserAdmin: (user: Partial<User> & { id: number }) => Promise<void>;
  updateUser: (id: number, user: Partial<User>) => Promise<void>;
  deleteUser: (id: number) => Promise<void>;
  toggleUserStatus: (id: number, status: number) => Promise<void>;
  assignRoles: (userId: number, roleIds: number[]) => Promise<void>;
  
  // Role actions
  fetchRoles: () => Promise<void>;
  createRole: (role: Partial<Role>) => Promise<void>;
  updateRole: (id: number, role: Partial<Role>) => Promise<void>;
  deleteRole: (id: number) => Promise<void>;
  toggleRoleStatus: (id: number, status: number) => Promise<void>;
  assignPermissions: (roleId: number, permissionIds: number[]) => Promise<void>;
  
  // Permission actions
  fetchPermissions: () => Promise<void>;
  createPermission: (permission: Partial<Permission>) => Promise<void>;
  updatePermission: (id: number, permission: Partial<Permission>) => Promise<void>;
  deletePermission: (id: number) => Promise<void>;
}

export const useAdminStore = create<AdminState>((set, get) => ({
  users: [],
  roles: [],
  permissions: [],
  loading: false,
  error: null,
  totalUsers: 0,

  fetchUsers: async (page = 1, pageSize = 10, params = {}) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      // Construct query string manually for params
      const queryParams = new URLSearchParams({
        page: page.toString(),
        pageSize: pageSize.toString(),
        ...params
      });
      const data = await apiFetch(`/api/users/list?${queryParams.toString()}`, {}, token || undefined, USER_SERVICE_URL);
      console.log("Fetch Users Response:", data); // Debug log
      
      // Handle different possible response structures
      // Case 1: data.data.list (Response wrapper -> Map -> list)
      // Case 2: data.list (Direct Map)
      // Case 3: data.data.records (MyBatis Plus Page)
      // Case 4: data.data (Direct Array)
      // Case 5: data.data.users (Specific to user list)
      const responseData = (data as any)?.data || data;
      let list = [];
      let total = 0;

      if (Array.isArray(responseData)) {
        list = responseData;
        total = responseData.length;
      } else {
        list = responseData?.list || responseData?.records || responseData?.users || [];
        total = responseData?.total || list.length || 0;
      }
      
      set({ users: list, totalUsers: total, loading: false });
    } catch (e: any) {
      console.error("Fetch Users Error:", e);
      set({ loading: false, error: e.message });
    }
  },

  createUser: async (user) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      await apiFetch("/api/users/register", {
        method: "POST",
        body: JSON.stringify(user),
      }, token || undefined, USER_SERVICE_URL);
      // Refresh list
      await get().fetchUsers(); 
      set({ loading: false });
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  getUserById: async (id) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      const data = await apiFetch(`/api/users/${id}`, {}, token || undefined, USER_SERVICE_URL);
      const u = (data as any)?.data || data;
      set({ loading: false });
      return u as User;
    } catch (e: any) {
      set({ loading: false, error: e.message });
      return null;
    }
  },

  updateUserAdmin: async (user) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      // Use standard RESTful endpoint for updating a specific user by ID.
      // Note: The provided backend code only listed /api/users/profile which updates the CURRENT user (Admin).
      // Using /profile would overwrite the Admin's account with the target user's data.
      // Therefore, we use /api/users/{id} assuming the backend will be updated to support this.
      const data = await apiFetch(`/api/users/${user.id}`, {
        method: "PUT",
        body: JSON.stringify(user),
      }, token || undefined, USER_SERVICE_URL);
      const updated = (data as any)?.data || data;
      set((state) => ({
        users: state.users.map((u) => (u.id === user.id ? { ...u, ...updated } : u)),
        loading: false,
      }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  updateUser: async (id, user) => {
    set({ loading: true, error: null });
    // const token = useAuthStore.getState().token;
    try {
      // ...
      console.warn("Update user by ID not explicitly provided in docs");
    } catch (e: any) {
      set({ loading: false, error: e.message });
    }
  },

  deleteUser: async (id) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      const target = get().users.find((u) => u.id === id);
      if (target && target.type === 1) {
        set({ loading: false });
        throw new Error("Cannot delete admin user");
      }
      await apiFetch(`/api/users/${id}`, { method: "DELETE" }, token || undefined, USER_SERVICE_URL);
      set((state) => ({ 
        users: state.users.filter(u => u.id !== id),
        loading: false 
      }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  toggleUserStatus: async (id, status) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      await apiFetch(`/api/users/${id}/status?status=${status}`, { method: "PUT" }, token || undefined, USER_SERVICE_URL);
      set((state) => ({ 
        users: state.users.map(u => u.id === id ? { ...u, status } : u),
        loading: false 
      }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  assignRoles: async (userId, roleIds) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      await apiFetch(`/api/users/${userId}/roles`, {
        method: "POST",
        body: JSON.stringify(roleIds)
      }, token || undefined, USER_SERVICE_URL);
      set({ loading: false });
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  fetchRoles: async () => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      const data = await apiFetch("/api/roles", {}, token || undefined, USER_SERVICE_URL);
      const list = (data as any)?.data || data || [];
      set({ roles: list, loading: false });
    } catch (e: any) {
      set({ loading: false, error: e.message });
    }
  },

  createRole: async (role) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      const data = await apiFetch("/api/roles", {
        method: "POST",
        body: JSON.stringify(role)
      }, token || undefined, USER_SERVICE_URL);
      const newRole = (data as any)?.data || data;
      set((state) => ({ roles: [...state.roles, newRole], loading: false }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  updateRole: async (id, role) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      const data = await apiFetch(`/api/roles/${id}`, {
        method: "PUT",
        body: JSON.stringify(role)
      }, token || undefined, USER_SERVICE_URL);
      const updatedRole = (data as any)?.data || data;
      set((state) => ({ 
        roles: state.roles.map(r => r.id === id ? updatedRole : r),
        loading: false 
      }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  deleteRole: async (id) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      await apiFetch(`/api/roles/${id}`, { method: "DELETE" }, token || undefined, USER_SERVICE_URL);
      set((state) => ({ 
        roles: state.roles.filter(r => r.id !== id),
        loading: false 
      }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  toggleRoleStatus: async (id, status) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      await apiFetch(`/api/roles/${id}/status?status=${status}`, { method: "PUT" }, token || undefined, USER_SERVICE_URL);
      set((state) => ({ 
        roles: state.roles.map(r => r.id === id ? { ...r, status } : r),
        loading: false 
      }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  assignPermissions: async (roleId, permissionIds) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      await apiFetch(`/api/roles/${roleId}/permissions`, {
        method: "POST",
        body: JSON.stringify(permissionIds)
      }, token || undefined, USER_SERVICE_URL);
      set({ loading: false });
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  fetchPermissions: async () => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      const data = await apiFetch("/api/permissions", {}, token || undefined, USER_SERVICE_URL);
      const list = (data as any)?.data || data || [];
      set({ permissions: list, loading: false });
    } catch (e: any) {
      set({ loading: false, error: e.message });
    }
  },

  createPermission: async (permission) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      const data = await apiFetch("/api/permissions", {
        method: "POST",
        body: JSON.stringify(permission)
      }, token || undefined, USER_SERVICE_URL);
      const newPerm = (data as any)?.data || data;
      set((state) => ({ permissions: [...state.permissions, newPerm], loading: false }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  updatePermission: async (id, permission) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      const data = await apiFetch(`/api/permissions/${id}`, {
        method: "PUT",
        body: JSON.stringify(permission)
      }, token || undefined, USER_SERVICE_URL);
      const updatedPerm = (data as any)?.data || data;
      set((state) => ({ 
        permissions: state.permissions.map(p => p.id === id ? updatedPerm : p),
        loading: false 
      }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },

  deletePermission: async (id) => {
    set({ loading: true, error: null });
    const token = useAuthStore.getState().token;
    try {
      await apiFetch(`/api/permissions/${id}`, { method: "DELETE" }, token || undefined, USER_SERVICE_URL);
      set((state) => ({ 
        permissions: state.permissions.filter(p => p.id !== id),
        loading: false 
      }));
    } catch (e: any) {
      set({ loading: false, error: e.message });
      throw e;
    }
  },
}));
