export interface Permission {
  id: number;
  name: string;
  key: string; // unique key e.g., "user:create"
  description?: string;
  resourcePath?: string;
  method?: string;
  status?: number; // 1: active, 0: inactive
}

export interface Role {
  id: number;
  name: string;
  key: string; // unique key e.g., "admin"
  description?: string;
  status?: number;
  permissions?: Permission[];
}

export interface User {
  id: number;
  username: string;
  name?: string;
  realName?: string;
  email: string;
  phone?: string;
  status?: number; // 1: active, 0: disabled
  type?: number; // 0: ordinary user, 1: admin
  roles?: Role[];
}
