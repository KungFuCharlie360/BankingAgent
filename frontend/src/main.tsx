import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { StompProvider } from "./websocket/stompClient";
import "./styles.css";
ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <StompProvider>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </StompProvider>
  </React.StrictMode>,
);
