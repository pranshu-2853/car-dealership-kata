import { useState } from "react";
import { toast } from "sonner";
import { Loader2, Minus, Package, Pencil, Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  ApiError,
  deleteVehicle,
  formatINR,
  purchaseVehicle,
  restockVehicle,
  type Vehicle,
} from "@/lib/api";
import VehicleFormDialog from "@/components/VehicleFormDialog";

interface Props {
  vehicle: Vehicle;
  isAdmin: boolean;
  onChanged: () => void | Promise<void>;
}

function coercePositiveQty(value: string): number {
  const n = Math.trunc(Number(value));
  return Number.isFinite(n) && n >= 1 ? n : 1;
}

function parsePositiveIntOrNull(value: string): number | null {
  const trimmed = value.trim();
  if (trimmed === "") return null;
  const n = Math.trunc(Number(trimmed));
  return Number.isFinite(n) && n >= 1 ? n : null;
}

export default function VehicleCard({ vehicle, isAdmin, onChanged }: Props) {
  const [purchaseQty, setPurchaseQty] = useState("1");
  const [restockQty, setRestockQty] = useState("1");
  const [restockError, setRestockError] = useState<string | null>(null);
  const [busy, setBusy] = useState<null | "purchase" | "restock" | "delete">(null);
  const [editOpen, setEditOpen] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const outOfStock = vehicle.quantity <= 0;

  async function handlePurchase() {
    const qty = coercePositiveQty(purchaseQty);
    setPurchaseQty(String(qty));
    setBusy("purchase");
    try {
      await purchaseVehicle(vehicle.id, qty);
      toast.success(`Purchased ${qty} × ${vehicle.make} ${vehicle.model}`);
      await onChanged();
    } catch (e) {
      if (e instanceof ApiError && e.status !== 401 && e.status !== 403) {
        toast.error(e.message);
      }
    } finally {
      setBusy(null);
    }
  }

  async function handleRestock() {
    const qty = parsePositiveIntOrNull(restockQty);
    if (qty === null) {
      setRestockError("Enter a quantity of 1 or more");
      return;
    }
    setRestockError(null);
    setBusy("restock");
    try {
      await restockVehicle(vehicle.id, qty);
      toast.success(`Restocked +${qty}`);
      await onChanged();
    } catch (e) {
      if (e instanceof ApiError && e.status !== 401 && e.status !== 403) {
        toast.error(e.message);
      }
    } finally {
      setBusy(null);
    }
  }

  async function handleDelete() {
    setBusy("delete");
    try {
      await deleteVehicle(vehicle.id);
      toast.success("Vehicle deleted");
      await onChanged();
    } catch (e) {
      if (e instanceof ApiError && e.status !== 401 && e.status !== 403) {
        toast.error(e.message);
      }
    } finally {
      setBusy(null);
      setConfirmDelete(false);
    }
  }

  return (
    <div className="flex flex-col overflow-hidden rounded-md border border-border bg-card transition-shadow hover:shadow-sm">
      {/* ── Card header ── */}
      <div className="flex items-start justify-between border-b border-border px-4 py-3">
        <div className="min-w-0 flex-1 pr-2">
          <p className="text-[11px] font-medium text-muted-foreground">{vehicle.category}</p>
          <h3 className="mt-0.5 truncate text-sm font-semibold text-foreground">
            {vehicle.make} {vehicle.model}
          </h3>
        </div>
        {outOfStock ? (
          <Badge
            variant="outline"
            className="shrink-0 border-destructive/30 text-destructive text-[10px] font-medium"
          >
            Out of stock
          </Badge>
        ) : (
          <Badge
            variant="outline"
            className="shrink-0 border-border text-muted-foreground text-[10px] font-medium"
          >
            In stock
          </Badge>
        )}
      </div>

      {/* ── Card body ── */}
      <div className="flex flex-1 flex-col gap-4 px-4 py-4">
        {/* Price + stock row */}
        <div className="flex items-center justify-between">
          <span className="text-sm font-bold text-foreground">{formatINR(vehicle.price)}</span>
          <span className="flex items-center gap-1 text-xs text-muted-foreground">
            <Package className="h-3.5 w-3.5" />
            {vehicle.quantity}
          </span>
        </div>

        {/* ── Purchase row ── */}
        <div className="space-y-2">
          <p className="text-[11px] font-medium text-muted-foreground">Purchase</p>
          <div className="flex items-center gap-2">
            <div className="flex items-center rounded-md border border-border">
              <button
                type="button"
                className="flex h-8 w-7 items-center justify-center text-muted-foreground hover:text-foreground disabled:opacity-40"
                onClick={() =>
                  setPurchaseQty(String(Math.max(1, coercePositiveQty(purchaseQty) - 1)))
                }
                disabled={outOfStock || busy !== null}
              >
                <Minus className="h-3 w-3" />
              </button>
              <Input
                type="number"
                min={1}
                value={purchaseQty}
                onChange={(e) => setPurchaseQty(e.target.value)}
                onBlur={() => setPurchaseQty(String(coercePositiveQty(purchaseQty)))}
                className="h-8 w-12 rounded-none border-x border-border border-y-0 text-center text-sm shadow-none focus-visible:ring-0"
                disabled={outOfStock}
              />
              <button
                type="button"
                className="flex h-8 w-7 items-center justify-center text-muted-foreground hover:text-foreground disabled:opacity-40"
                onClick={() => setPurchaseQty(String(coercePositiveQty(purchaseQty) + 1))}
                disabled={outOfStock || busy !== null}
              >
                <Plus className="h-3 w-3" />
              </button>
            </div>
            <Button
              size="sm"
              className="h-8 flex-1 text-xs font-medium"
              onClick={handlePurchase}
              disabled={outOfStock || busy !== null}
            >
              {busy === "purchase" ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : "Purchase"}
            </Button>
          </div>
        </div>

        {/* ── Admin controls ── */}
        {isAdmin && (
          <div className="space-y-2 border-t border-border pt-3">
            <p className="text-[11px] font-medium text-muted-foreground">Admin</p>

            {/* Restock */}
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <div className="flex items-center rounded-md border border-border">
                  <button
                    type="button"
                    className="flex h-8 w-7 items-center justify-center text-muted-foreground hover:text-foreground disabled:opacity-40"
                    onClick={() =>
                      setRestockQty(String(Math.max(1, (parseInt(restockQty) || 1) - 1)))
                    }
                    disabled={busy !== null}
                  >
                    <Minus className="h-3 w-3" />
                  </button>
                  <Input
                    type="number"
                    min={1}
                    value={restockQty}
                    onChange={(e) => {
                      setRestockQty(e.target.value);
                      if (restockError) setRestockError(null);
                    }}
                    aria-invalid={restockError !== null}
                    className="h-8 w-12 rounded-none border-x border-border border-y-0 text-center text-sm shadow-none focus-visible:ring-0"
                  />
                  <button
                    type="button"
                    className="flex h-8 w-7 items-center justify-center text-muted-foreground hover:text-foreground disabled:opacity-40"
                    onClick={() => setRestockQty(String((parseInt(restockQty) || 0) + 1))}
                    disabled={busy !== null}
                  >
                    <Plus className="h-3 w-3" />
                  </button>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  className="h-8 flex-1 text-xs font-medium"
                  onClick={handleRestock}
                  disabled={busy !== null}
                >
                  {busy === "restock" ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                  ) : (
                    "Restock"
                  )}
                </Button>
              </div>
              {restockError && <p className="text-[11px] text-destructive">{restockError}</p>}
            </div>

            {/* Edit / Delete */}
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="h-8 flex-1 gap-1 text-xs font-medium"
                onClick={() => setEditOpen(true)}
              >
                <Pencil className="h-3 w-3" />
                Edit
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="h-8 flex-1 gap-1 text-xs font-medium text-destructive hover:text-destructive"
                onClick={() => setConfirmDelete(true)}
              >
                <Trash2 className="h-3 w-3" />
                Delete
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* Dialogs */}
      {isAdmin && (
        <VehicleFormDialog
          open={editOpen}
          onOpenChange={setEditOpen}
          vehicle={vehicle}
          onSaved={onChanged}
        />
      )}

      <AlertDialog open={confirmDelete} onOpenChange={setConfirmDelete}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete vehicle?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently remove{" "}
              <span className="font-medium text-foreground">
                {vehicle.make} {vehicle.model}
              </span>{" "}
              from inventory. This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={busy === "delete"}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={(e) => {
                e.preventDefault();
                handleDelete();
              }}
              disabled={busy === "delete"}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {busy === "delete" ? <Loader2 className="h-4 w-4 animate-spin" /> : "Delete"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
