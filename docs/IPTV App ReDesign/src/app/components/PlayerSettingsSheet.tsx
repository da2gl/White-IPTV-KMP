import { X, Volume2, Sun, Zap, Monitor } from "lucide-react";
import { Drawer } from "vaul";
import { useState } from "react";

interface PlayerSettingsSheetProps {
  isOpen: boolean;
  onClose: () => void;
  volume: number;
  brightness: number;
  onVolumeChange: (value: number) => void;
  onBrightnessChange: (value: number) => void;
}

export default function PlayerSettingsSheet({
  isOpen,
  onClose,
  volume,
  brightness,
  onVolumeChange,
  onBrightnessChange,
}: PlayerSettingsSheetProps) {
  const qualities = ["Auto", "1080p", "720p", "480p", "360p"];
  const sleepTimers = ["Off", "15 min", "30 min", "1 hour", "2 hours"];
  
  const [selectedQuality, setSelectedQuality] = useState("Auto");
  const [selectedSleepTimer, setSelectedSleepTimer] = useState("Off");

  return (
    <Drawer.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <Drawer.Portal>
        <Drawer.Overlay className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50" />
        <Drawer.Content className="fixed bottom-0 left-0 right-0 z-50 flex flex-col bg-[#0f1419] border-t border-white/10 rounded-t-3xl max-h-[85vh]">
          <Drawer.Title className="sr-only">
            Player Settings
          </Drawer.Title>
          <Drawer.Description className="sr-only">
            Adjust playback quality, volume, brightness, and sleep timer settings
          </Drawer.Description>
          <div className="flex-shrink-0">
            <div className="mx-auto w-12 h-1.5 flex-shrink-0 rounded-full bg-white/20 mt-4 mb-6" />
            
            <div className="flex items-center justify-between px-6 pb-4">
              <div>
                <h2 className="text-xl font-bold text-white">Player Settings</h2>
                <p className="text-sm text-white/50 mt-0.5">Adjust playback preferences</p>
              </div>
              <button
                onClick={onClose}
                className="size-10 rounded-xl bg-white/5 flex items-center justify-center active:scale-95 transition-transform"
              >
                <X className="size-5 text-white" />
              </button>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto px-6 pb-6 space-y-6">
            {/* Volume Control */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="size-10 rounded-xl bg-gradient-to-br from-[#00d4ff]/20 to-[#0088ff]/20 border border-[#00d4ff]/30 flex items-center justify-center">
                    <Volume2 className="size-5 text-[#00d4ff]" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-white">Volume</h3>
                    <p className="text-sm text-white/50">{volume}%</p>
                  </div>
                </div>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                value={volume}
                onChange={(e) => onVolumeChange(Number(e.target.value))}
                className="w-full h-2 bg-white/10 rounded-full appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:size-4 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-gradient-to-r [&::-webkit-slider-thumb]:from-[#00d4ff] [&::-webkit-slider-thumb]:to-[#0088ff]"
              />
            </div>

            {/* Brightness Control */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="size-10 rounded-xl bg-gradient-to-br from-[#fbbf24]/20 to-[#f59e0b]/20 border border-[#fbbf24]/30 flex items-center justify-center">
                    <Sun className="size-5 text-[#fbbf24]" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-white">Brightness</h3>
                    <p className="text-sm text-white/50">{brightness}%</p>
                  </div>
                </div>
              </div>
              <input
                type="range"
                min="30"
                max="100"
                value={brightness}
                onChange={(e) => onBrightnessChange(Number(e.target.value))}
                className="w-full h-2 bg-white/10 rounded-full appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:size-4 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-gradient-to-r [&::-webkit-slider-thumb]:from-[#fbbf24] [&::-webkit-slider-thumb]:to-[#f59e0b]"
              />
            </div>

            <div className="h-px bg-white/10" />

            {/* Quality Selection */}
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <div className="size-10 rounded-xl bg-gradient-to-br from-[#8b5cf6]/20 to-[#7c3aed]/20 border border-[#8b5cf6]/30 flex items-center justify-center">
                  <Monitor className="size-5 text-[#8b5cf6]" />
                </div>
                <div>
                  <h3 className="font-semibold text-white">Quality</h3>
                  <p className="text-sm text-white/50">Stream quality</p>
                </div>
              </div>
              <div className="grid grid-cols-5 gap-2">
                {qualities.map((quality) => (
                  <button
                    key={quality}
                    className={`py-2 rounded-xl text-sm font-medium transition-all ${
                      selectedQuality === quality
                        ? "bg-gradient-to-r from-[#8b5cf6] to-[#7c3aed] text-white"
                        : "bg-white/5 text-white/50 active:scale-95"
                    }`}
                    onClick={() => setSelectedQuality(quality)}
                  >
                    {quality}
                  </button>
                ))}
              </div>
            </div>

            {/* Sleep Timer */}
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <div className="size-10 rounded-xl bg-gradient-to-br from-[#10b981]/20 to-[#059669]/20 border border-[#10b981]/30 flex items-center justify-center">
                  <Zap className="size-5 text-[#10b981]" />
                </div>
                <div>
                  <h3 className="font-semibold text-white">Sleep Timer</h3>
                  <p className="text-sm text-white/50">Auto stop playback</p>
                </div>
              </div>
              <div className="grid grid-cols-5 gap-2">
                {sleepTimers.map((timer) => (
                  <button
                    key={timer}
                    className={`py-2 rounded-xl text-xs font-medium transition-all ${
                      selectedSleepTimer === timer
                        ? "bg-gradient-to-r from-[#10b981] to-[#059669] text-white"
                        : "bg-white/5 text-white/50 active:scale-95"
                    }`}
                    onClick={() => setSelectedSleepTimer(timer)}
                  >
                    {timer}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </Drawer.Content>
      </Drawer.Portal>
    </Drawer.Root>
  );
}