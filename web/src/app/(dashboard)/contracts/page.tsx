"use client";

import { useState, useEffect, useCallback } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  FileText,
  Search,
  Filter,
  MoreHorizontal,
  Plus,
  Eye,
  Play,
  Trash2,
  Loader2,
} from "lucide-react";
import Link from "next/link";
import { UploadContractDialog } from "@/components/contracts/UploadContractDialog";
import { ContractStatus, CONTRACT_STATUS_MAP, ContractMain } from "@/types/contract";
import { contractService } from "@/services/contract";
import { format } from "date-fns";

export default function ContractsPage() {
  const [contracts, setContracts] = useState<ContractMain[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadContracts = useCallback(async () => {
    setLoading(true);
    try {
      const { list } = await contractService.fetchContracts();
      setContracts(list);
      setError(null);
    } catch (err: any) {
      console.error("Failed to fetch contracts:", err);
      setError(err.message || "Failed to load contracts");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadContracts();
  }, [loadContracts]);

  const handleUploadSuccess = () => {
    loadContracts();
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Are you sure you want to delete this contract?")) return;
    try {
      await contractService.deleteContract(id);
      loadContracts();
    } catch (err: any) {
      alert(err.message || "Delete failed");
    }
  };

  const getStatusBadge = (status: number) => {
    const config = CONTRACT_STATUS_MAP[status] || { label: "Unknown", variant: "default" };
    return <Badge variant={config.variant}>{config.label}</Badge>;
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Contracts</h1>
          <p className="text-muted-foreground">
            Manage and review your contracts.
          </p>
        </div>
        <UploadContractDialog onUploadSuccess={handleUploadSuccess} />
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
          <CardTitle>All Contracts</CardTitle>
          <div className="flex items-center gap-2">
            <div className="relative">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                type="search"
                placeholder="Search..."
                className="w-[200px] rounded-md border border-input bg-background pl-9 pr-4 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>
            <Button variant="outline" size="icon">
              <Filter className="h-4 w-4" />
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Contract Name</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Risk Level</TableHead>
                  <TableHead>Last Modified</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                   <TableRow>
                    <TableCell colSpan={6} className="h-24 text-center">
                      <div className="flex justify-center items-center">
                        <Loader2 className="h-6 w-6 animate-spin text-primary" />
                        <span className="ml-2">Loading contracts...</span>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : error ? (
                   <TableRow>
                    <TableCell colSpan={6} className="h-24 text-center text-red-500">
                      {error}
                    </TableCell>
                  </TableRow>
                ) : contracts.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="h-24 text-center">
                      No contracts found.
                    </TableCell>
                  </TableRow>
                ) : (
                  contracts.map((contract) => (
                    <TableRow key={contract.id}>
                      <TableCell className="font-medium">
                        <Link href={`/contracts/${contract.id}`} className="hover:underline flex items-center gap-2">
                          <FileText className="h-4 w-4 text-muted-foreground" />
                          {contract.contractName}
                        </Link>
                      </TableCell>
                      <TableCell>{contract.category || "General"}</TableCell>
                      <TableCell>
                        {getStatusBadge(contract.status)}
                      </TableCell>
                      <TableCell>
                         {/* Risk is not yet in ContractMain, so mocking logic or check if backend adds it */}
                         <Badge variant="outline">Pending</Badge>
                      </TableCell>
                      <TableCell>{contract.updateTime ? format(new Date(contract.updateTime), 'yyyy-MM-dd') : "-"}</TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                           <Button variant="ghost" size="icon" title="Start Review">
                            <Play className="h-4 w-4" />
                          </Button>
                          <Link href={`/contracts/${contract.id}`}>
                            <Button variant="ghost" size="icon" title="View Details">
                              <Eye className="h-4 w-4" />
                            </Button>
                          </Link>
                          <Button 
                            variant="ghost" 
                            size="icon" 
                            className="text-destructive hover:text-destructive" 
                            title="Delete"
                            onClick={() => handleDelete(contract.id)}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
