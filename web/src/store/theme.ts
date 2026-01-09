"use client";

import { create } from "zustand";

type ThemeState = {
  theme: "light" | "dark";
  setTheme: (t: "light" | "dark") => void;
  init: () => void;
};

export const useThemeStore = create<ThemeState>((set) => ({
  theme: typeof window !== "undefined" ? ((localStorage.getItem("theme") as "light" | "dark") || "light") : "light",
  setTheme: (t) => {
    if (typeof window !== "undefined") {
      localStorage.setItem("theme", t);
      const root = document.documentElement;
      if (t === "dark") root.classList.add("dark");
      else root.classList.remove("dark");
    }
    set({ theme: t });
  },
  init: () => {
    if (typeof window !== "undefined") {
      const t = ((localStorage.getItem("theme") as "light" | "dark") || "light");
      const root = document.documentElement;
      if (t === "dark") root.classList.add("dark");
      else root.classList.remove("dark");
      set({ theme: t });
    }
  },
}));

