import { Link, useLocation } from "react-router";
import { Home, Grid3x3, Heart, Settings } from "lucide-react";
import { useTheme } from "../lib/ThemeContext";

export default function BottomNav() {
  const location = useLocation();
  const { theme } = useTheme();

  const navItems = [
    { path: "/", icon: Home, label: "Home" },
    { path: "/channels", icon: Grid3x3, label: "Channels" },
    { path: "/favorites", icon: Heart, label: "Favorites" },
    { path: "/settings", icon: Settings, label: "Settings" },
  ];

  const isDark = theme === "dark";

  return (
    <nav className={`border-t backdrop-blur-xl safe-area-bottom ${
      isDark 
        ? 'bg-[#0f1419] border-[#1a2026]' 
        : 'bg-white border-gray-200'
    }`}>
      <div className="flex items-center justify-around px-2 py-3 max-w-[600px] mx-auto">
        {navItems.map(({ path, icon: Icon, label }) => {
          const isActive = path === "/" ? location.pathname === path : location.pathname.startsWith(path);
          
          return (
            <Link
              key={path}
              to={path}
              className={`flex flex-col items-center justify-center gap-1 px-4 py-2 rounded-xl transition-all ${
                isActive
                  ? "text-[#00d4ff]"
                  : isDark 
                    ? "text-[#6b7280] active:scale-95"
                    : "text-gray-400 active:scale-95"
              }`}
            >
              <Icon className={`size-6 ${isActive ? "fill-[#00d4ff]/20" : ""}`} strokeWidth={isActive ? 2.5 : 2} />
              <span className={`text-xs ${isActive ? "font-semibold" : "font-medium"}`}>
                {label}
              </span>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}