import { useState } from "react";
import { Link } from "react-router";
import { Heart } from "lucide-react";
import { mockChannels, mockPlaylists } from "../lib/mockData";
import { ImageWithFallback } from "../components/figma/ImageWithFallback";
import { useTheme } from "../lib/ThemeContext";

export default function Favorites() {
  const [selectedPlaylist, setSelectedPlaylist] = useState<string | null>(null);
  const { theme } = useTheme();

  const favoriteChannels = mockChannels.filter((c) => c.isFavorite);
  const filteredFavorites = selectedPlaylist
    ? favoriteChannels.filter((c) => c.playlistId === selectedPlaylist)
    : favoriteChannels;

  const isDark = theme === "dark";

  return (
    <div className={`min-h-screen ${
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
        <div className="px-4 py-4">
          <div className="flex items-center gap-2 mb-4">
            <Heart className="size-6 text-[#ff006e] fill-[#ff006e]" />
            <h1 className={`text-2xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>Favorites</h1>
          </div>

          {/* Playlist Filter */}
          <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
            <button
              onClick={() => setSelectedPlaylist(null)}
              className={`px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap transition-all ${
                selectedPlaylist === null
                  ? "bg-gradient-to-r from-[#ff006e] to-[#d41359] text-white"
                  : isDark 
                    ? "bg-white/5 text-white/60 active:scale-95"
                    : "bg-white text-gray-600 border border-gray-200 active:scale-95"
              }`}
            >
              All Playlists
            </button>
            {mockPlaylists.map((playlist) => (
              <button
                key={playlist.id}
                onClick={() => setSelectedPlaylist(playlist.id)}
                className={`px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap transition-all ${
                  selectedPlaylist === playlist.id
                    ? "bg-gradient-to-r from-[#00d4ff] to-[#0088ff] text-white"
                    : isDark 
                      ? "bg-white/5 text-white/60 active:scale-95"
                      : "bg-white text-gray-600 border border-gray-200 active:scale-95"
                }`}
              >
                {playlist.name}
              </button>
            ))}
          </div>
        </div>
      </header>

      {/* Favorites Grid */}
      <div className="px-4 py-6">
        {filteredFavorites.length > 0 ? (
          <>
            <p className={`text-sm mb-4 ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
              {filteredFavorites.length} favorite{filteredFavorites.length !== 1 ? "s" : ""}
              {selectedPlaylist && ` from ${mockPlaylists.find((p) => p.id === selectedPlaylist)?.name}`}
            </p>
            <div className="grid grid-cols-2 gap-4">
              {filteredFavorites.map((channel) => (
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
                      <div className="absolute top-3 right-3 size-8 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center">
                        <Heart className="size-4 text-[#ff006e] fill-[#ff006e]" />
                      </div>

                      {/* Live Badge */}
                      {channel.isLive && (
                        <div className="absolute bottom-3 left-3">
                          <span className="px-2 py-1 rounded-full bg-[#00d4ff] text-xs font-bold text-black">
                            LIVE
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                  <h3 className={`mt-2 font-semibold text-sm line-clamp-1 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                    {channel.name}
                  </h3>
                  <p className={`text-xs ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
                    {mockPlaylists.find((p) => p.id === channel.playlistId)?.name || channel.category}
                  </p>
                </Link>
              ))}
            </div>
          </>
        ) : (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="size-20 rounded-full bg-gradient-to-br from-[#ff006e]/20 to-[#d41359]/20 border border-[#ff006e]/30 flex items-center justify-center mb-4">
              <Heart className="size-10 text-[#ff006e]" />
            </div>
            <h3 className={`text-lg font-semibold mb-2 ${isDark ? 'text-white' : 'text-gray-900'}`}>No favorites yet</h3>
            <p className={`text-center max-w-xs ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
              {selectedPlaylist
                ? "No favorite channels in this playlist"
                : "Start adding channels to your favorites"}
            </p>
            {selectedPlaylist && (
              <button
                onClick={() => setSelectedPlaylist(null)}
                className={`mt-4 px-4 py-2 rounded-xl text-sm active:scale-95 transition-transform ${
                  isDark ? 'bg-white/5 text-white/70' : 'bg-gray-100 text-gray-700'
                }`}
              >
                View all favorites
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}