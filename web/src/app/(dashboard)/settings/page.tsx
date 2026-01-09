"use client";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useThemeStore } from "@/store/theme";
import { useAuthStore } from "@/store/auth";

export default function SettingsPage() {
  const { theme, setTheme } = useThemeStore();
  const { user, updateProfile, updatePassword, loading } = useAuthStore();
  
  const [profileData, setProfileData] = useState({
    realName: user?.realName || "",
    email: user?.email || "",
    phone: user?.phone || "",
    role: user?.role || "",
  });

  const [passwordData, setPasswordData] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const userInitials = user?.name 
    ? user.name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2)
    : user?.username?.slice(0, 2).toUpperCase() || "JD";

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setProfileData({ ...profileData, [e.target.name]: e.target.value });
  };

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPasswordData({ ...passwordData, [e.target.name]: e.target.value });
  };

  const onSaveProfile = async () => {
    try {
      await updateProfile(profileData);
      alert("Profile updated successfully");
    } catch (e: any) {
      alert("Failed to update profile: " + e.message);
    }
  };

  const onUpdatePassword = async () => {
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      alert("New passwords do not match");
      return;
    }
    try {
      await updatePassword({ 
        oldPassword: passwordData.oldPassword, 
        newPassword: passwordData.newPassword 
      });
      alert("Password updated successfully");
      setPasswordData({ oldPassword: "", newPassword: "", confirmPassword: "" });
    } catch (e: any) {
      alert("Failed to update password: " + e.message);
    }
  };
    
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
        <p className="text-muted-foreground">
          Manage your account settings and preferences.
        </p>
      </div>

      <Tabs defaultValue="general" className="space-y-4">
        <TabsList>
          <TabsTrigger value="general">General</TabsTrigger>
          <TabsTrigger value="notifications">Notifications</TabsTrigger>
          <TabsTrigger value="security">Security</TabsTrigger>
        </TabsList>

        <TabsContent value="general" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Profile Information</CardTitle>
              <CardDescription>
                Update your account profile details.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-4">
                <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center text-primary text-2xl font-bold">
                  {userInitials}
                </div>
                <Button variant="outline" size="sm">Change Avatar</Button>
              </div>
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Full Name</label>
                  <Input name="realName" value={profileData.realName} onChange={handleProfileChange} placeholder={user?.realName || user?.name || user?.username || ""} />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium">Email</label>
                  <Input name="email" value={profileData.email} onChange={handleProfileChange} />
                </div>
                 <div className="space-y-2">
                  <label className="text-sm font-medium">Job Title</label>
                  <Input name="role" value={profileData.role} onChange={handleProfileChange} disabled />
                </div>
                 <div className="space-y-2">
                  <label className="text-sm font-medium">Phone</label>
                  <Input name="phone" value={profileData.phone} onChange={handleProfileChange} />
                </div>
              </div>
              <div className="flex justify-end">
                  <Button onClick={onSaveProfile} disabled={loading}>Save Changes</Button>
              </div>
            </CardContent>
          </Card>
           <Card>
            <CardHeader>
              <CardTitle>Appearance</CardTitle>
              <CardDescription>
                Customize the interface theme.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
               <div className="flex items-center justify-between">
                   <div className="space-y-0.5">
                       <label className="text-sm font-medium">Theme</label>
                       <p className="text-xs text-muted-foreground">Select your preferred theme.</p>
                   </div>
                   <div className="flex gap-2">
                       <button aria-label="Light" onClick={() => setTheme("light")} className={`h-8 w-8 rounded-full bg-white border border-gray-200 ${theme === "light" ? "ring-2 ring-primary" : ""}`}></button>
                       <button aria-label="Dark" onClick={() => setTheme("dark")} className={`h-8 w-8 rounded-full bg-slate-950 border border-slate-800 ${theme === "dark" ? "ring-2 ring-primary" : ""}`}></button>
                   </div>
               </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="notifications" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Notification Preferences</CardTitle>
              <CardDescription>
                Choose what you want to be notified about.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
                {[
                    { title: "Contract Reviews", desc: "Notify me when a contract review is completed." },
                    { title: "Risk Alerts", desc: "Notify me when high-risk clauses are detected." },
                    { title: "Approval Requests", desc: "Notify me when a contract needs my approval." },
                    { title: "System Updates", desc: "Notify me about new features and updates." },
                ].map((item, i) => (
                    <div key={i} className="flex items-center justify-between py-2">
                         <div className="space-y-0.5">
                            <label className="text-sm font-medium">{item.title}</label>
                            <p className="text-xs text-muted-foreground">{item.desc}</p>
                        </div>
                        <div className="h-6 w-11 rounded-full bg-primary relative cursor-pointer">
                            <div className="absolute right-1 top-1 h-4 w-4 rounded-full bg-white"></div>
                        </div>
                    </div>
                ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="security" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Password & Security</CardTitle>
              <CardDescription>
                Manage your password and security settings.
              </CardDescription>
            </CardHeader>
             <CardContent className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Current Password</label>
                  <Input type="password" name="oldPassword" value={passwordData.oldPassword} onChange={handlePasswordChange} />
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                    <label className="text-sm font-medium">New Password</label>
                    <Input type="password" name="newPassword" value={passwordData.newPassword} onChange={handlePasswordChange} />
                    </div>
                    <div className="space-y-2">
                    <label className="text-sm font-medium">Confirm New Password</label>
                    <Input type="password" name="confirmPassword" value={passwordData.confirmPassword} onChange={handlePasswordChange} />
                    </div>
                </div>
                 <div className="flex justify-end">
                    <Button onClick={onUpdatePassword} disabled={loading}>Update Password</Button>
                </div>
             </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
