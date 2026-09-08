const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const configuredWebSocketUrl = import.meta.env.VITE_WS_URL;
const websocketUrl = configuredWebSocketUrl
  ? configuredWebSocketUrl.replace(/^http:/, "ws:").replace(/^https:/, "wss:")
  : `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}/ws`;

export const config = {
  apiBaseUrl,
  wsUrl: websocketUrl,
};
