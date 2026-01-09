"use client";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Download, Calendar } from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
} from "recharts";

const complianceData = [
  { name: "Jan", passed: 45, rejected: 5 },
  { name: "Feb", passed: 52, rejected: 3 },
  { name: "Mar", passed: 48, rejected: 8 },
  { name: "Apr", passed: 61, rejected: 2 },
  { name: "May", passed: 55, rejected: 4 },
  { name: "Jun", passed: 67, rejected: 3 },
];

const riskData = [
  { name: "Low Risk", value: 65, color: "#22c55e" },
  { name: "Medium Risk", value: 25, color: "#f59e0b" },
  { name: "High Risk", value: 10, color: "#ef4444" },
];

const typeData = [
  { name: "NDA", count: 120 },
  { name: "Service", count: 85 },
  { name: "License", count: 45 },
  { name: "Employment", count: 30 },
  { name: "Other", count: 15 },
];

const ComplianceTooltip = ({ active, payload, label }: any) => {
  if (!active || !payload || payload.length === 0) return null;
  return (
    <div className="rounded-lg border bg-background/95 backdrop-blur-sm p-3 shadow-xl min-w-[150px]">
      <div className="text-sm font-semibold mb-2">{label}</div>
      <div className="space-y-1.5">
        {payload.map((item: any, idx: number) => (
          <div key={idx} className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-2">
              <span
                className="w-2 h-2 rounded-full"
                style={{
                  backgroundColor:
                    item.name === "Passed"
                      ? "hsl(var(--primary))"
                      : "hsl(var(--destructive))",
                }}
              />
              <span className="text-xs text-muted-foreground">{item.name}</span>
            </div>
            <span className="text-xs font-bold font-mono">
              {item.value?.toLocaleString?.() ?? item.value}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

const PieTooltip = ({ active, payload }: any) => {
  if (!active || !payload || payload.length === 0) return null;
  const item = payload[0];
  const color = item?.payload?.color || item?.fill;
  return (
    <div className="rounded-lg border bg-background/95 backdrop-blur-sm p-3 shadow-xl min-w-[150px]">
      <div className="flex items-center gap-2 mb-2">
        <span
          className="w-2 h-2 rounded-full"
          style={{ backgroundColor: color }}
        />
        <span className="text-sm font-semibold">{item.name}</span>
      </div>
      <div className="flex items-center justify-between gap-4">
        <span className="text-xs text-muted-foreground">Share</span>
        <span className="text-xs font-bold font-mono">
          {item.value?.toLocaleString?.() ?? item.value}%
        </span>
      </div>
    </div>
  );
};

export default function ReportsPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Reports & Analytics</h1>
          <p className="text-muted-foreground">
            Detailed insights into contract compliance and risk trends.
          </p>
        </div>
        <div className="flex items-center gap-2">
            <Select defaultValue="30d">
                <SelectTrigger className="w-[140px]">
                    <Calendar className="mr-2 h-4 w-4" />
                    <SelectValue placeholder="Period" />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value="7d">Last 7 Days</SelectItem>
                    <SelectItem value="30d">Last 30 Days</SelectItem>
                    <SelectItem value="90d">Last 3 Months</SelectItem>
                    <SelectItem value="ytd">Year to Date</SelectItem>
                </SelectContent>
            </Select>
          <Button variant="outline">
            <Download className="mr-2 h-4 w-4" /> Export PDF
          </Button>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-7">
        {/* Compliance Trend */}
        <Card className="col-span-4">
          <CardHeader>
            <CardTitle>Compliance Review Trend</CardTitle>
            <CardDescription>
              Number of contracts passed vs. rejected over time.
            </CardDescription>
          </CardHeader>
          <CardContent className="pl-2">
            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={complianceData}>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                    <XAxis dataKey="name" className="text-xs" />
                    <YAxis className="text-xs" />
                    <Tooltip content={<ComplianceTooltip />} />
                    <Bar dataKey="passed" name="Passed" fill="hsl(var(--primary))" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="rejected" name="Rejected" fill="hsl(var(--destructive))" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Risk Distribution */}
        <Card className="col-span-3">
          <CardHeader>
            <CardTitle>Risk Distribution</CardTitle>
            <CardDescription>
              Percentage of contracts by risk level.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[300px] w-full flex justify-center">
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
                            data={riskData}
                            cx="50%"
                            cy="50%"
                            innerRadius={60}
                            outerRadius={80}
                            paddingAngle={5}
                            dataKey="value"
                        >
                            {riskData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={entry.color} />
                            ))}
                        </Pie>
                         <Tooltip content={<PieTooltip />} />
                    </PieChart>
                </ResponsiveContainer>
            </div>
            <div className="flex justify-center gap-6 mt-4">
                {riskData.map((item, index) => (
                    <div key={index} className="flex items-center gap-2">
                        <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
                        <span className="text-sm text-muted-foreground">{item.name}</span>
                    </div>
                ))}
            </div>
          </CardContent>
        </Card>
      </div>

       <div className="grid gap-6 md:grid-cols-2">
           <Card>
                <CardHeader>
                    <CardTitle>Contract Types</CardTitle>
                     <CardDescription>
                        Breakdown of processed contract types.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                     <div className="space-y-4">
                        {typeData.map((item, i) => (
                            <div key={i} className="flex items-center justify-between">
                                <div className="flex items-center gap-2">
                                     <div className="w-full bg-secondary h-2 rounded-full w-[100px] overflow-hidden">
                                        <div className="bg-primary h-full" style={{ width: `${(item.count / 150) * 100}%` }}></div>
                                     </div>
                                     <span className="text-sm font-medium">{item.name}</span>
                                </div>
                                <span className="text-sm text-muted-foreground">{item.count}</span>
                            </div>
                        ))}
                     </div>
                </CardContent>
           </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Average Processing Time</CardTitle>
                     <CardDescription>
                        Average time taken to review contracts.
                    </CardDescription>
                </CardHeader>
                 <CardContent>
                    <div className="flex items-baseline gap-2">
                        <span className="text-4xl font-bold">1.2</span>
                        <span className="text-sm text-muted-foreground">days</span>
                    </div>
                     <p className="text-xs text-muted-foreground mt-2">
                        ↓ 0.5 days faster than last month
                    </p>
                    <div className="h-[200px] mt-4">
                         <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={complianceData}>
                                <Line type="monotone" dataKey="passed" stroke="hsl(var(--primary))" strokeWidth={2} dot={false} />
                            </LineChart>
                         </ResponsiveContainer>
                    </div>
                </CardContent>
           </Card>
       </div>
    </div>
  );
}
