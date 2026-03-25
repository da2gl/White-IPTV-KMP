import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router";
import {
  ChevronLeft,
  Play,
  Pause,
  Volume2,
  VolumeX,
  Sun,
  Moon,
  Settings,
  Maximize,
  PictureInPicture,
  Timer,
  Airplay,
  SkipBack,
  SkipForward,
} from "lucide-react";
import { mockChannels } from "../lib/mockData";
import { ImageWithFallback } from "../components/figma/ImageWithFallback";
import PlayerControls from "../components/PlayerControls";
import ChannelDrawer from "../components/ChannelDrawer";
import PlayerSettingsSheet from "../components/PlayerSettingsSheet";

export default function Player() {
  const { channelId } = useParams();
  const navigate = useNavigate();
  const [showControls, setShowControls] = useState(true);
  const [isPlaying, setIsPlaying] = useState(true);
  const [isMuted, setIsMuted] = useState(false);
  const [volume, setVolume] = useState(80);
  const [brightness, setBrightness] = useState(100);
  const [showChannels, setShowChannels] = useState(false);
  const [showSettings, setShowSettings] = useState(false);

  const channel = mockChannels.find((c) => c.id === channelId);
  const currentIndex = mockChannels.findIndex((c) => c.id === channelId);

  useEffect(() => {
    if (!showControls) return;
    const timer = setTimeout(() => setShowControls(false), 4000);
    return () => clearTimeout(timer);
  }, [showControls]);

  if (!channel) {
    return (
      <div className="flex items-center justify-center h-screen bg-black text-white">
        Channel not found
      </div>
    );
  }

  const handlePrevChannel = () => {
    const prevIndex = currentIndex > 0 ? currentIndex - 1 : mockChannels.length - 1;
    navigate(`/player/${mockChannels[prevIndex].id}`);
  };

  const handleNextChannel = () => {
    const nextIndex = currentIndex < mockChannels.length - 1 ? currentIndex + 1 : 0;
    navigate(`/player/${mockChannels[nextIndex].id}`);
  };

  const handlePiP = async () => {
    // Picture-in-picture logic would go here
    console.log("Enable PiP");
  };

  const handleAirplay = () => {
    // Airplay logic would go here
    console.log("Open Airplay");
  };

  const handleSleepTimer = () => {
    // Sleep timer logic would go here
    console.log("Set sleep timer");
  };

  return (
    <div
      className="relative h-screen bg-black overflow-hidden"
      onClick={() => setShowControls(!showControls)}
      style={{ filter: `brightness(${brightness}%)` }}
    >
      {/* Video Player Area */}
      <div className="absolute inset-0 flex items-center justify-center">
        <ImageWithFallback
          src={channel.logo}
          alt={channel.name}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-black/60" />
      </div>

      {/* Controls Overlay */}
      <div
        className={`absolute inset-0 transition-opacity duration-300 ${
          showControls ? "opacity-100" : "opacity-0 pointer-events-none"
        }`}
      >
        {/* Top Bar */}
        <div className="absolute top-0 left-0 right-0 bg-gradient-to-b from-black/80 to-transparent">
          <div className="flex items-center justify-between p-4 safe-area-top">
            <button
              onClick={(e) => {
                e.stopPropagation();
                navigate(-1);
              }}
              className="size-12 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
            >
              <ChevronLeft className="size-6 text-white" />
            </button>

            <div className="flex-1 mx-4">
              <div className="flex items-center gap-2">
                <span className="px-2 py-1 rounded-full bg-[#ff006e] text-xs font-bold text-white">
                  LIVE
                </span>
                <h1 className="text-white font-bold text-lg">{channel.name}</h1>
              </div>
              <p className="text-white/70 text-sm mt-0.5">{channel.category}</p>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleAirplay();
                }}
                className="size-12 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
              >
                <Airplay className="size-5 text-white" />
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowSettings(true);
                }}
                className="size-12 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
              >
                <Settings className="size-5 text-white" />
              </button>
            </div>
          </div>
        </div>

        {/* Center Controls */}
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="flex items-center gap-12">
            <button
              onClick={(e) => {
                e.stopPropagation();
                handlePrevChannel();
              }}
              className="size-16 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
            >
              <SkipBack className="size-8 text-white fill-white" />
            </button>

            <button
              onClick={(e) => {
                e.stopPropagation();
                setIsPlaying(!isPlaying);
              }}
              className="size-20 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
            >
              {isPlaying ? (
                <Pause className="size-10 text-white fill-white" />
              ) : (
                <Play className="size-10 text-white fill-white ml-1" />
              )}
            </button>

            <button
              onClick={(e) => {
                e.stopPropagation();
                handleNextChannel();
              }}
              className="size-16 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
            >
              <SkipForward className="size-8 text-white fill-white" />
            </button>
          </div>
        </div>

        {/* Bottom Controls */}
        <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent pb-8 safe-area-bottom">
          <div className="px-4 space-y-4">
            {/* Quick Actions */}
            <div className="flex items-center justify-between">
              {/* Volume Control */}
              <div className="flex items-center gap-3">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setIsMuted(!isMuted);
                  }}
                  className="size-12 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
                >
                  {isMuted || volume === 0 ? (
                    <VolumeX className="size-5 text-white" />
                  ) : (
                    <Volume2 className="size-5 text-white" />
                  )}
                </button>
                <input
                  type="range"
                  min="0"
                  max="100"
                  value={isMuted ? 0 : volume}
                  onChange={(e) => {
                    e.stopPropagation();
                    setVolume(Number(e.target.value));
                    setIsMuted(false);
                  }}
                  onClick={(e) => e.stopPropagation()}
                  className="w-24 h-1 bg-white/20 rounded-full appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:size-3 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-white"
                />
              </div>

              {/* Brightness Control */}
              <div className="flex items-center gap-3">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setBrightness(brightness === 100 ? 50 : 100);
                  }}
                  className="size-12 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
                >
                  {brightness === 100 ? (
                    <Sun className="size-5 text-white" />
                  ) : (
                    <Moon className="size-5 text-white" />
                  )}
                </button>
                <input
                  type="range"
                  min="30"
                  max="100"
                  value={brightness}
                  onChange={(e) => {
                    e.stopPropagation();
                    setBrightness(Number(e.target.value));
                  }}
                  onClick={(e) => e.stopPropagation()}
                  className="w-24 h-1 bg-white/20 rounded-full appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:size-3 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-white"
                />
              </div>
            </div>

            {/* Additional Controls */}
            <div className="flex items-center justify-center gap-4">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handlePiP();
                }}
                className="flex items-center gap-2 px-4 py-2.5 rounded-full bg-black/40 backdrop-blur-sm active:scale-95 transition-transform"
              >
                <PictureInPicture className="size-4 text-white" />
                <span className="text-sm font-medium text-white">PiP</span>
              </button>

              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleSleepTimer();
                }}
                className="flex items-center gap-2 px-4 py-2.5 rounded-full bg-black/40 backdrop-blur-sm active:scale-95 transition-transform"
              >
                <Timer className="size-4 text-white" />
                <span className="text-sm font-medium text-white">Sleep</span>
              </button>

              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowChannels(true);
                }}
                className="flex items-center gap-2 px-4 py-2.5 rounded-full bg-gradient-to-r from-[#00d4ff] to-[#0088ff] active:scale-95 transition-transform"
              >
                <span className="text-sm font-bold text-white">Channels</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <ChannelDrawer
        isOpen={showChannels}
        onClose={() => setShowChannels(false)}
        channels={mockChannels}
        currentChannelId={channelId!}
      />

      <PlayerSettingsSheet
        isOpen={showSettings}
        onClose={() => setShowSettings(false)}
        volume={volume}
        brightness={brightness}
        onVolumeChange={setVolume}
        onBrightnessChange={setBrightness}
      />
    </div>
  );
}
