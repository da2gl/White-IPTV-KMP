import { useState } from "react";
import { useNavigate } from "react-router";
import { ChevronLeft, Plus, Edit, Trash2, Play, MoreVertical } from "lucide-react";
import { mockPlaylists, mockChannels } from "../lib/mockData";
import PlaylistDialog from "../components/PlaylistDialog";
import type { Playlist } from "../lib/mockData";
import * as DropdownMenu from "@radix-ui/react-dropdown-menu";
import { useTheme } from "../lib/ThemeContext";

export default function PlaylistManager() {
  const navigate = useNavigate();
  const [showDialog, setShowDialog] = useState(false);
  const [editingPlaylist, setEditingPlaylist] = useState<Playlist | null>(null);
  const { theme } = useTheme();

  const handleAdd = () => {
    setEditingPlaylist(null);
    setShowDialog(true);
  };

  const handleEdit = (playlist: Playlist) => {
    setEditingPlaylist(playlist);
    setShowDialog(true);
  };

  const handleDelete = (id: string) => {
    console.log("Delete playlist:", id);
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
          <div className="flex items-center gap-4 mb-4">
            <button
              onClick={() => navigate(-1)}
              className={`size-10 rounded-xl flex items-center justify-center active:scale-95 transition-transform ${
                isDark ? 'bg-white/5' : 'bg-gray-100'
              }`}
            >
              <ChevronLeft className={`size-5 ${isDark ? 'text-white' : 'text-gray-900'}`} />
            </button>
            <div className="flex-1">
              <h1 className={`text-2xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>Playlists</h1>
              <p className={`text-sm mt-0.5 ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
                Manage your IPTV playlists
              </p>
            </div>
            <button
              onClick={handleAdd}
              className="size-12 rounded-xl bg-gradient-to-r from-[#00d4ff] to-[#0088ff] flex items-center justify-center active:scale-95 transition-transform"
            >
              <Plus className="size-6 text-white" />
            </button>
          </div>
        </div>
      </header>

      {/* Playlists List */}
      <div className="px-4 py-6 space-y-4">
        {mockPlaylists.map((playlist) => {
          const channelsInPlaylist = mockChannels.filter(
            (c) => c.playlistId === playlist.id
          );

          return (
            <div
              key={playlist.id}
              className={`rounded-2xl border overflow-hidden ${
                isDark 
                  ? 'bg-white/5 border-white/10' 
                  : 'bg-white border-gray-200 shadow-sm'
              }`}
            >
              <div className="p-4">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <h3 className={`text-lg font-bold mb-1 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                      {playlist.name}
                    </h3>
                    <p className={`text-sm ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
                      {playlist.channelCount} channels
                    </p>
                  </div>

                  <DropdownMenu.Root>
                    <DropdownMenu.Trigger asChild>
                      <button className="size-10 rounded-xl bg-white/5 flex items-center justify-center active:scale-95 transition-transform">
                        <MoreVertical className="size-5 text-white" />
                      </button>
                    </DropdownMenu.Trigger>

                    <DropdownMenu.Portal>
                      <DropdownMenu.Content
                        className="min-w-[200px] bg-[#0f1419] border border-white/10 rounded-2xl p-2 shadow-2xl z-50"
                        sideOffset={5}
                      >
                        <DropdownMenu.Item
                          onClick={() => handleEdit(playlist)}
                          className="flex items-center gap-3 px-4 py-3 rounded-xl text-white cursor-pointer hover:bg-white/5 focus:outline-none focus:bg-white/5"
                        >
                          <Edit className="size-4 text-[#00d4ff]" />
                          <span>Edit Playlist</span>
                        </DropdownMenu.Item>

                        {playlist.id !== "main" && (
                          <DropdownMenu.Item
                            onClick={() => handleDelete(playlist.id)}
                            className="flex items-center gap-3 px-4 py-3 rounded-xl text-[#ff006e] cursor-pointer hover:bg-white/5 focus:outline-none focus:bg-white/5"
                          >
                            <Trash2 className="size-4" />
                            <span>Delete Playlist</span>
                          </DropdownMenu.Item>
                        )}
                      </DropdownMenu.Content>
                    </DropdownMenu.Portal>
                  </DropdownMenu.Root>
                </div>

                {/* Channel Preview */}
                {channelsInPlaylist.length > 0 && (
                  <div className="grid grid-cols-4 gap-2">
                    {channelsInPlaylist.slice(0, 4).map((channel) => (
                      <div
                        key={channel.id}
                        className="aspect-square rounded-lg bg-white/5 border border-white/10 overflow-hidden"
                      >
                        <img
                          src={channel.logo}
                          alt={channel.name}
                          className="w-full h-full object-cover"
                        />
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Actions */}
              <div className="border-t border-white/10 p-3 flex gap-2">
                <button className="flex-1 py-2.5 rounded-xl bg-white/5 text-white font-medium text-sm active:scale-95 transition-transform">
                  View Channels
                </button>
                <button className="flex-1 py-2.5 rounded-xl bg-gradient-to-r from-[#00d4ff] to-[#0088ff] text-white font-bold text-sm active:scale-95 transition-transform">
                  <div className="flex items-center justify-center gap-2">
                    <Play className="size-4 fill-white" />
                    <span>Play</span>
                  </div>
                </button>
              </div>
            </div>
          );
        })}

        {/* Add Playlist Card */}
        <button
          onClick={handleAdd}
          className="w-full p-8 rounded-2xl border-2 border-dashed border-white/20 bg-white/5 flex flex-col items-center justify-center gap-3 active:scale-98 transition-transform"
        >
          <div className="size-16 rounded-full bg-gradient-to-br from-[#00d4ff]/20 to-[#0088ff]/20 border border-[#00d4ff]/30 flex items-center justify-center">
            <Plus className="size-8 text-[#00d4ff]" />
          </div>
          <div>
            <h3 className="font-bold text-white">Add New Playlist</h3>
            <p className="text-sm text-white/50 mt-1">Import M3U or M3U8</p>
          </div>
        </button>
      </div>

      <PlaylistDialog
        isOpen={showDialog}
        onClose={() => {
          setShowDialog(false);
          setEditingPlaylist(null);
        }}
        playlist={editingPlaylist}
      />
    </div>
  );
}