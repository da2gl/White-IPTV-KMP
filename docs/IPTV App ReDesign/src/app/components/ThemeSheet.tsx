import { X, Sun, Moon, Check } from "lucide-react";
import { Drawer } from "vaul";
import { useTheme } from "../lib/ThemeContext";

interface ThemeSheetProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function ThemeSheet({ isOpen, onClose }: ThemeSheetProps) {
  const { theme, setTheme } = useTheme();

  const themes = [
    {
      id: "dark" as const,
      name: "Dark",
      icon: Moon,
      description: "Dark background for better viewing",
      gradient: "from-[#1e293b] to-[#0f172a]",
    },
    {
      id: "light" as const,
      name: "Light",
      icon: Sun,
      description: "Light background for daytime use",
      gradient: "from-[#f1f5f9] to-[#e2e8f0]",
    },
  ];

  const handleSelectTheme = (themeId: "light" | "dark") => {
    setTheme(themeId);
    setTimeout(() => onClose(), 300);
  };

  const isDark = theme === "dark";

  return (
    <Drawer.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <Drawer.Portal>
        <Drawer.Overlay className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50" />
        <Drawer.Content className={`fixed bottom-0 left-0 right-0 z-50 flex flex-col border-t rounded-t-3xl max-h-[60vh] ${
          isDark ? 'bg-[#0f1419] border-white/10' : 'bg-white border-gray-200'
        }`}>
          <Drawer.Title className="sr-only">
            Theme Selection
          </Drawer.Title>
          <Drawer.Description className="sr-only">
            Choose between light and dark theme for the app
          </Drawer.Description>
          <div className="flex-shrink-0">
            <div className={`mx-auto w-12 h-1.5 flex-shrink-0 rounded-full mt-4 mb-6 ${
              isDark ? 'bg-white/20' : 'bg-gray-300'
            }`} />
            
            <div className="flex items-center justify-between px-6 pb-4">
              <div>
                <h2 className={`text-xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>Theme</h2>
                <p className={`text-sm mt-0.5 ${isDark ? 'text-white/50' : 'text-gray-500'}`}>Choose your theme</p>
              </div>
              <button
                onClick={onClose}
                className={`size-10 rounded-xl flex items-center justify-center active:scale-95 transition-transform ${
                  isDark ? 'bg-white/5' : 'bg-gray-100'
                }`}
              >
                <X className={`size-5 ${isDark ? 'text-white' : 'text-gray-900'}`} />
              </button>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto px-6 pb-6">
            <div className="space-y-3">
              {themes.map((themeOption) => (
                <button
                  key={themeOption.id}
                  onClick={() => handleSelectTheme(themeOption.id)}
                  className={`group w-full p-4 rounded-2xl border transition-all ${
                    theme === themeOption.id
                      ? "bg-gradient-to-r from-[#00d4ff]/20 to-[#0088ff]/20 border-[#00d4ff]/50"
                      : isDark 
                        ? "bg-white/5 border-white/10 active:scale-98"
                        : "bg-gray-50 border-gray-200 active:scale-98"
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div
                        className={`size-14 rounded-xl bg-gradient-to-br ${themeOption.gradient} border border-white/20 flex items-center justify-center`}
                      >
                        <themeOption.icon className="size-6 text-white" />
                      </div>
                      <div className="text-left">
                        <h3 className={`font-semibold text-lg ${isDark ? 'text-white' : 'text-gray-900'}`}>
                          {themeOption.name}
                        </h3>
                        <p className={`text-sm mt-0.5 ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
                          {themeOption.description}
                        </p>
                      </div>
                    </div>
                    {theme === themeOption.id && (
                      <div className="size-6 rounded-full bg-[#00d4ff] flex items-center justify-center">
                        <Check className="size-4 text-black" />
                      </div>
                    )}
                  </div>
                </button>
              ))}
            </div>
          </div>
        </Drawer.Content>
      </Drawer.Portal>
    </Drawer.Root>
  );
}