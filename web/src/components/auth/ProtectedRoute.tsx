"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuthStore } from "@/store/auth";

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const { token, load, loading } = useAuthStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    load();
  }, [load]);

  useEffect(() => {
    if (mounted && !loading && !token && pathname !== "/login") {
      router.push("/login");
    }
  }, [token, loading, router, pathname, mounted]);

  // Prevent hydration mismatch by rendering null until mounted
  if (!mounted) {
    return null;
  }

  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }

  if (!token && pathname !== "/login") {
      return null;
  }

  return <>{children}</>;
}
