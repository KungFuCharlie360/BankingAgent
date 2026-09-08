import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs";
import {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from "react";
import { config } from "../config";
type State = "CONNECTED" | "CONNECTING" | "DISCONNECTED";
type StompContextValue = {
  state: State;
  subscribe: (topic: string, callback: (event: unknown) => void) => () => void;
};
const C = createContext<StompContextValue>({
  state: "DISCONNECTED",
  subscribe: () => () => {},
});
export function StompProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<State>("CONNECTING");
  const clientRef = useRef<Client>();
  const callbacks = useRef(new Map<string, Set<(event: unknown) => void>>());
  const subscriptions = useRef(new Map<string, StompSubscription>());
  useEffect(() => {
    const client = new Client({
      brokerURL: config.wsUrl,
      reconnectDelay: 3000,
      onConnect: () => {
        setState("CONNECTED");
        callbacks.current.forEach((_, topic) => subscribeTopic(topic));
      },
      onWebSocketClose: () => setState("DISCONNECTED"),
      onStompError: (frame) =>
        console.error("STOMP error", frame.headers["message"], frame.body),
    });
    clientRef.current = client;
    function subscribeTopic(topic: string) {
      if (subscriptions.current.has(topic) || !client.connected) return;
      subscriptions.current.set(
        topic,
        client.subscribe(topic, (message: IMessage) => {
          try {
            const value: unknown = JSON.parse(message.body);
            callbacks.current.get(topic)?.forEach((fn) => fn(value));
          } catch (error) {
            console.error("Invalid STOMP message", error);
          }
        }),
      );
    }
    client.activate();
    return () => {
      subscriptions.current.forEach((s) => s.unsubscribe());
      subscriptions.current.clear();
      client.deactivate();
    };
  }, []);
  const subscribe = (topic: string, callback: (event: unknown) => void) => {
    let set = callbacks.current.get(topic);
    if (!set) {
      set = new Set();
      callbacks.current.set(topic, set);
    }
    set.add(callback);
    const client = clientRef.current;
    if (client?.connected && !subscriptions.current.has(topic)) {
      subscriptions.current.set(
        topic,
        client.subscribe(topic, (m) => {
          const value: unknown = JSON.parse(m.body);
          callbacks.current.get(topic)?.forEach((fn) => fn(value));
        }),
      );
    }
    return () => {
      const listeners = callbacks.current.get(topic);
      listeners?.delete(callback);
      if (!listeners?.size) {
        callbacks.current.delete(topic);
        subscriptions.current.get(topic)?.unsubscribe();
        subscriptions.current.delete(topic);
      }
    };
  };
  return <C.Provider value={{ state, subscribe }}>{children}</C.Provider>;
}
export const useStomp = () => useContext(C);
