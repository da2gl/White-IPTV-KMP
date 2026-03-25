export interface Channel {
  id: string;
  name: string;
  logo: string;
  category: string;
  playlistId: string;
  isFavorite: boolean;
  streamUrl: string;
  isLive: boolean;
}

export interface Playlist {
  id: string;
  name: string;
  channelCount: number;
  url?: string;
}

export const mockPlaylists: Playlist[] = [
  { id: "main", name: "Main Playlist", channelCount: 45 },
  { id: "sports", name: "Sports HD", channelCount: 12 },
  { id: "movies", name: "Premium Movies", channelCount: 28 },
  { id: "kids", name: "Kids & Family", channelCount: 15 },
];

export const mockChannels: Channel[] = [
  {
    id: "hbo",
    name: "HBO",
    logo: "https://images.unsplash.com/photo-1574267432644-f416f0a8f08c?w=200&h=200&fit=crop",
    category: "Movies",
    playlistId: "main",
    isFavorite: true,
    streamUrl: "",
    isLive: true,
  },
  {
    id: "espn",
    name: "ESPN",
    logo: "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=200&h=200&fit=crop",
    category: "Sports",
    playlistId: "sports",
    isFavorite: true,
    streamUrl: "",
    isLive: true,
  },
  {
    id: "cnn",
    name: "CNN",
    logo: "https://images.unsplash.com/photo-1495020689067-958852a7765e?w=200&h=200&fit=crop",
    category: "News",
    playlistId: "main",
    isFavorite: false,
    streamUrl: "",
    isLive: true,
  },
  {
    id: "discovery",
    name: "Discovery",
    logo: "https://images.unsplash.com/photo-1472214103451-9374bd1c798e?w=200&h=200&fit=crop",
    category: "Documentary",
    playlistId: "main",
    isFavorite: true,
    streamUrl: "",
    isLive: true,
  },
  {
    id: "fox-sports",
    name: "Fox Sports",
    logo: "https://images.unsplash.com/photo-1517466787929-bc90951d0974?w=200&h=200&fit=crop",
    category: "Sports",
    playlistId: "sports",
    isFavorite: false,
    streamUrl: "",
    isLive: true,
  },
  {
    id: "disney",
    name: "Disney Channel",
    logo: "https://images.unsplash.com/photo-1634825503942-95b60f3dbef3?w=200&h=200&fit=crop",
    category: "Kids",
    playlistId: "kids",
    isFavorite: true,
    streamUrl: "",
    isLive: true,
  },
  {
    id: "nat-geo",
    name: "National Geographic",
    logo: "https://images.unsplash.com/photo-1504893524553-b855bce32c67?w=200&h=200&fit=crop",
    category: "Documentary",
    playlistId: "main",
    isFavorite: false,
    streamUrl: "",
    isLive: true,
  },
  {
    id: "nbc-sports",
    name: "NBC Sports",
    logo: "https://images.unsplash.com/photo-1577223625816-7546f23bb060?w=200&h=200&fit=crop",
    category: "Sports",
    playlistId: "sports",
    isFavorite: true,
    streamUrl: "",
    isLive: true,
  },
];

export const categories = ["All", "Movies", "Sports", "News", "Documentary", "Kids"];
