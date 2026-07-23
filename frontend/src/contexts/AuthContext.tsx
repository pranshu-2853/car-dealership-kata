import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { toast } from "sonner";
import {
  login as apiLogin,
  register as apiRegister,
  setAuthHandlers,
  type Role,
} from "@/lib/api";

interface AuthState {
  token: string | null;
  username: string | null;
  role: Role | null;
}

interface AuthContextValue extends AuthState {
  isAuthenticated: boolean;
  isReady: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function readInitial(): AuthState {
  if (typeof window === "undefined")
    return { token: null, username: null, role: null };
  return {
    token: window.localStorage.getItem("token"),
    username: window.localStorage.getItem("username"),
    role: window.localStorage.getItem("role") as Role | null,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    token: null,
    username: null,
    role: null,
  });
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    setState(readInitial());
    setIsReady(true);
  }, []);

  const logout = useCallback(() => {
    window.localStorage.removeItem("token");
    window.localStorage.removeItem("username");
    window.localStorage.removeItem("role");
    setState({ token: null, username: null, role: null });
  }, []);

  useEffect(() => {
    setAuthHandlers({
      onUnauthorized: () => {
        logout();
        if (typeof window !== "undefined" && window.location.pathname !== "/login") {
          window.location.href = "/login";
        }
      },
      onForbidden: (msg) => {
        toast.error(msg);
      },
    });
  }, [logout]);

  const login = useCallback(async (username: string, password: string) => {
    const res = await apiLogin(username, password);
    window.localStorage.setItem("token", res.token);
    window.localStorage.setItem("username", res.username);
    window.localStorage.setItem("role", res.role);
    setState({ token: res.token, username: res.username, role: res.role });
  }, []);

  const register = useCallback(async (username: string, password: string) => {
    await apiRegister(username, password);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      isAuthenticated: !!state.token,
      isReady,
      login,
      register,
      logout,
    }),
    [state, isReady, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}