import { createFileRoute } from "@tanstack/react-router";
import Dashboard from "@/components/Dashboard";
import RequireAuth from "@/components/RequireAuth";

export const Route = createFileRoute("/")({
  component: DashboardRoute,
});

function DashboardRoute() {
  return (
    <RequireAuth>
      <Dashboard />
    </RequireAuth>
  );
}
