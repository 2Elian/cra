"use client";

import Link from "next/link";
import { Bell, Search } from "lucide-react";
import { useAuthStore } from "@/store/auth";

export function Header() {
  const { user, logout } = useAuthStore();
  const initials = user?.name ? user.name.split(" ").map((s) => s[0]).join("").slice(0, 2).toUpperCase() : "LG";
  return (
    <header className="sticky top-0 z-30 flex h-14 items-center gap-4 border-b bg-background px-6">
      <div className="flex flex-1 items-center gap-4">
        <form className="relative flex-1 max-w-md hidden sm:flex">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <input
            type="search"
            placeholder="Search contracts, clauses, or risks..."
            className="w-full rounded-md border border-input bg-background pl-9 pr-4 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          />
        </form>
      </div>
      <div className="flex items-center gap-4">
        <button className="relative rounded-full p-2 hover:bg-accent text-muted-foreground hover:text-foreground transition-colors">
          <Bell className="h-5 w-5" />
          <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-destructive" />
          <span className="sr-only">Notifications</span>
        </button>
        {user ? (
          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center text-primary text-xs font-bold">{initials}</div>
            <button onClick={logout} className="text-sm text-muted-foreground hover:text-foreground">Logout</button>
          </div>
        ) : (
          <Link href="/login" className="text-sm text-muted-foreground hover:text-foreground">Login</Link>
        )}
      </div>
    </header>
  );
}
