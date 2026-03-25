import { useState } from "react";
import { Link } from "react-router";
import { Search, X, Heart } from "lucide-react";
import { mockChannels, categories } from "../lib/mockData";
import { ImageWithFallback } from "../components/figma/ImageWithFallback";
import { useTheme } from "../lib/ThemeContext";

export default function AllChannels() {
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [searchQuery, setSearchQuery] = useState("");
  const [favorites, setFavorites] = useState<Set<string>>(
    new Set(mockChannels.filter((c) => c.isFavorite).map((c) => c.id))
  );
  const { theme } = useTheme();

  const filteredChannels = mockChannels.filter((channel) => {
    const matchesCategory = selectedCategory === "All" || channel.category === selectedCategory;
    const matchesSearch =
      searchQuery === "" ||
      channel.name.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  const toggleFavorite = (channelId: string, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setFavorites((prev) => {
      const next = new Set(prev);
      if (next.has(channelId)) {
        next.delete(channelId);
      } else {
        next.add(channelId);
      }
      return next;
    });
  };

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
          <h1 className={`text-2xl font-bold mb-4 ${isDark ? 'text-white' : 'text-gray-900'}`}>All Channels</h1>
          
          {/* Search */}
          <div className="relative mb-4">
            <Search className={`absolute left-4 top-1/2 -translate-y-1/2 size-5 ${isDark ? 'text-white/40' : 'text-gray-400'}`} />
            <input
              type="text"
              placeholder="Search channels..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className={`w-full pl-12 pr-12 py-3 rounded-2xl border focus:outline-none focus:border-[#00d4ff]/50 focus:ring-2 focus:ring-[#00d4ff]/20 ${
                isDark 
                  ? 'bg-white/5 border-white/10 text-white placeholder:text-white/30' 
                  : 'bg-white border-gray-200 text-gray-900 placeholder:text-gray-400'
              }`}
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery("")}
                className={`absolute right-4 top-1/2 -translate-y-1/2 size-6 rounded-full flex items-center justify-center ${
                  isDark ? 'bg-white/10' : 'bg-gray-200'
                }`}
              >
                <X className={`size-4 ${isDark ? 'text-white/60' : 'text-gray-600'}`} />
              </button>
            )}
          </div>

          {/* Category Filters */}
          <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
            {categories.map((category) => (
              <button
                key={category}
                onClick={() => setSelectedCategory(category)}
                className={`px-4 py-2 rounded-xl text-sm font-medium whitespace-nowrap transition-all ${
                  selectedCategory === category
                    ? "bg-gradient-to-r from-[#00d4ff] to-[#0088ff] text-white"
                    : isDark 
                      ? "bg-white/5 text-white/60 active:scale-95"
                      : "bg-white text-gray-600 border border-gray-200 active:scale-95"
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        </div>
      </header>

      {/* Channels Grid */}
      <div className="px-4 py-6">
        {filteredChannels.length > 0 ? (
          <div className="grid grid-cols-2 gap-4">
            {filteredChannels.map((channel) => (
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

                    {/* Favorite Button */}
                    <button
                      onClick={(e) => toggleFavorite(channel.id, e)}
                      className="absolute top-3 right-3 size-8 rounded-full bg-black/40 backdrop-blur-sm flex items-center justify-center active:scale-95 transition-transform"
                    >
                      <Heart
                        className={`size-4 ${
                          favorites.has(channel.id)
                            ? "text-[#ff006e] fill-[#ff006e]"
                            : "text-white"
                        }`}
                      />
                    </button>

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
                <p className={`text-xs ${isDark ? 'text-white/50' : 'text-gray-500'}`}>{channel.category}</p>
              </Link>
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-20">
            <div className={`size-20 rounded-full flex items-center justify-center mb-4 ${
              isDark ? 'bg-white/5' : 'bg-gray-100'
            }`}>
              <Search className={`size-10 ${isDark ? 'text-white/20' : 'text-gray-300'}`} />
            </div>
            <h3 className={`text-lg font-semibold mb-2 ${isDark ? 'text-white' : 'text-gray-900'}`}>No channels found</h3>
            <p className={`text-center ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
              Try adjusting your search or filters
            </p>
          </div>
        )}
      </div>
    </div>
  );
}