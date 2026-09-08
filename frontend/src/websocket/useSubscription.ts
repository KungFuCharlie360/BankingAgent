import { useEffect } from "react";
import { useStomp } from "./stompClient";
export function useSubscription(
  topic: string | undefined,
  handler: (event: unknown) => void,
) {
  const { subscribe } = useStomp();
  useEffect(
    () => (topic ? subscribe(topic, handler) : undefined),
    [topic, subscribe, handler],
  );
}
