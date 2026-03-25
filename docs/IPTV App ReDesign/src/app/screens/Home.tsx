import { useState } from "react";
import { Link } from "react-router";
import { Search, MoreVertical, Plus, Play, Heart } from "lucide-react";
import { mockChannels, mockPlaylists } from "../lib/mockData";
import PlaylistSheet from "../components/PlaylistSheet";
import { ImageWithFallback } from "../components/figma/ImageWithFallback";
import { useTheme } from "../lib/ThemeContext";

export default function Home() {
  const [selectedPlaylist, setSelectedPlaylist] = useState("main");
  const [showPlaylistSheet, setShowPlaylistSheet] = useState(false);
  const { theme } = useTheme();

  const continueWatching = mockChannels.slice(0, 2);
  const favorites = mockChannels.filter((c) => c.isFavorite);
  const currentPlaylist = mockPlaylists.find((p) => p.id === selectedPlaylist);

  const isDark = theme === "dark";

  return (
    <div className={`min-h-screen pb-4 ${
      isDark 
        ? 'bg-gradient-to-b from-[#0a0f14] to-[#0f1419]' 
        : 'bg-gradient-to-b from-[#f8f9fa] to-[#e9ecef]'
    }`}>
      {/* Header */}
      <header className={`sticky top-0 z-40 backdrop-blur-xl border-b ${
        isDark 
          ? 'bg-[#0a0f14]/80 border-[#1a2026]' 
          : 'bg-white/80 border-gray-200'
      }`}>
        <div className="flex items-center justify-between px-4 py-4">
          <button
            onClick={() => setShowPlaylistSheet(true)}
            className={`flex items-center gap-2 px-3 py-2 rounded-xl bg-gradient-to-r from-[#00d4ff]/10 to-[#0088ff]/10 border border-[#00d4ff]/30 active:scale-95 transition-transform`}
          >
            <span className={`font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>{currentPlaylist?.name}</span>
            <MoreVertical className="size-4 text-[#00d4ff]" />
          </button>
          <div className="flex items-center gap-2">
            <Link
              to="/channels"
              className={`p-2.5 rounded-xl border active:scale-95 transition-transform ${
                isDark 
                  ? 'bg-white/5 border-white/10' 
                  : 'bg-white border-gray-200 shadow-sm'
              }`}
            >
              <Search className={`size-5 ${isDark ? 'text-white' : 'text-gray-900'}`} />
            </Link>
            <Link
              to="/playlists"
              className="p-2.5 rounded-xl bg-gradient-to-r from-[#00d4ff] to-[#0088ff] active:scale-95 transition-transform"
            >
              <Plus className="size-5 text-white" />
            </Link>
          </div>
        </div>
      </header>

      <div className="px-4 pt-6 space-y-8">
        {/* Continue Watching */}
        {continueWatching.length > 0 && (
          <section>
            <h2 className={`text-xl font-bold mb-4 ${isDark ? 'text-white' : 'text-gray-900'}`}>Continue Watching</h2>
            <div className="space-y-4">
              {continueWatching.map((channel, idx) => (
                <Link
                  key={channel.id}
                  to={`/player/${channel.id}`}
                  className="block group"
                >
                  <div className="relative rounded-2xl overflow-hidden bg-gradient-to-br from-[#1a2026] to-[#0f1419] border border-white/10 shadow-2xl">
                    <div className="aspect-video relative">
                      <ImageWithFallback
                        src={channel.logo}
                        alt={channel.name}
                        className="w-full h-full object-cover"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent" />
                      
                      {/* Play Button Overlay */}
                      <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                        <div className="size-16 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center">
                          <Play className="size-8 text-white fill-white ml-1" />
                        </div>
                      </div>

                      {/* Channel Info */}
                      <div className="absolute bottom-0 left-0 right-0 p-4">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="px-2 py-0.5 rounded-full bg-[#00d4ff] text-xs font-bold text-black">
                            LIVE
                          </span>
                          <span className="text-xs text-white/70">{channel.category}</span>
                        </div>
                        <h3 className="text-lg font-bold text-white">{channel.name}</h3>
                        
                        {/* Progress Bar */}
                        <div className="mt-3 h-1 bg-white/20 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-gradient-to-r from-[#00d4ff] to-[#0088ff] rounded-full"
                            style={{ width: idx === 0 ? "65%" : "25%" }}
                          />
                        </div>
                        <p className="text-xs text-white/60 mt-1">
                          {idx === 0 ? "24m left" : "1h 15m left"}
                        </p>
                      </div>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          </section>
        )}

        {/* Favorites */}
        <section>
          <div className="flex items-center justify-between mb-4">
            <h2 className={`text-xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>Favorites</h2>
            <Link
              to="/favorites"
              className="text-sm font-medium text-[#00d4ff] active:opacity-70"
            >
              See all
            </Link>
          </div>
          <div className="grid grid-cols-2 gap-4">
            {favorites.slice(0, 4).map((channel) => (
              <Link
                key={channel.id}
                to={`/player/${channel.id}`}
                className="group"
              >
                <div className="relative rounded-2xl overflow-hidden bg-gradient-to-br from-[#1a2026] to-[#0f1419] border border-white/10 shadow-xl">
                  <div className="aspect-square relative">
                    <ImageWithFallback
                      src={channel.logo}
                      alt={channel.name}
                      className="w-full h-full object-cover"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />
                    
                    {/* Favorite Badge */}
                    <button className="absolute top-3 right-3 size-8 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center">
                      <Heart className="size-4 text-[#ff006e] fill-[#ff006e]" />
                    </button>

                    {/* Live Badge */}
                    <div className="absolute bottom-3 left-3">
                      <span className="px-2 py-1 rounded-full bg-[#00d4ff] text-xs font-bold text-black">
                        LIVE
                      </span>
                    </div>
                  </div>
                </div>
                <h3 className={`mt-2 font-semibold text-sm ${isDark ? 'text-white' : 'text-gray-900'}`}>{channel.name}</h3>
                <p className={`text-xs ${isDark ? 'text-white/50' : 'text-gray-500'}`}>{channel.category}</p>
              </Link>
            ))}
          </div>
        </section>

        {/* Categories */}
        <section>
          <h2 className={`text-xl font-bold mb-4 ${isDark ? 'text-white' : 'text-gray-900'}`}>Browse by Category</h2>
          <div className="grid grid-cols-2 gap-3">
            {["Movies", "Sports", "News", "Kids"].map((category) => (
              <Link
                key={category}
                to={`/channels?category=${category}`}
                className={`p-4 rounded-2xl border active:scale-95 transition-transform ${
                  isDark 
                    ? 'bg-gradient-to-br from-[#1a2026] to-[#0f1419] border-white/10' 
                    : 'bg-white border-gray-200 shadow-sm'
                }`}
              >
                <h3 className={`font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>{category}</h3>
                <p className={`text-xs mt-1 ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
                  {mockChannels.filter((c) => c.category === category).length} channels
                </p>
              </Link>
            ))}
          </div>
        </section>
      </div>

      <PlaylistSheet
        isOpen={showPlaylistSheet}
        onClose={() => setShowPlaylistSheet(false)}
        playlists={mockPlaylists}
        selectedPlaylist={selectedPlaylist}
        onSelectPlaylist={setSelectedPlaylist}
      />
    </div>
  );
}