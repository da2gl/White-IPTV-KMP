import { createBrowserRouter } from "react-router";
import Root from "./Root";
import Home from "./screens/Home";
import AllChannels from "./screens/AllChannels";
import Favorites from "./screens/Favorites";
import Settings from "./screens/Settings";
import Player from "./screens/Player";
import PlaylistManager from "./screens/PlaylistManager";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Root,
    children: [
      { index: true, Component: Home },
      { path: "channels", Component: AllChannels },
      { path: "favorites", Component: Favorites },
      { path: "settings", Component: Settings },
      { path: "playlists", Component: PlaylistManager },
    ],
  },
  {
    path: "/player/:channelId",
    Component: Player,
  },
]);
