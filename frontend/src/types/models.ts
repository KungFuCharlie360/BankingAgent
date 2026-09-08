export type AgentStatus='ONLINE'|'BUSY'|'OFFLINE'; export type EnquiryStatus='PENDING'|'ASSIGNED'|'ACTIVE'|'RESOLVED'|'CLOSED';
export interface Agent {id:string;name:string;status:AgentStatus;languages:string[];skills:string[];activeEnquiryCount:number;maxCapacity:number;utilization:number}
export interface Enquiry {enquiryId:string;customerId:string;category:string;preferredLanguage:string;status:EnquiryStatus;assignedAgent?:{id:string;name:string};createdAt:string;assignedAt?:string;closedAt?:string}
export interface ConversationMessage {messageId:string;enquiryId:string;senderType:'CUSTOMER'|'AGENT';senderId:string;content:string;createdAt:string}
export interface RoutingEvent {eventType:string;enquiryId?:string;agentId?:string;payload:Record<string,unknown>;timestamp:string}
export interface SimulationItem {enquiryId:string;agentId?:string;simulatedCompletionAt?:string;handlingDurationSeconds:number;status:string}
export interface SimulationDashboard {running:boolean;activeTimers:number;waiting:number;simulatedEnquiries:SimulationItem[]}
export interface KnowledgeSuggestion {id:string;category:string;question:string;answer:string}
