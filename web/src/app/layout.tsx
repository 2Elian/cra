import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { ThemeInit } from "@/components/ThemeInit";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Contract Review Agent",
  description: "AI-powered contract review and compliance system",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <ThemeInit />
        {children}
      </body>
    </html>
  );
}
