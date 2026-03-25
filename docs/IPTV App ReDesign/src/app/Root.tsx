import { Outlet, useLocation } from "react-router";
import BottomNav from "./components/BottomNav";

export default function Root() {
  const location = useLocation();
  const hideNav = location.pathname.startsWith("/player");

  return (
    <div className="flex flex-col h-screen bg-[#0a0f14] text-white overflow-hidden">
      <div className="flex-1 overflow-auto">
        <Outlet />
      </div>
      {!hideNav && <BottomNav />}
    </div>
  );
}
