import { useCallback, useEffect, useState } from "react";
import { agentsApi, enquiriesApi, simulationApi } from "../api/apis";
import type {
  Agent,
  Enquiry,
  RoutingEvent,
  SimulationDashboard,
} from "../types/models";
import { AgentCard } from "../components/agents/AgentCard";
import { useSubscription } from "../websocket/useSubscription";
const pretty = (v: string) => v.replaceAll("_", " ");
const eventText = (e: RoutingEvent) => {
  const id = e.enquiryId?.slice(0, 8);
  if (e.eventType === "ENQUIRY_ASSIGNED")
    return `${id} → ${String(e.payload.agentName ?? "agent")}`;
  if (e.eventType === "ENQUIRY_PENDING") return `${id} added to pending queue`;
  return `${pretty(e.eventType)}${id ? ` · ${id}` : ""}`;
};
export function DashboardPage() {
  const [agents, setAgents] = useState<Agent[]>([]),
    [enquiries, setEnquiries] = useState<Enquiry[]>([]),
    [sim, setSim] = useState<SimulationDashboard>(),
    [events, setEvents] = useState<RoutingEvent[]>([]),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false),
    [batch, setBatch] = useState(10),
    [now, setNow] = useState(Date.now());
  const refresh = useCallback(async () => {
    try {
      const [a, e, s] = await Promise.all([
        agentsApi.all(),
        enquiriesApi.all(),
        simulationApi.dashboard(),
      ]);
      setAgents(a);
      setEnquiries(e);
      setSim(s);
      setError("");
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    refresh();
    const id = window.setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, [refresh]);
  const onEvent = useCallback(
    (raw: unknown) => {
      const event = raw as RoutingEvent;
      setEvents((old) => [event, ...old].slice(0, 100));
      refresh();
    },
    [refresh],
  );
  useSubscription("/topic/routing", onEvent);
  async function command(action: () => Promise<unknown>) {
    setBusy(true);
    try {
      await action();
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }
  const pending = enquiries
    .filter((e) => e.status === "PENDING")
    .sort((a, b) => Date.parse(a.createdAt) - Date.parse(b.createdAt));
  const active = enquiries.filter((e) => e.status !== "CLOSED").length;
  if (loading) return <p>Loading routing dashboard…</p>;
  return (
    <>
      <div className="page-title">
        <div>
          <h1>Routing Dashboard</h1>
          <p>Live capacity, routing, and pending-enquiry visibility.</p>
        </div>
        <span className={`badge ${sim?.running ? "online" : "offline"}`}>
          {sim?.running ? "RUNNING" : "STOPPED"}
        </span>
      </div>
      {error && <p className="error">{error}</p>}
      <section className="controls">
        <button disabled={busy} onClick={() => command(simulationApi.start)}>
          Start Simulation
        </button>
        <button disabled={busy} onClick={() => command(simulationApi.stop)}>
          Stop Simulation
        </button>
        <button disabled={busy} onClick={() => command(simulationApi.generate)}>
          Generate Enquiry
        </button>
        <input
          type="number"
          min="1"
          max="500"
          value={batch}
          onChange={(e) => setBatch(Number(e.target.value))}
        />
        <button
          disabled={busy}
          onClick={() => command(() => simulationApi.batch(batch))}
        >
          Generate Batch
        </button>
        <button
          className="danger"
          disabled={busy}
          onClick={() => command(simulationApi.reset)}
        >
          Reset Simulation
        </button>
      </section>
      <section className="summary">
        {[
          ["Online Agents", agents.filter((a) => a.status === "ONLINE").length],
          ["Active Enquiries", active],
          ["Pending Enquiries", pending.length],
          [
            "Completed Enquiries",
            enquiries.filter((e) => e.status === "CLOSED").length,
          ],
          [
            "Total Available Capacity",
            agents.reduce(
              (n, a) => n + Math.max(0, a.maxCapacity - a.activeEnquiryCount),
              0,
            ),
          ],
        ].map(([label, value]) => (
          <div className="card" key={String(label)}>
            <span>{label}</span>
            <strong>{value}</strong>
          </div>
        ))}
      </section>
      <section className="dashboard-grid">
        <div>
          <h2>Agents</h2>
          <div className="agents">
            {agents.map((a) => (
              <AgentCard
                key={a.id}
                agent={a}
                enquiries={enquiries}
                states={sim?.simulatedEnquiries ?? []}
                now={now}
              />
            ))}
          </div>
        </div>
        <aside>
          <section className="panel">
            <h2>Pending Queue</h2>
            {pending.map((e) => (
              <div className="queue" key={e.enquiryId}>
                <b>{e.enquiryId.slice(0, 8)}</b>
                <span>
                  {pretty(e.category)} · {pretty(e.preferredLanguage)}
                </span>
                <small>
                  {e.customerId} · waiting{" "}
                  {Math.floor((now - Date.parse(e.createdAt)) / 1000)}s
                </small>
              </div>
            ))}
            {!pending.length && <p>No enquiries are waiting.</p>}
          </section>
          <section className="panel events">
            <h2>Live Routing Events</h2>
            {events.map((e, i) => (
              <div key={`${e.timestamp}-${i}`}>
                <time>{new Date(e.timestamp).toLocaleTimeString()}</time>
                {eventText(e)}
              </div>
            ))}
            {!events.length && <p>Waiting for routing events…</p>}
          </section>
        </aside>
      </section>
    </>
  );
}
