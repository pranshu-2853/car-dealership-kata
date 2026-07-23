import { useState } from "react";
import { toast } from "sonner";
import { Loader2, Package, Pencil, ShoppingCart, Trash2 } from "lucide-react";
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

export default function VehicleCard({ vehicle, isAdmin, onChanged }: Props) {
  const [purchaseQty, setPurchaseQty] = useState(1);
  const [restockQty, setRestockQty] = useState(1);
  const [busy, setBusy] = useState<null | "purchase" | "restock" | "delete">(null);
  const [editOpen, setEditOpen] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const outOfStock = vehicle.quantity <= 0;

  async function handlePurchase() {
    if (purchaseQty <= 0) return;
    setBusy("purchase");
    try {
      await purchaseVehicle(vehicle.id, purchaseQty);
      toast.success(`Purchased ${purchaseQty} × ${vehicle.make} ${vehicle.model}`);
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
    if (restockQty <= 0) {
      toast.error("Quantity must be positive");
      return;
    }
    setBusy("restock");
    try {
      await restockVehicle(vehicle.id, restockQty);
      toast.success(`Restocked +${restockQty}`);
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
    <div className="group flex flex-col overflow-hidden rounded-xl border border-border bg-card shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-lg">
      <div className="relative flex h-32 items-center justify-center overflow-hidden bg-gradient-to-br from-primary/10 via-accent to-background">
        <span className="text-4xl font-black tracking-tight text-primary/30">
          {vehicle.make.slice(0, 2).toUpperCase()}
        </span>
        <Badge className="absolute left-3 top-3" variant="secondary">
          {vehicle.category}
        </Badge>
        {outOfStock && (
          <Badge className="absolute right-3 top-3" variant="destructive">
            Out of Stock
          </Badge>
        )}
      </div>

      <div className="flex flex-1 flex-col gap-3 p-5">
        <div>
          <h3 className="text-lg font-bold leading-tight">
            {vehicle.make} {vehicle.model}
          </h3>
          <p className="mt-1 text-2xl font-extrabold text-primary">
            {formatINR(vehicle.price)}
          </p>
        </div>

        <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
          <Package className="h-4 w-4" />
          <span>{vehicle.quantity} in stock</span>
        </div>

        <div className="mt-auto space-y-2 pt-2">
          <div className="flex items-center gap-2">
            <Input
              type="number"
              min={1}
              value={purchaseQty}
              onChange={(e) => setPurchaseQty(Math.max(1, Number(e.target.value) || 1))}
              className="h-10 w-20"
              disabled={outOfStock}
            />
            <Button
              className="flex-1"
              onClick={handlePurchase}
              disabled={outOfStock || busy !== null}
            >
              {busy === "purchase" ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <>
                  <ShoppingCart className="mr-1.5 h-4 w-4" />
                  Purchase
                </>
              )}
            </Button>
          </div>

          {isAdmin && (
            <>
              <div className="flex items-center gap-2">
                <Input
                  type="number"
                  min={1}
                  required
                  value={restockQty}
                  onChange={(e) => setRestockQty(Math.max(1, Number(e.target.value) || 1))}
                  className="h-10 w-20"
                />
                <Button
                  variant="secondary"
                  className="flex-1"
                  onClick={handleRestock}
                  disabled={busy !== null}
                >
                  {busy === "restock" ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    "Restock"
                  )}
                </Button>
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  className="flex-1"
                  onClick={() => setEditOpen(true)}
                >
                  <Pencil className="mr-1.5 h-4 w-4" /> Edit
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  className="flex-1 text-destructive hover:text-destructive"
                  onClick={() => setConfirmDelete(true)}
                >
                  <Trash2 className="mr-1.5 h-4 w-4" /> Delete
                </Button>
              </div>
            </>
          )}
        </div>
      </div>

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
            <AlertDialogTitle>Delete this vehicle?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently remove the {vehicle.make} {vehicle.model} from
              inventory. This action cannot be undone.
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
              {busy === "delete" ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                "Delete"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}