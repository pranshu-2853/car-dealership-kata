import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import { Loader2, LogOut, Plus, Search, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { useAuth } from "@/contexts/AuthContext";
import {
  ApiError,
  listVehicles,
  searchVehicles,
  type SearchParams,
  type Vehicle,
} from "@/lib/api";
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
      if (filters.model && String(filters.model).trim()) params.model = String(filters.model).trim();
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

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-10 border-b border-border bg-background/80 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground font-black">
              A
            </div>
            <div>
              <h1 className="text-lg font-bold tracking-tight">AutoVault</h1>
              <p className="text-xs text-muted-foreground">Dealership Inventory</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium leading-tight">{username}</p>
              <Badge
                variant={isAdmin ? "default" : "secondary"}
                className="mt-0.5 text-[10px] uppercase tracking-wider"
              >
                {role}
              </Badge>
            </div>
            <Button variant="outline" size="sm" onClick={logout}>
              <LogOut className="mr-1.5 h-4 w-4" />
              Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6">
        <div className="mb-8 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="text-3xl font-bold tracking-tight">Vehicle Inventory</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Browse the fleet, purchase vehicles, and track live stock.
            </p>
          </div>
          {isAdmin && (
            <Button onClick={() => setAddOpen(true)}>
              <Plus className="mr-1.5 h-4 w-4" /> Add Vehicle
            </Button>
          )}
        </div>

        <div className="mb-8 rounded-xl border border-border bg-card p-4 shadow-sm sm:p-6">
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-5">
            <Input
              placeholder="Make"
              value={filters.make ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, make: e.target.value }))}
            />
            <Input
              placeholder="Model"
              value={filters.model ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, model: e.target.value }))}
            />
            <Input
              placeholder="Category"
              value={filters.category ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, category: e.target.value }))}
            />
            <Input
              type="number"
              placeholder="Min price"
              value={filters.minPrice ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, minPrice: e.target.value }))}
            />
            <Input
              type="number"
              placeholder="Max price"
              value={filters.maxPrice ?? ""}
              onChange={(e) => setFilters((f) => ({ ...f, maxPrice: e.target.value }))}
            />
          </div>
          <div className="mt-4 flex flex-wrap gap-2">
            <Button onClick={runSearch} disabled={loading}>
              <Search className="mr-1.5 h-4 w-4" /> Search
            </Button>
            <Button variant="outline" onClick={clearFilters} disabled={loading}>
              <X className="mr-1.5 h-4 w-4" /> Clear
            </Button>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-24">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
          </div>
        ) : vehicles && vehicles.length > 0 ? (
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {vehicles.map((v) => (
              <VehicleCard
                key={v.id}
                vehicle={v}
                isAdmin={isAdmin}
                onChanged={refresh}
              />
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-border bg-card px-6 py-20 text-center">
            <div className="mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-muted">
              <Search className="h-6 w-6 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold">No vehicles found</h3>
            <p className="mt-1 max-w-sm text-sm text-muted-foreground">
              Try adjusting your search filters or clearing them to see the full inventory.
            </p>
          </div>
        )}
      </main>

      {isAdmin && (
        <VehicleFormDialog
          open={addOpen}
          onOpenChange={setAddOpen}
          onSaved={refresh}
        />
      )}
    </div>
  );
}