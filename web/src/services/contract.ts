import { apiFetch, CONTRACT_SERVICE_URL } from "@/lib/api";
import { ContractMain } from "@/types/contract";
import { useAuthStore } from "@/store/auth";

export const contractService = {
  uploadSingle: async (file: File, category: string): Promise<ContractMain> => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("category", category);
    formData.append("contractType", category); // Try both standard names
    const token = useAuthStore.getState().token || undefined;
    
    const res = await apiFetch("/upload/single", {
      method: "POST",
      body: formData,
    }, token, CONTRACT_SERVICE_URL);
    
    if (res.code !== 200) {
        throw new Error(res.message || "Upload failed");
    }
    return res.data;
  },

  uploadBatch: async (files: File[], categories: string[]): Promise<ContractMain[]> => {
    const formData = new FormData();
    files.forEach(file => formData.append("files", file));
    // Append categories in order. Backend should bind List<String> categories
    categories.forEach(cat => {
        formData.append("categories", cat);
        formData.append("contractTypes", cat); // Try both
    });
    
    const token = useAuthStore.getState().token || undefined;

    const res = await apiFetch("/upload/batch", {
      method: "POST",
      body: formData,
    }, token, CONTRACT_SERVICE_URL);

    if (res.code !== 200) {
        throw new Error(res.message || "Batch upload failed");
    }
    return res.data;
  },

  fetchContracts: async (page = 0, size = 10, params = {}): Promise<{ list: ContractMain[], total: number }> => {
    const token = useAuthStore.getState().token || undefined;
    const queryParams = new URLSearchParams({
      page: page.toString(), // Backend expects 0-indexed page
      size: size.toString(),
      ...params
    });
    
    // Backend: @GetMapping
    const res = await apiFetch(`/?${queryParams.toString()}`, {}, token, CONTRACT_SERVICE_URL);
    
    if (res.code !== 200) {
      throw new Error(res.message || "Fetch contracts failed");
    }

    const data = res.data;
    let list: ContractMain[] = [];
    let total = 0;

    // Backend returns Page<ContractMain>
    // Spring Page structure usually has 'content' for list and 'totalElements' for count
    if (data && Array.isArray(data.content)) {
        list = data.content;
        total = data.totalElements;
    } else if (Array.isArray(data)) {
        // Fallback or different structure
        list = data;
        total = data.length;
    } else {
        // Fallback for MyBatis Page or similar if structure differs
        list = data?.list || data?.records || [];
        total = data?.total || list.length || 0;
    }
    
    return { list, total };
  },
  
  deleteContract: async (id: number): Promise<void> => {
      const token = useAuthStore.getState().token || undefined;
      // Backend: @DeleteMapping("/{id}")
      const res = await apiFetch(`/${id}`, {
          method: "DELETE"
      }, token, CONTRACT_SERVICE_URL);
      
      if (res.code !== 200) {
          throw new Error(res.message || "Delete failed");
      }
  },

  getContractById: async (id: number): Promise<ContractMain> => {
    const token = useAuthStore.getState().token || undefined;
    // Backend: @GetMapping("/{id}")
    const res = await apiFetch(`/${id}`, {}, token, CONTRACT_SERVICE_URL);

    if (res.code !== 200) {
        throw new Error(res.message || "Get contract failed");
    }
    return res.data;
  },

  getContractContent: async (contractId: number): Promise<string> => {
    const token = useAuthStore.getState().token || undefined;
    // Try to fetch content. Assuming endpoint /content/{contractId} or similar.
    // Based on common patterns: /content?contractId={id} or /content/{id}
    // Let's try /content/{id} relative to CONTRACT_SERVICE_URL which is /api/contracts
    // So it becomes /api/contracts/content/{id}
    const res = await apiFetch(`/content/${contractId}`, {}, token, CONTRACT_SERVICE_URL);

    if (res.code !== 200) {
       // If fail, return empty or throw? Return empty string to be safe for UI
       console.warn("Failed to fetch contract content", res);
       return "";
    }
    // Assuming it returns ContractContent object or just string?
    // If it returns ContractContent object, we need .content or .plainTextContent
    if (typeof res.data === 'string') return res.data;
    return res.data?.plainTextContent || res.data?.content || "";
  },

  updateContract: async (id: number, contract: Partial<ContractMain>): Promise<ContractMain> => {
    const token = useAuthStore.getState().token || undefined;
    // Backend: @PutMapping("/{id}")
    const res = await apiFetch(`/${id}`, {
        method: "PUT",
        body: JSON.stringify(contract)
    }, token, CONTRACT_SERVICE_URL);

    if (res.code !== 200) {
        throw new Error(res.message || "Update contract failed");
    }
    return res.data;
  }
};
