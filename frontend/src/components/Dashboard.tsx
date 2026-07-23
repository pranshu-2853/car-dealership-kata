import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import { Loader2, LogOut, Plus, Search, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/contexts/AuthContext";
import { ApiError, listVehicles, searchVehicles, type SearchParams, type Vehicle } from "@/lib/api";
import VehicleCard from "@/components/VehicleCard";
import VehicleFormDialog from "@/components/VehicleFormDialog";

const emptyFilters: SearchParams = {
  make: "",
  model: "",
  category: "",
  minPrice: "",
  maxPrice: "",
};

export default function Dashboard() {
  const { username, role, logout } = useAuth();
  const isAdmin = role === "ADMIN";
  const [vehicles, setVehicles] = useState<Vehicle[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState<SearchParams>(emptyFilters);
  const [addOpen, setAddOpen] = useState(false);
  const [isSearchActive, setIsSearchActive] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listVehicles();
      setVehicles(data);
      setIsSearchActive(false);
    } catch (e) {
      if (e instanceof ApiError && e.status !== 401 && e.status !== 403) {
        toast.error(e.message);
      }
      setVehicles([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const refresh = useCallback(async () => {
    if (isSearchActive) {
      await runSearch();
    } else {
      await load();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isSearchActive, load]);

  async function runSearch() {
    setLoading(true);
    try {
      const params: SearchParams = {};
      if (filters.make && String(filters.make).trim()) params.make = String(filters.make).trim();
      if (filters.model && String(filters.model).trim())
        params.model = String(filters.model).trim();
      if (filters.category && String(filters.category).trim())
        params.category = String(filters.category).trim();
      if (filters.minPrice !== "" && filters.minPrice !== undefined)
        params.minPrice = filters.minPrice;
      if (filters.maxPrice !== "" && filters.maxPrice !== undefined)
        params.maxPrice = filters.maxPrice;

      const data = await searchVehicles(params);
      setVehicles(data);
      setIsSearchActive(true);
    } catch (e) {
      if (e instanceof ApiError && e.status !== 401 && e.status !== 403) {
        toast.error(e.message);
      }
    } finally {
      setLoading(false);
    }
  }

  function clearFilters() {
    setFilters(emptyFilters);
    load();
  }

  const hasFilters = Object.values(filters).some((v) => v !== "");

  return (
    <div className="min-h-screen bg-background">
      {/* ── Top navigation bar ── */}
      <header className="sticky top-0 z-20 border-b border-border bg-card">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-6 h-14">
          {/* Brand */}
          <div className="flex items-center gap-2.5">
            <div className="flex h-7 w-7 items-center justify-center rounded-md bg-foreground">
              <svg
                width="13"
                height="13"
                viewBox="0 0 24 24"
                fill="none"
                stroke="white"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <rect x="1" y="3" width="15" height="13" rx="1" />
                <path d="M16 8h4l3 5v4h-7V8z" />
                <circle cx="5.5" cy="18.5" r="2.5" />
                <circle cx="18.5" cy="18.5" r="2.5" />
              </svg>
            </div>
            <span className="text-sm font-semibold text-foreground">AutoVault</span>
          </div>

          {/* Right: user + logout */}
          <div className="flex items-center gap-4">
            <div className="hidden sm:flex flex-col items-end leading-tight">
              <span className="text-sm font-medium text-foreground">{username}</span>
              <span className="text-xs text-muted-foreground">{role}</span>
            </div>
            <Button variant="outline" size="sm" onClick={logout} className="h-8 gap-1.5 text-xs">
              <LogOut className="h-3.5 w-3.5" />
              <span className="hidden sm:inline">Logout</span>
            </Button>
          </div>
        </div>
      </header>

      {/* ── Page body ── */}
      <main className="mx-auto max-w-7xl px-4 sm:px-6 py-8">
        {/* Page title + action */}
        <div className="mb-6 flex items-center justify-between gap-4">
          <div>
            <h1 className="text-base font-semibold text-foreground">Vehicle Inventory</h1>
            <p className="mt-0.5 text-sm text-muted-foreground">
              {vehicles !== null
                ? `${vehicles.length} vehicle${vehicles.length !== 1 ? "s" : ""}${isSearchActive ? " matching your search" : " in stock"}`
                : "Loading inventory…"}
            </p>
          </div>
          {isAdmin && (
            <Button
              size="sm"
              className="h-8 gap-1.5 text-xs font-medium"
              onClick={() => setAddOpen(true)}
            >
              <Plus className="h-3.5 w-3.5" />
              Add Vehicle
            </Button>
          )}
        </div>

        {/* ── Filter bar ── */}
        <div className="mb-6 rounded-md border border-border bg-card p-3">
          <div className="flex flex-wrap gap-2 items-center">
            <Input
              placeholder="Make"
              value={filters.make ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, make: e.target.value }))}
              className="h-8 w-28 text-xs flex-shrink-0"
            />
            <Input
              placeholder="Model"
              value={filters.model ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, model: e.target.value }))}
              className="h-8 w-28 text-xs flex-shrink-0"
            />
            <Input
              placeholder="Category"
              value={filters.category ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, category: e.target.value }))}
              className="h-8 w-28 text-xs flex-shrink-0"
            />
            <Input
              type="number"
              placeholder="Min price"
              value={filters.minPrice ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, minPrice: e.target.value }))}
              className="h-8 w-24 text-xs flex-shrink-0"
            />
            <Input
              type="number"
              placeholder="Max price"
              value={filters.maxPrice ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, maxPrice: e.target.value }))}
              className="h-8 w-24 text-xs flex-shrink-0"
            />
            <div className="flex gap-2 ml-auto">
              <Button
                size="sm"
                variant="outline"
                onClick={runSearch}
                disabled={loading}
                className="h-8 gap-1.5 text-xs"
              >
                <Search className="h-3.5 w-3.5" />
                Search
              </Button>
              {hasFilters && (
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={clearFilters}
                  disabled={loading}
                  className="h-8 gap-1.5 text-xs text-muted-foreground hover:text-foreground"
                >
                  <X className="h-3.5 w-3.5" />
                  Clear
                </Button>
              )}
            </div>
          </div>
        </div>

        {/* ── Content area ── */}
        {loading ? (
          <div className="flex items-center justify-center py-32">
            <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
          </div>
        ) : vehicles && vehicles.length > 0 ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {vehicles.map((v) => (
              <VehicleCard key={v.id} vehicle={v} isAdmin={isAdmin} onChanged={refresh} />
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center rounded-md border border-dashed border-border bg-card py-24 text-center">
            <Search className="mb-3 h-7 w-7 text-muted-foreground/40" />
            <p className="text-sm font-medium text-foreground">No vehicles found</p>
            <p className="mt-1 text-xs text-muted-foreground">
              {isSearchActive
                ? "Try adjusting your filters."
                : "Add the first vehicle to get started."}
            </p>
          </div>
        )}
      </main>

      {isAdmin && <VehicleFormDialog open={addOpen} onOpenChange={setAddOpen} onSaved={refresh} />}
    </div>
  );
}
