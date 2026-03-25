import { X, Plus, Edit, Trash2, List } from "lucide-react";
import { Drawer } from "vaul";
import type { Playlist } from "../lib/mockData";
import { useState } from "react";
import PlaylistDialog from "./PlaylistDialog";
import { useTheme } from "../lib/ThemeContext";

interface PlaylistSheetProps {
  isOpen: boolean;
  onClose: () => void;
  playlists: Playlist[];
  selectedPlaylist?: string;
  onSelectPlaylist?: (id: string) => void;
}

export default function PlaylistSheet({
  isOpen,
  onClose,
  playlists,
  selectedPlaylist,
  onSelectPlaylist,
}: PlaylistSheetProps) {
  const [showDialog, setShowDialog] = useState(false);
  const [editingPlaylist, setEditingPlaylist] = useState<Playlist | null>(null);
  const { theme } = useTheme();

  const handleAdd = () => {
    setEditingPlaylist(null);
    setShowDialog(true);
  };

  const handleEdit = (playlist: Playlist, e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingPlaylist(playlist);
    setShowDialog(true);
  };

  const handleDelete = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    console.log("Delete playlist:", id);
  };

  const isDark = theme === "dark";

  return (
    <>
      <Drawer.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
        <Drawer.Portal>
          <Drawer.Overlay className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50" />
          <Drawer.Content className={`fixed bottom-0 left-0 right-0 z-50 flex flex-col border-t rounded-t-3xl max-h-[85vh] ${
            isDark ? 'bg-[#0f1419] border-white/10' : 'bg-white border-gray-200'
          }`}>
            <Drawer.Title className="sr-only">
              Playlist Manager
            </Drawer.Title>
            <Drawer.Description className="sr-only">
              Manage your IPTV playlists - edit, delete, or add new playlists
            </Drawer.Description>
            <div className="flex-shrink-0">
              <div className={`mx-auto w-12 h-1.5 flex-shrink-0 rounded-full mt-4 mb-6 ${
                isDark ? 'bg-white/20' : 'bg-gray-300'
              }`} />
              
              <div className="flex items-center justify-between px-6 pb-4">
                <div className="flex items-center gap-3">
                  <div className="size-10 rounded-xl bg-gradient-to-br from-[#00d4ff] to-[#0088ff] flex items-center justify-center">
                    <List className="size-5 text-white" />
                  </div>
                  <div>
                    <h2 className={`text-xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>Playlists</h2>
                    <p className={`text-sm ${isDark ? 'text-white/50' : 'text-gray-500'}`}>{playlists.length} playlists</p>
                  </div>
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
                {playlists.map((playlist) => (
                  <div
                    key={playlist.id}
                    onClick={() => onSelectPlaylist?.(playlist.id)}
                    className={`group relative p-4 rounded-2xl border cursor-pointer transition-all ${
                      selectedPlaylist === playlist.id
                        ? "bg-gradient-to-r from-[#00d4ff]/20 to-[#0088ff]/20 border-[#00d4ff]/50"
                        : isDark 
                          ? "bg-white/5 border-white/10 active:scale-98"
                          : "bg-gray-50 border-gray-200 active:scale-98"
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <h3 className={`font-semibold ${isDark ? 'text-white' : 'text-gray-900'}`}>{playlist.name}</h3>
                        <p className={`text-sm mt-0.5 ${isDark ? 'text-white/50' : 'text-gray-500'}`}>
                          {playlist.channelCount} channels
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={(e) => handleEdit(playlist, e)}
                          className={`size-9 rounded-lg flex items-center justify-center transition-all active:scale-95 ${
                            isDark ? 'bg-white/10' : 'bg-gray-200'
                          }`}
                        >
                          <Edit className="size-4 text-[#00d4ff]" />
                        </button>
                        {playlist.id !== "main" && (
                          <button
                            onClick={(e) => handleDelete(playlist.id, e)}
                            className={`size-9 rounded-lg flex items-center justify-center transition-all active:scale-95 ${
                              isDark ? 'bg-white/10' : 'bg-gray-200'
                            }`}
                          >
                            <Trash2 className="size-4 text-[#ff006e]" />
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}

                {/* Add Playlist Button */}
                <button
                  onClick={handleAdd}
                  className={`w-full p-4 rounded-2xl border-2 border-dashed flex items-center justify-center gap-2 active:scale-98 transition-transform ${
                    isDark 
                      ? 'border-white/20 bg-white/5' 
                      : 'border-gray-300 bg-gray-50'
                  }`}
                >
                  <Plus className="size-5 text-[#00d4ff]" />
                  <span className={`font-semibold ${isDark ? 'text-white' : 'text-gray-900'}`}>Add Playlist</span>
                </button>
              </div>
            </div>
          </Drawer.Content>
        </Drawer.Portal>
      </Drawer.Root>

      <PlaylistDialog
        isOpen={showDialog}
        onClose={() => {
          setShowDialog(false);
          setEditingPlaylist(null);
        }}
        playlist={editingPlaylist}
      />
    </>
  );
}