const API_BASE_URL =
  (import.meta.env.VITE_API_BASE_URL as string | undefined) ??
  "http://localhost:8080";

export type Role = "USER" | "ADMIN";

export interface Vehicle {
  id: number;
  make: string;
  model: string;
  category: string;
  price: number;
  quantity: number;
}

export interface VehicleInput {
  make: string;
  model: string;
  category: string;
  price: number;
  quantity: number;
}

export interface AuthUser {
  token: string;
  username: string;
  role: Role;
}

export interface SearchParams {
  make?: string;
  model?: string;
  category?: string;
  minPrice?: number | string;
  maxPrice?: number | string;
}

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

type UnauthorizedHandler = () => void;
type ForbiddenHandler = (message: string) => void;

let onUnauthorized: UnauthorizedHandler | null = null;
let onForbidden: ForbiddenHandler | null = null;

export function setAuthHandlers(handlers: {
  onUnauthorized?: UnauthorizedHandler;
  onForbidden?: ForbiddenHandler;
}) {
  onUnauthorized = handlers.onUnauthorized ?? null;
  onForbidden = handlers.onForbidden ?? null;
}

function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem("token");
}

async function request<T>(
  path: string,
  options: RequestInit & { auth?: boolean } = {},
): Promise<T> {
  const { auth = true, headers, ...rest } = options;
  const finalHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    ...(headers as Record<string, string> | undefined),
  };
  if (auth) {
    const token = getToken();
    if (token) finalHeaders.Authorization = `Bearer ${token}`;
  }

  let res: Response;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, {
      ...rest,
      headers: finalHeaders,
    });
  } catch (e) {
    throw new ApiError(
      "Network error — could not reach the server.",
      0,
    );
  }

  if (res.status === 204) return undefined as T;

  const text = await res.text();
  let body: any = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = { message: text };
    }
  }

  if (!res.ok) {
    // The backend's GlobalExceptionHandler returns { "message": ... } for the errors it
    // handles (400/401-bad-creds/404/409). Prefer that; fall back to a generic string only
    // when it's absent (e.g. Spring Security's 403, which bypasses our handler).
    const backendMessage: string | undefined =
      body && typeof body.message === "string" && body.message.trim() !== ""
        ? body.message
        : undefined;

    // Only a 401 from a PROTECTED endpoint means the session/token expired -> log out.
    // /api/auth/login and /api/auth/register return 401 for BAD CREDENTIALS, which must
    // surface the real message, not trigger a logout + "Session expired" redirect.
    const isAuthEndpoint =
      path.startsWith("/api/auth/login") ||
      path.startsWith("/api/auth/register");

    if (res.status === 401 && !isAuthEndpoint) {
      onUnauthorized?.();
      throw new ApiError("Session expired. Please log in again.", 401);
    }

    if (res.status === 403) {
      const msg = backendMessage ?? "You do not have permission to do this";
      onForbidden?.(msg);
      throw new ApiError(msg, 403);
    }

    throw new ApiError(
      backendMessage ?? `Request failed (${res.status})`,
      res.status,
    );
  }

  return body as T;
}

// --- Auth ---
export function register(username: string, password: string) {
  return request<{ id: number; username: string; role: Role }>(
    "/api/auth/register",
    {
      method: "POST",
      body: JSON.stringify({ username, password }),
      auth: false,
    },
  );
}

export function login(username: string, password: string) {
  return request<AuthUser>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
    auth: false,
  });
}

// --- Vehicles ---
export function listVehicles() {
  return request<Vehicle[]>("/api/vehicles");
}

export function searchVehicles(params: SearchParams) {
  const qs = new URLSearchParams();
  (Object.keys(params) as Array<keyof SearchParams>).forEach((k) => {
    const v = params[k];
    if (v === undefined || v === null) return;
    const s = String(v).trim();
    if (s === "") return;
    qs.append(k, s);
  });
  const q = qs.toString();
  return request<Vehicle[]>(`/api/vehicles/search${q ? `?${q}` : ""}`);
}

export function createVehicle(v: VehicleInput) {
  return request<Vehicle>("/api/vehicles", {
    method: "POST",
    body: JSON.stringify(v),
  });
}

export function updateVehicle(id: number, v: VehicleInput) {
  return request<Vehicle>(`/api/vehicles/${id}`, {
    method: "PUT",
    body: JSON.stringify(v),
  });
}

export function deleteVehicle(id: number) {
  return request<void>(`/api/vehicles/${id}`, { method: "DELETE" });
}

export function purchaseVehicle(id: number, quantity = 1) {
  return request<Vehicle>(
    `/api/vehicles/${id}/purchase?quantity=${quantity}`,
    { method: "POST" },
  );
}

export function restockVehicle(id: number, quantity: number) {
  return request<Vehicle>(
    `/api/vehicles/${id}/restock?quantity=${quantity}`,
    { method: "POST" },
  );
}

export function formatINR(amount: number): string {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount);
}