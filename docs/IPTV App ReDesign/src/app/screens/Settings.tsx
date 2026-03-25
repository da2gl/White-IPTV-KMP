import { useState } from "react";
import { useNavigate } from "react-router";
import {
  ChevronLeft,
  ChevronRight,
  Palette,
  MonitorPlay,
  Languages,
  Zap,
  Database,
  List,
  Heart,
  RotateCcw,
  Shield,
  FileText,
  Info,
  Bell,
} from "lucide-react";
import * as Switch from "@radix-ui/react-switch";
import { useTheme } from "../lib/ThemeContext";
import ThemeSheet from "../components/ThemeSheet";

export default function Settings() {
  const navigate = useNavigate();
  const { theme } = useTheme();
  const [autoUpdate, setAutoUpdate] = useState(true);
  const [notifications, setNotifications] = useState(false);
  const [showThemeSheet, setShowThemeSheet] = useState(false);

  const settingsSections = [
    {
      title: "Appearance",
      icon: Palette,
      color: "from-[#8b5cf6] to-[#7c3aed]",
      items: [
        {
          label: "Theme",
          value: theme === "dark" ? "Dark" : "Light",
          icon: Palette,
          action: () => setShowThemeSheet(true),
        },
        { label: "Accent Color", value: "Cyan", icon: Palette },
        { label: "Channel View", value: "Grid", icon: MonitorPlay },
      ],
    },
    {
      title: "Playback",
      icon: MonitorPlay,
      color: "from-[#00d4ff] to-[#0088ff]",
      items: [
        { label: "Default Player", value: "Internal", icon: MonitorPlay },
        { label: "Quality", value: "Auto", icon: Zap },
      ],
    },
    {
      title: "App Behavior",
      icon: Zap,
      color: "from-[#10b981] to-[#059669]",
      items: [
        { label: "Default Playlist", value: "Main Playlist", icon: List },
        { label: "Language", value: "English", icon: Languages },
      ],
    },
    {
      title: "Data & Storage",
      icon: Database,
      color: "from-[#f59e0b] to-[#d97706]",
      items: [
        { label: "Manage Playlists", action: () => navigate("/playlists"), icon: List },
        { label: "Clear Favorites", action: () => {}, icon: Heart },
        { label: "Clear Cache", action: () => {}, icon: RotateCcw },
      ],
    },
  ];

  const isDark = theme === "dark";

  return (
    <div
      className={`min-h-screen ${
        isDark ? "bg-gradient-to-b from-[#0a0f14] to-[#0f1419]" : "bg-gradient-to-b from-[#f8f9fa] to-[#e9ecef]"
      }`}
    >
      {/* Header */}
      <header
        className={`sticky top-0 z-40 backdrop-blur-xl border-b ${
          isDark ? "bg-[#0a0f14]/80 border-[#1a2026]" : "bg-white/80 border-gray-200"
        }`}
      >
        <div className="px-4 py-4">
          <h1
            className={`text-2xl font-bold ${
              isDark ? "text-white" : "text-gray-900"
            }`}
          >
            Settings
          </h1>
        </div>
      </header>

      <div className="px-4 py-6 space-y-6">
        {settingsSections.map((section) => (
          <section key={section.title} className="space-y-3">
            <div className="flex items-center gap-3 px-2">
              <div
                className={`size-8 rounded-lg bg-gradient-to-br ${section.color} flex items-center justify-center`}
              >
                <section.icon className="size-4 text-white" />
              </div>
              <h2 className={`text-sm font-bold uppercase tracking-wider ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
                {section.title}
              </h2>
            </div>

            <div className="space-y-2">
              {section.items.map((item) => (
                <button
                  key={item.label}
                  onClick={item.action}
                  className={`w-full p-4 rounded-2xl border active:scale-98 transition-transform ${
                    isDark 
                      ? 'bg-white/5 border-white/10' 
                      : 'bg-white border-gray-200 shadow-sm'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <item.icon className={`size-5 ${isDark ? 'text-white/70' : 'text-gray-600'}`} />
                      <span className={`font-medium ${isDark ? 'text-white' : 'text-gray-900'}`}>{item.label}</span>
                    </div>
                    {item.value && (
                      <div className="flex items-center gap-2">
                        <span className="text-sm text-[#00d4ff]">{item.value}</span>
                        <ChevronRight className={`size-4 ${isDark ? 'text-white/40' : 'text-gray-400'}`} />
                      </div>
                    )}
                    {item.action && !item.value && (
                      <ChevronRight className={`size-4 ${isDark ? 'text-white/40' : 'text-gray-400'}`} />
                    )}
                  </div>
                </button>
              ))}
            </div>
          </section>
        ))}

        {/* Toggle Settings */}
        <section className="space-y-3">
          <div className="flex items-center gap-3 px-2">
            <div className="size-8 rounded-lg bg-gradient-to-br from-[#ec4899] to-[#be185d] flex items-center justify-center">
              <Bell className="size-4 text-white" />
            </div>
            <h2 className={`text-sm font-bold uppercase tracking-wider ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
              Preferences
            </h2>
          </div>

          <div className="space-y-2">
            <div className={`p-4 rounded-2xl border ${
              isDark 
                ? 'bg-white/5 border-white/10' 
                : 'bg-white border-gray-200 shadow-sm'
            }`}>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Zap className={`size-5 ${isDark ? 'text-white/70' : 'text-gray-600'}`} />
                  <div>
                    <p className={`font-medium ${isDark ? 'text-white' : 'text-gray-900'}`}>Auto Update Playlists</p>
                    <p className={`text-xs mt-0.5 ${isDark ? 'text-white/50' : 'text-gray-500'}`}>Update on app startup</p>
                  </div>
                </div>
                <Switch.Root
                  checked={autoUpdate}
                  onCheckedChange={setAutoUpdate}
                  className={`w-12 h-7 rounded-full transition-colors ${
                    isDark 
                      ? 'bg-white/10 data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-[#00d4ff] data-[state=checked]:to-[#0088ff]'
                      : 'bg-gray-200 data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-[#00d4ff] data-[state=checked]:to-[#0088ff]'
                  }`}
                >
                  <Switch.Thumb className="block size-5 bg-white rounded-full transition-transform translate-x-1 data-[state=checked]:translate-x-6 shadow-md" />
                </Switch.Root>
              </div>
            </div>

            <div className={`p-4 rounded-2xl border ${
              isDark 
                ? 'bg-white/5 border-white/10' 
                : 'bg-white border-gray-200 shadow-sm'
            }`}>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Bell className={`size-5 ${isDark ? 'text-white/70' : 'text-gray-600'}`} />
                  <div>
                    <p className={`font-medium ${isDark ? 'text-white' : 'text-gray-900'}`}>Notifications</p>
                    <p className={`text-xs mt-0.5 ${isDark ? 'text-white/50' : 'text-gray-500'}`}>Channel updates</p>
                  </div>
                </div>
                <Switch.Root
                  checked={notifications}
                  onCheckedChange={setNotifications}
                  className={`w-12 h-7 rounded-full transition-colors ${
                    isDark 
                      ? 'bg-white/10 data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-[#00d4ff] data-[state=checked]:to-[#0088ff]'
                      : 'bg-gray-200 data-[state=checked]:bg-gradient-to-r data-[state=checked]:from-[#00d4ff] data-[state=checked]:to-[#0088ff]'
                  }`}
                >
                  <Switch.Thumb className="block size-5 bg-white rounded-full transition-transform translate-x-1 data-[state=checked]:translate-x-6 shadow-md" />
                </Switch.Root>
              </div>
            </div>
          </div>
        </section>

        {/* About Section */}
        <section className="space-y-3">
          <div className="flex items-center gap-3 px-2">
            <div className="size-8 rounded-lg bg-gradient-to-br from-[#64748b] to-[#475569] flex items-center justify-center">
              <Info className="size-4 text-white" />
            </div>
            <h2 className={`text-sm font-bold uppercase tracking-wider ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
              About
            </h2>
          </div>

          <div className="space-y-2">
            <div className={`p-4 rounded-2xl border ${
              isDark 
                ? 'bg-white/5 border-white/10' 
                : 'bg-white border-gray-200 shadow-sm'
            }`}>
              <div className="flex items-center justify-between">
                <span className={`font-medium ${isDark ? 'text-white' : 'text-gray-900'}`}>App Version</span>
                <span className={`text-sm ${isDark ? 'text-white/50' : 'text-gray-500'}`}>1.4.2</span>
              </div>
            </div>

            <button className={`w-full p-4 rounded-2xl border active:scale-98 transition-transform ${
              isDark 
                ? 'bg-white/5 border-white/10' 
                : 'bg-white border-gray-200 shadow-sm'
            }`}>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Shield className={`size-5 ${isDark ? 'text-white/70' : 'text-gray-600'}`} />
                  <span className={`font-medium ${isDark ? 'text-white' : 'text-gray-900'}`}>Privacy Policy</span>
                </div>
                <ChevronRight className={`size-4 ${isDark ? 'text-white/40' : 'text-gray-400'}`} />
              </div>
            </button>

            <button className={`w-full p-4 rounded-2xl border active:scale-98 transition-transform ${
              isDark 
                ? 'bg-white/5 border-white/10' 
                : 'bg-white border-gray-200 shadow-sm'
            }`}>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <FileText className={`size-5 ${isDark ? 'text-white/70' : 'text-gray-600'}`} />
                  <span className={`font-medium ${isDark ? 'text-white' : 'text-gray-900'}`}>Terms of Service</span>
                </div>
                <ChevronRight className={`size-4 ${isDark ? 'text-white/40' : 'text-gray-400'}`} />
              </div>
            </button>
          </div>
        </section>
      </div>

      {/* Theme Sheet */}
      <ThemeSheet
        isOpen={showThemeSheet}
        onClose={() => setShowThemeSheet(false)}
      />
    </div>
  );
}