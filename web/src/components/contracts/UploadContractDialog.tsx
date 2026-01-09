"use client";

import { useState, useRef, useEffect } from "react";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Upload, X, FileText, Loader2 } from "lucide-react";
import { contractService } from "@/services/contract";
import { CONTRACT_TYPES } from "@/types/contract";

interface UploadContractDialogProps {
  onUploadSuccess?: () => void;
  trigger?: React.ReactNode;
}

export function UploadContractDialog({ onUploadSuccess, trigger }: UploadContractDialogProps) {
  const [open, setOpen] = useState(false);
  const [files, setFiles] = useState<{ file: File; type: string }[]>([]);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [defaultType, setDefaultType] = useState<string>(CONTRACT_TYPES[0]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const newFiles = Array.from(e.target.files).map(file => ({
        file,
        type: defaultType
      }));
      setFiles((prev) => [...prev, ...newFiles]);
      setError(null);
    }
    if (fileInputRef.current) {
        fileInputRef.current.value = "";
    }
  };

  const removeFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleTypeChange = (index: number, newType: string) => {
    setFiles(prev => prev.map((item, i) => i === index ? { ...item, type: newType } : item));
  };

  const handleDefaultTypeChange = (newType: string) => {
    setDefaultType(newType);
    // Optional: Update all existing files to the new default?
    // User flow: "if didn't choose others, use default". 
    // It's helpful to update all when changing the main dropdown.
    setFiles(prev => prev.map(item => ({ ...item, type: newType })));
  };

  const handleUpload = async () => {
    if (files.length === 0) {
      setError("Please select at least one file.");
      return;
    }

    setUploading(true);
    setError(null);

    try {
      const fileObjs = files.map(f => f.file);
      const types = files.map(f => f.type);

      if (files.length === 1) {
        await contractService.uploadSingle(fileObjs[0], types[0]);
      } else {
        await contractService.uploadBatch(fileObjs, types);
      }
      
      setFiles([]);
      setOpen(false);
      if (onUploadSuccess) {
        onUploadSuccess();
      }
    } catch (err: any) {
      console.error(err);
      setError(err.message || "Upload failed");
    } finally {
      setUploading(false);
    }
  };

  const handleOpenChange = (newOpen: boolean) => {
      setOpen(newOpen);
      if (!newOpen) {
          setFiles([]);
          setError(null);
      }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        {trigger || (
          <Button>
            <Upload className="mr-2 h-4 w-4" /> Upload Contract
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Upload Contract</DialogTitle>
          <DialogDescription>
            Upload contract files (PDF, DOCX) and specify their types.
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          <div className="flex items-center gap-4">
             <label className="text-sm font-medium whitespace-nowrap">Default Type:</label>
             <Select value={defaultType} onValueChange={handleDefaultTypeChange}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="Select contract type" />
                </SelectTrigger>
                <SelectContent>
                  {CONTRACT_TYPES.map(type => (
                    <SelectItem key={type} value={type}>{type}</SelectItem>
                  ))}
                </SelectContent>
             </Select>
          </div>

          <div className="flex items-center justify-center w-full">
            <label
              htmlFor="dropzone-file"
              className="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed rounded-lg cursor-pointer bg-gray-50 dark:hover:bg-gray-800 dark:bg-gray-900 hover:bg-gray-100 dark:border-gray-700 dark:hover:border-gray-500 dark:hover:bg-gray-800"
            >
              <div className="flex flex-col items-center justify-center pt-5 pb-6">
                <Upload className="w-8 h-8 mb-4 text-gray-500 dark:text-gray-400" />
                <p className="mb-2 text-sm text-gray-500 dark:text-gray-400">
                  <span className="font-semibold">Click to upload</span>
                </p>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  PDF, DOCX (Max 10MB)
                </p>
              </div>
              <input
                id="dropzone-file"
                type="file"
                className="hidden"
                multiple
                onChange={handleFileChange}
                ref={fileInputRef}
                accept=".pdf,.doc,.docx"
              />
            </label>
          </div>

          {files.length > 0 && (
            <div className="space-y-2 max-h-[200px] overflow-y-auto pr-2">
              {files.map((item, index) => (
                <div key={index} className="flex items-center justify-between p-2 bg-muted rounded-md text-sm gap-2">
                  <div className="flex items-center gap-2 overflow-hidden flex-1">
                    <FileText className="h-4 w-4 flex-shrink-0 text-muted-foreground" />
                    <span className="truncate max-w-[150px]" title={item.file.name}>{item.file.name}</span>
                  </div>
                  
                  <Select value={item.type} onValueChange={(val) => handleTypeChange(index, val)}>
                    <SelectTrigger className="w-[180px] h-8 text-xs">
                      <SelectValue placeholder="Type" />
                    </SelectTrigger>
                    <SelectContent>
                        {CONTRACT_TYPES.map(type => (
                            <SelectItem key={type} value={type} className="text-xs">{type}</SelectItem>
                        ))}
                    </SelectContent>
                  </Select>

                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-6 w-6 flex-shrink-0"
                    onClick={() => removeFile(index)}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              ))}
            </div>
          )}

          {error && <div className="text-sm text-red-500">{error}</div>}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)} disabled={uploading}>
            Cancel
          </Button>
          <Button onClick={handleUpload} disabled={files.length === 0 || uploading}>
            {uploading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Upload {files.length > 0 && `(${files.length})`}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
