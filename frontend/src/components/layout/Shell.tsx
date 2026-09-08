import { NavLink, Outlet } from "react-router-dom";
import { useStomp } from "../../websocket/stompClient";
export function Shell() {
  const { state } = useStomp();
  return (
    <>
      <header>
        <div>
          <strong>Banking Routing</strong>
          <span className="subtitle">Customer enquiry operations</span>
        </div>
        <nav>
          <NavLink to="/dashboard">Routing Dashboard</NavLink>
          <NavLink to="/customer">Customer</NavLink>
          <NavLink to="/agent">Agent Console</NavLink>
        </nav>
        <span className={`connection ${state.toLowerCase()}`}>● {state}</span>
      </header>
      <main>
        <Outlet />
      </main>
    </>
  );
}
