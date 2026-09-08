import { config } from "../config";
export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number,
  ) {
    super(message);
  }
}
export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  try {
    const response = await fetch(`${config.apiBaseUrl}${path}`, {
      ...init,
      headers: { "Content-Type": "application/json", ...init.headers },
    });
    if (!response.ok) {
      const body = await response.text();
      throw new ApiError(
        body || `Request failed (${response.status})`,
        response.status,
      );
    }
    const body = await response.text();
    return body ? (JSON.parse(body) as T) : (undefined as T);
  } catch (error) {
    console.error("API request failed", path, error);
    throw error instanceof ApiError
      ? error
      : new ApiError("Unable to reach the backend. Is Spring Boot running?");
  }
}
