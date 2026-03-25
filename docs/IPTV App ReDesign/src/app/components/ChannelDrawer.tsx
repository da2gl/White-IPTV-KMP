import { useNavigate } from "react-router";
import { X, Play } from "lucide-react";
import { Drawer } from "vaul";
import type { Channel } from "../lib/mockData";
import { ImageWithFallback } from "./figma/ImageWithFallback";

interface ChannelDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  channels: Channel[];
  currentChannelId: string;
}

export default function ChannelDrawer({
  isOpen,
  onClose,
  channels,
  currentChannelId,
}: ChannelDrawerProps) {
  const navigate = useNavigate();

  const handleSelectChannel = (channelId: string) => {
    navigate(`/player/${channelId}`);
    onClose();
  };

  return (
    <Drawer.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <Drawer.Portal>
        <Drawer.Overlay className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50" />
        <Drawer.Content className="fixed bottom-0 left-0 right-0 z-50 flex flex-col bg-[#0f1419] border-t border-white/10 rounded-t-3xl max-h-[80vh]">
          <Drawer.Title className="sr-only">
            Channel Switcher
          </Drawer.Title>
          <Drawer.Description className="sr-only">
            Switch between available channels
          </Drawer.Description>
          <div className="flex-shrink-0">
            <div className="mx-auto w-12 h-1.5 flex-shrink-0 rounded-full bg-white/20 mt-4 mb-6" />
            
            <div className="flex items-center justify-between px-6 pb-4">
              <div>
                <h2 className="text-xl font-bold text-white">Switch Channel</h2>
                <p className="text-sm text-white/50 mt-0.5">{channels.length} channels</p>
              </div>
              <button
                onClick={onClose}
                className="size-10 rounded-xl bg-white/5 flex items-center justify-center active:scale-95 transition-transform"
              >
                <X className="size-5 text-white" />
              </button>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto px-6 pb-6">
            <div className="grid grid-cols-2 gap-3">
              {channels.map((channel) => (
                <button
                  key={channel.id}
                  onClick={() => handleSelectChannel(channel.id)}
                  className={`group relative rounded-2xl overflow-hidden border transition-all ${
                    currentChannelId === channel.id
                      ? "border-[#00d4ff] ring-2 ring-[#00d4ff]/20"
                      : "border-white/10 active:scale-95"
                  }`}
                >
                  <div className="aspect-square relative">
                    <ImageWithFallback
                      src={channel.logo}
                      alt={channel.name}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />
                    
                    {/* Play Icon on Hover */}
                    {currentChannelId !== channel.id && (
                      <div className="absolute inset-0 flex items-center justify-center opacity-0 group-active:opacity-100 transition-opacity">
                        <div className="size-12 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center">
                          <Play className="size-6 text-white fill-white ml-0.5" />
                        </div>
                      </div>
                    )}

                    {/* Current Badge */}
                    {currentChannelId === channel.id && (
                      <div className="absolute inset-0 flex items-center justify-center">
                        <div className="px-3 py-1.5 rounded-full bg-[#00d4ff] text-xs font-bold text-black">
                          PLAYING
                        </div>
                      </div>
                    )}

                    {/* Channel Info */}
                    <div className="absolute bottom-0 left-0 right-0 p-3">
                      <div className="flex items-center gap-1.5 mb-1">
                        {channel.isLive && (
                          <span className="px-1.5 py-0.5 rounded-full bg-[#ff006e] text-[10px] font-bold text-white">
                            LIVE
                          </span>
                        )}
                        <span className="text-[10px] text-white/60">{channel.category}</span>
                      </div>
                      <h3 className="text-sm font-semibold text-white line-clamp-1">
                        {channel.name}
                      </h3>
                    </div>
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