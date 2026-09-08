import type { Agent, Enquiry, SimulationItem } from "../../types/models";
const pretty = (v: string) => v.replaceAll("_", " ");
export function AgentCard({
  agent,
  enquiries,
  states,
  now,
}: {
  agent: Agent;
  enquiries: Enquiry[];
  states: SimulationItem[];
  now: number;
}) {
  const assigned = enquiries.filter(
    (e) => e.assignedAgent?.id === agent.id && e.status !== "CLOSED",
  );
  return (
    <article className="agent-card">
      <div className="agent-top">
        <div>
          <h3>{agent.name}</h3>
          <span className={`badge ${agent.status.toLowerCase()}`}>
            {agent.status}
          </span>
        </div>
        <b>
          {agent.activeEnquiryCount} / {agent.maxCapacity}
        </b>
      </div>
      <p>{agent.languages.map(pretty).join(" · ")}</p>
      <p className="skills">{agent.skills.map(pretty).join(" · ")}</p>
      <div className="progress">
        <i style={{ width: `${Math.min(100, agent.utilization * 100)}%` }} />
      </div>
      <small>Utilization {Math.round(agent.utilization * 100)}%</small>
      <div className="assigned">
        {assigned.map((e) => {
          const end = states.find(
            (s) => s.enquiryId === e.enquiryId,
          )?.simulatedCompletionAt;
          const seconds = end
            ? Math.max(0, Math.ceil((Date.parse(end) - now) / 1000))
            : undefined;
          return (
            <div key={e.enquiryId}>
              <b>{e.enquiryId.slice(0, 8)}</b>
              <span>
                {pretty(e.category)} · {e.customerId}
              </span>
              {seconds !== undefined && (
                <em>
                  {String(Math.floor(seconds / 60)).padStart(2, "0")}:
                  {String(seconds % 60).padStart(2, "0")} remaining
                </em>
              )}
            </div>
          );
        })}
        {!assigned.length && <small>No active enquiries</small>}
      </div>
    </article>
  );
}
