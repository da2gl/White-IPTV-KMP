import { useState, useEffect } from "react";
import { X, Link as LinkIcon, Upload } from "lucide-react";
import * as Dialog from "@radix-ui/react-dialog";
import type { Playlist } from "../lib/mockData";

interface PlaylistDialogProps {
  isOpen: boolean;
  onClose: () => void;
  playlist?: Playlist | null;
}

export default function PlaylistDialog({ isOpen, onClose, playlist }: PlaylistDialogProps) {
  const [name, setName] = useState("");
  const [url, setUrl] = useState("");
  const [method, setMethod] = useState<"url" | "file">("url");

  useEffect(() => {
    if (playlist) {
      setName(playlist.name);
      setUrl(playlist.url || "");
    } else {
      setName("");
      setUrl("");
    }
  }, [playlist]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // In a real app, this would call an API
    console.log("Save playlist:", { name, url, method });
    onClose();
  };

  return (
    <Dialog.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 animate-in fade-in" />
        <Dialog.Content className="fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 z-50 w-[90vw] max-w-md bg-[#0f1419] rounded-3xl border border-white/10 shadow-2xl animate-in fade-in zoom-in">
          <Dialog.Title className="sr-only">
            {playlist ? "Edit Playlist" : "Add Playlist"}
          </Dialog.Title>
          <Dialog.Description className="sr-only">
            {playlist ? "Edit playlist name and settings" : "Add a new IPTV playlist by URL or file upload"}
          </Dialog.Description>
          <form onSubmit={handleSubmit} className="p-6 space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold text-white">
                {playlist ? "Edit Playlist" : "Add Playlist"}
              </h2>
              <Dialog.Close className="size-10 rounded-xl bg-white/5 flex items-center justify-center active:scale-95 transition-transform">
                <X className="size-5 text-white" />
              </Dialog.Close>
            </div>

            {/* Name Input */}
            <div className="space-y-2">
              <label className="block text-sm font-medium text-white/70">
                Playlist Name
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="My IPTV Playlist"
                className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder:text-white/30 focus:outline-none focus:border-[#00d4ff]/50 focus:ring-2 focus:ring-[#00d4ff]/20"
                required
              />
            </div>

            {!playlist && (
              <>
                {/* Method Selector */}
                <div className="flex gap-2 p-1 rounded-xl bg-white/5">
                  <button
                    type="button"
                    onClick={() => setMethod("url")}
                    className={`flex-1 py-2 rounded-lg text-sm font-medium transition-all ${
                      method === "url"
                        ? "bg-gradient-to-r from-[#00d4ff] to-[#0088ff] text-white"
                        : "text-white/50"
                    }`}
                  >
                    <div className="flex items-center justify-center gap-2">
                      <LinkIcon className="size-4" />
                      URL
                    </div>
                  </button>
                  <button
                    type="button"
                    onClick={() => setMethod("file")}
                    className={`flex-1 py-2 rounded-lg text-sm font-medium transition-all ${
                      method === "file"
                        ? "bg-gradient-to-r from-[#00d4ff] to-[#0088ff] text-white"
                        : "text-white/50"
                    }`}
                  >
                    <div className="flex items-center justify-center gap-2">
                      <Upload className="size-4" />
                      File
                    </div>
                  </button>
                </div>

                {/* URL/File Input */}
                {method === "url" ? (
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-white/70">
                      Playlist URL (M3U, M3U8)
                    </label>
                    <input
                      type="url"
                      value={url}
                      onChange={(e) => setUrl(e.target.value)}
                      placeholder="https://example.com/playlist.m3u8"
                      className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white placeholder:text-white/30 focus:outline-none focus:border-[#00d4ff]/50 focus:ring-2 focus:ring-[#00d4ff]/20"
                      required
                    />
                  </div>
                ) : (
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-white/70">
                      Choose File
                    </label>
                    <div className="relative">
                      <input
                        type="file"
                        accept=".m3u,.m3u8"
                        className="absolute inset-0 opacity-0 cursor-pointer"
                      />
                      <div className="px-4 py-3 rounded-xl bg-white/5 border border-dashed border-white/20 text-center">
                        <Upload className="size-8 text-[#00d4ff] mx-auto mb-2" />
                        <p className="text-sm text-white/70">
                          Click to upload M3U/M3U8 file
                        </p>
                      </div>
                    </div>
                  </div>
                )}
              </>
            )}

            {/* Actions */}
            <div className="flex gap-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="flex-1 py-3 rounded-xl bg-white/5 text-white font-medium active:scale-95 transition-transform"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="flex-1 py-3 rounded-xl bg-gradient-to-r from-[#00d4ff] to-[#0088ff] text-white font-bold active:scale-95 transition-transform"
              >
                {playlist ? "Save" : "Add"}
              </button>
            </div>
          </form>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}