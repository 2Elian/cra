export const USER_SERVICE_URL = "";
export const CONTRACT_SERVICE_URL = "/api/contracts";

export async function apiFetch(path: string, options: RequestInit = {}, token?: string, baseUrl: string = "") {
  // path: 接口路径，如 /login --> options: fetch 的原生配置 --> token: 登录 token（JWT） --> baseUrl: 服务基础地址
  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), 30000); // 30 seconds timeout for uploads

  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  } as Record<string, string>;

  // Automatically remove Content-Type for FormData to allow browser to set boundary
  if (options.body instanceof FormData) {
    delete headers["Content-Type"];
  }

  try {
    const res = await fetch(`${baseUrl}${path}`, { 
      ...options, 
      headers, 
      credentials: "include",
      signal: controller.signal
    });
    clearTimeout(id);
    
    const contentType = res.headers.get("content-type") || "";
    const data = contentType.includes("application/json") ? await res.json() : await res.text();
    if (!res.ok) {
      const message = typeof data === "string" ? data : (data as any)?.message || "Request failed";
      throw new Error(message);
    }
    return data;
  } catch (error: any) {
    clearTimeout(id);
    if (error.name === 'AbortError') {
      throw new Error('Request timed out. Please check your network or backend server.');
    }
    throw error;
  }
}

