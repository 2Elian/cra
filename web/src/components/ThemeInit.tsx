"use client";

import { useEffect } from "react";
import { useThemeStore } from "@/store/theme";

export function ThemeInit() {
  const init = useThemeStore((s) => s.init);
  useEffect(() => { init(); }, [init]);
  return null;
}

