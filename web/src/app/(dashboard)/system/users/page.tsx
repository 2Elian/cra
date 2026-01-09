"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { useAdminStore } from "@/store/admin";
import { Loader2, Trash2, Shield, UserX, UserCheck, Plus, Info } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { Select, SelectTrigger, SelectContent, SelectItem, SelectValue } from "@/components/ui/select";

export default function UsersPage() {
  const { users, fetchUsers, deleteUser, toggleUserStatus, createUser, loading, getUserById, updateUserAdmin } = useAdminStore();
  const [isCreating, setIsCreating] = useState(false);
  const [newUser, setNewUser] = useState({ username: "", password: "", email: "", realName: "", phone: "" });
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailUser, setDetailUser] = useState<any | null>(null);
  const [createUsernameError, setCreateUsernameError] = useState("");
  const [createEmailError, setCreateEmailError] = useState("");

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const handleDelete = async (id: number) => {
    const target = users.find((u) => u.id === id);
    if (target?.type === 1) {
      alert("Cannot delete admin user");
      return;
    }
    if (confirm("Are you sure you want to delete this user?")) {
      await deleteUser(id);
    }
  };

  const handleToggleStatus = async (id: number, currentStatus: number) => {
    const newStatus = currentStatus === 1 ? 0 : 1;
    await toggleUserStatus(id, newStatus);
  };

  const openDetails = async (id: number) => {
    const u = await getUserById(id);
    if (u) {
      setDetailUser({
        id: u.id,
        username: u.username || "",
        realName: u.realName || "",
        email: u.email || "",
        phone: u.phone || "",
        type: u.type ?? 0,
        password: "",
      });
      setDetailOpen(true);
    }
  };

  const handleDetailSave = async () => {
    if (!detailUser) return;

    // Frontend Validation: Check for duplicate email
    if (detailUser.email) {
      const duplicateEmail = users.find(u => u.email === detailUser.email && u.id !== detailUser.id);
      if (duplicateEmail) {
        alert("This email is already in use by another user.");
        return;
      }
    }

    try {
      const payload: any = { 
        id: detailUser.id,
        email: detailUser.email,
        phone: detailUser.phone,
        type: detailUser.type,
        realName: detailUser.realName,
      };
      // Only include password if it's not empty
      if (detailUser.password) {
        payload.password = detailUser.password;
      }
      
      await updateUserAdmin(payload);
      setDetailOpen(false);
      setDetailUser(null);
      await fetchUsers();
    } catch (e: any) {
      alert(e.message || "Failed to update user");
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createUser(newUser);
      setIsCreating(false);
      setNewUser({ username: "", password: "", email: "", realName: "", phone: "" });
    } catch (e: any) {
      alert("Failed to create user: " + e.message);
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Users</h1>
          <p className="text-muted-foreground">
            Manage system users and their roles.
          </p>
        </div>
        <div className="flex gap-2">
          <Button onClick={() => setIsCreating(!isCreating)}>
            {isCreating ? "Cancel" : <><Plus className="mr-2 h-4 w-4" /> Create User</>}
          </Button>
          <Button variant="outline" onClick={() => fetchUsers()}>Refresh</Button>
        </div>
      </div>

      {isCreating && (
        <Card>
          <CardHeader>
            <CardTitle>Create New User</CardTitle>
            <CardDescription>Add a new user to the system.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleCreate} className="grid gap-4 md:grid-cols-2 lg:grid-cols-3 items-end">
              <div className="space-y-2">
                <label className="text-sm font-medium">Username</label>
                <Input 
                  value={newUser.username} 
                  onChange={(e) => {
                    const v = e.target.value;
                    setNewUser({ ...newUser, username: v });
                    const dup = users.some((u) => (u.username || "") === v);
                    setCreateUsernameError(dup ? "Username already exists" : "");
                  }} 
                  required 
                />
                {createUsernameError ? <p className="text-xs text-destructive">{createUsernameError}</p> : null}
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Password</label>
                <Input 
                  type="password"
                  value={newUser.password} 
                  onChange={(e) => setNewUser({...newUser, password: e.target.value})} 
                  required 
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Email</label>
                <Input 
                  type="email"
                  value={newUser.email} 
                  onChange={(e) => {
                    const v = e.target.value;
                    setNewUser({ ...newUser, email: v });
                    const dup = users.some((u) => ((u.email || "").toLowerCase()) === v.toLowerCase());
                    setCreateEmailError(dup ? "Email already exists" : "");
                  }} 
                  required 
                />
                {createEmailError ? <p className="text-xs text-destructive">{createEmailError}</p> : null}
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Real Name</label>
                <Input 
                  value={newUser.realName} 
                  onChange={(e) => setNewUser({...newUser, realName: e.target.value})} 
                />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Phone</label>
                <Input 
                  value={newUser.phone} 
                  onChange={(e) => setNewUser({...newUser, phone: e.target.value})} 
                />
              </div>
              <Button type="submit" disabled={!!createEmailError || !!createUsernameError}>Create User</Button>
            </form>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>User List</CardTitle>
          <CardDescription>
            A list of all users in the system.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Username</TableHead>
                <TableHead>Real Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading && users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="h-24 text-center">
                    <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                  </TableCell>
                </TableRow>
              ) : users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="h-24 text-center">
                    No users found.
                  </TableCell>
                </TableRow>
              ) : (
                users.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell className="font-medium">{user.username}</TableCell>
                    <TableCell>{user.realName || "-"}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>
                      <Badge variant={user.status === 1 ? "default" : "secondary"}>
                        {user.status === 1 ? "Active" : "Disabled"}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          onClick={() => handleToggleStatus(user.id, user.status || 0)}
                          title={user.status === 1 ? "Disable User" : "Enable User"}
                        >
                          {user.status === 1 ? <UserX className="h-4 w-4" /> : <UserCheck className="h-4 w-4" />}
                        </Button>
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          onClick={() => openDetails(user.id)}
                          title="View Details"
                        >
                          <Info className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" onClick={() => handleDelete(user.id)} disabled={user.type === 1} title={user.type === 1 ? "Admin cannot be deleted" : "Delete"}>
                          <Trash2 className="h-4 w-4 text-destructive" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {detailOpen && (
        <Dialog open={true} onOpenChange={setDetailOpen}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>User Details</DialogTitle>
              <DialogDescription>View and edit user information.</DialogDescription>
            </DialogHeader>
            {detailUser ? (
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Username</label>
                  <Input value={detailUser.username} disabled />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Real Name</label>
                  <Input value={detailUser.realName} onChange={(e) => setDetailUser({ ...detailUser, realName: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Email</label>
                  <Input type="email" value={detailUser.email} onChange={(e) => setDetailUser({ ...detailUser, email: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Phone</label>
                  <Input value={detailUser.phone} onChange={(e) => setDetailUser({ ...detailUser, phone: e.target.value })} />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Type</label>
                  <Select value={String(detailUser.type)} onValueChange={(v) => setDetailUser({ ...detailUser, type: Number(v) })}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select type" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="0">Ordinary User</SelectItem>
                      <SelectItem value="1">Admin</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2 md:col-span-2">
                  <label className="text-sm font-medium">New Password</label>
                  <Input type="password" value={detailUser.password} onChange={(e) => setDetailUser({ ...detailUser, password: e.target.value })} placeholder="Leave blank to keep unchanged" />
                </div>
              </div>
            ) : null}
            <DialogFooter>
              <Button variant="outline" onClick={() => setDetailOpen(false)}>Cancel</Button>
              <Button onClick={handleDetailSave}>Save Changes</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
