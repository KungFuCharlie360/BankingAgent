const wsValue = import.meta.env.VITE_WS_URL ?? "http://localhost:8080/ws";
const websocketUrl = wsValue
  .replace(/^http:/, "ws:")
  .replace(/^https:/, "wss:");
export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
  wsUrl: websocketUrl,
};
