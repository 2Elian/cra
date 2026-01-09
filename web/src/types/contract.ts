export interface ContractMain {
  id: number;
  contractNumber: string;
  contractName: string;
  partyAId: number;
  partyBId: number;
  amount: number;
  startDate?: string;
  endDate?: string;
  status: number;
  category?: string;
  department?: string;
  creatorId: string;
  createTime: string;
  updateTime?: string;
  remark?: string;
}

export interface ContractContent {
  id: string;
  contractId: number;
  versionId: number;
  content: string;
  plainTextContent: string;
  htmlContent: string;
  extractedClauses: string;
  metadata: string;
  creatorId: string;
  createTime: string;
  updateTime: string;
}

export enum ContractStatus {
  DRAFT = 0,
  PENDING_REVIEW = 1,
  IN_PROGRESS = 2,
  COMPLETED = 3,
  RISK_DETECTED = 4,
}

export const CONTRACT_STATUS_MAP: Record<number, { label: string; variant: "default" | "secondary" | "destructive" | "outline" | "success" | "warning" }> = {
  [ContractStatus.DRAFT]: { label: "Draft", variant: "secondary" },
  [ContractStatus.PENDING_REVIEW]: { label: "Pending Review", variant: "warning" },
  [ContractStatus.IN_PROGRESS]: { label: "In Progress", variant: "default" },
  [ContractStatus.COMPLETED]: { label: "Completed", variant: "success" },
  [ContractStatus.RISK_DETECTED]: { label: "Risk Detected", variant: "destructive" },
};

export const CONTRACT_TYPES = [
  "Procurement Contract",
  "Sales Contract",
  "Non-Disclosure Agreement (NDA)",
  "Employment Contract",
  "Lease Agreement",
  "Service Agreement",
  "Other"
];
