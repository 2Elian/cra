"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import {
  ArrowLeft,
  FileText,
  Search,
  Share2,
  Download,
  AlertTriangle,
  Send,
  Sparkles,
  Loader2,
} from "lucide-react";
import Link from "next/link";
import { contractService } from "@/services/contract";
import { ContractMain, CONTRACT_STATUS_MAP } from "@/types/contract";

export default function ContractDetailPage({ params }: { params: { id: string } }) {
  const [contract, setContract] = useState<ContractMain | null>(null);
  const [content, setContent] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const id = parseInt(params.id);
        const [contractData, contentData] = await Promise.all([
          contractService.getContractById(id),
          contractService.getContractContent(id)
        ]);
        setContract(contractData);
        setContent(contentData);
      } catch (err: any) {
        console.error(err);
        setError(err.message || "Failed to load contract details");
      } finally {
        setLoading(false);
      }
    };

    if (params.id) {
      fetchData();
    }
  }, [params.id]);

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (error || !contract) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-4">
        <p className="text-destructive">{error || "Contract not found"}</p>
        <Link href="/contracts">
          <Button variant="outline">Back to Contracts</Button>
        </Link>
      </div>
    );
  }

  const statusConfig = CONTRACT_STATUS_MAP[contract.status] || { label: "Unknown", variant: "default" };
  const formattedDate = contract.updateTime 
    ? new Date(contract.updateTime).toLocaleString() 
    : (contract.createTime ? new Date(contract.createTime).toLocaleString() : "N/A");

  return (
    <div className="flex flex-col h-[calc(100vh-theme(spacing.20))] gap-4">
      {/* Header */}
      <div className="flex items-center justify-between border-b pb-4">
        <div className="flex items-center gap-4">
          <Link href="/contracts">
            <Button variant="ghost" size="icon">
              <ArrowLeft className="h-4 w-4" />
            </Button>
          </Link>
          <div>
            <h1 className="text-xl font-bold tracking-tight flex items-center gap-2">
              {contract.contractName} <Badge variant={statusConfig.variant}>{statusConfig.label}</Badge>
            </h1>
            <p className="text-sm text-muted-foreground">
              ID: {contract.id} • Type: {contract.category || "General"} • Last updated: {formattedDate}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm">
            <Share2 className="mr-2 h-4 w-4" /> Share
          </Button>
          <Button variant="outline" size="sm">
            <Download className="mr-2 h-4 w-4" /> Export
          </Button>
          <Button size="sm">
             Submit for Approval
          </Button>
        </div>
      </div>

      {/* Main Content Split View */}
      <div className="flex flex-1 gap-6 overflow-hidden">
        {/* Document Viewer (Left) */}
        <Card className="flex-1 flex flex-col overflow-hidden">
          <CardHeader className="py-3 border-b flex flex-row items-center justify-between">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <FileText className="h-4 w-4" />
                <span>Document View</span>
            </div>
             <div className="flex items-center gap-2">
                <Button variant="ghost" size="icon" className="h-8 w-8">
                    <Search className="h-4 w-4" />
                </Button>
             </div>
          </CardHeader>
          <div className="flex-1 overflow-auto p-8 bg-muted/20">
            <div className="mx-auto max-w-3xl bg-background shadow-sm p-12 min-h-full">
                <p className="text-sm font-serif leading-loose whitespace-pre-wrap">
                  {content || "No content available for this contract."}
                </p>
            </div>
          </div>
        </Card>

        {/* AI Assistant (Right) */}
        <Card className="w-[400px] flex flex-col overflow-hidden">
            <Tabs defaultValue="risks" className="flex flex-col h-full">
                <div className="px-4 py-2 border-b">
                    <TabsList className="w-full">
                        <TabsTrigger value="risks" className="flex-1">Risks (3)</TabsTrigger>
                        <TabsTrigger value="suggestions" className="flex-1">Suggestions</TabsTrigger>
                        <TabsTrigger value="chat" className="flex-1">Assistant</TabsTrigger>
                    </TabsList>
                </div>

                <TabsContent value="risks" className="flex-1 overflow-auto p-4 m-0 space-y-4">
                    <div className="space-y-4">
                        <Card className="border-destructive/50 bg-destructive/5">
                            <CardHeader className="p-3 pb-2">
                                <CardTitle className="text-sm font-medium flex items-center gap-2 text-destructive">
                                    <AlertTriangle className="h-4 w-4" />
                                    Unbalanced Termination
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="p-3 pt-0 text-xs text-muted-foreground">
                                Clause 5 allows Client to terminate for convenience but restricts Provider. Recommended: Mutual termination for convenience.
                            </CardContent>
                        </Card>
                         <Card className="border-orange-200 bg-orange-50 dark:bg-orange-900/20 dark:border-orange-800">
                            <CardHeader className="p-3 pb-2">
                                <CardTitle className="text-sm font-medium flex items-center gap-2 text-orange-600 dark:text-orange-400">
                                    <AlertTriangle className="h-4 w-4" />
                                    Short Confidentiality Term
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="p-3 pt-0 text-xs text-muted-foreground">
                                Clause 3 limits confidentiality to 2 years. Standard for this industry is 5 years.
                            </CardContent>
                        </Card>
                         <Card className="border-yellow-200 bg-yellow-50 dark:bg-yellow-900/20 dark:border-yellow-800">
                            <CardHeader className="p-3 pb-2">
                                <CardTitle className="text-sm font-medium flex items-center gap-2 text-yellow-600 dark:text-yellow-400">
                                    <AlertTriangle className="h-4 w-4" />
                                    Missing Mutual Indemnification
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="p-3 pt-0 text-xs text-muted-foreground">
                                Clause 4 only indemnifies the Client. Consider adding mutual indemnification.
                            </CardContent>
                        </Card>
                    </div>
                </TabsContent>

                <TabsContent value="suggestions" className="flex-1 overflow-auto p-4 m-0">
                     <div className="space-y-4">
                        <div className="border rounded-lg p-3">
                            <div className="flex items-center gap-2 mb-2">
                                <Sparkles className="h-4 w-4 text-primary" />
                                <span className="text-sm font-medium">Clause 3 Rewrite</span>
                            </div>
                            <p className="text-xs text-muted-foreground mb-2">
                                "Provider agrees to keep all Client information confidential. This obligation survives the termination of this Agreement for a period of 5 years, and indefinitely for trade secrets."
                            </p>
                            <Button size="sm" variant="outline" className="w-full h-7 text-xs">Apply Change</Button>
                        </div>
                     </div>
                </TabsContent>

                <TabsContent value="chat" className="flex-1 flex flex-col p-0 m-0">
                    <div className="flex-1 overflow-auto p-4 space-y-4">
                        <div className="flex gap-3 text-sm">
                             <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                                <Sparkles className="h-4 w-4 text-primary" />
                             </div>
                             <div className="bg-muted p-3 rounded-lg rounded-tl-none">
                                <p>I've analyzed the contract. There are 3 high-risk clauses requiring your attention. Would you like me to draft a revision for the Termination clause?</p>
                             </div>
                        </div>
                        <div className="flex gap-3 text-sm flex-row-reverse">
                             <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center shrink-0 text-primary-foreground">
                                JD
                             </div>
                             <div className="bg-primary text-primary-foreground p-3 rounded-lg rounded-tr-none">
                                <p>Yes, please draft a mutual termination clause.</p>
                             </div>
                        </div>
                    </div>
                    <div className="p-4 border-t">
                        <form className="flex gap-2">
                            <Input placeholder="Ask AI about this contract..." />
                            <Button size="icon" type="submit">
                                <Send className="h-4 w-4" />
                            </Button>
                        </form>
                    </div>
                </TabsContent>
            </Tabs>
        </Card>
      </div>
    </div>
  );
}
