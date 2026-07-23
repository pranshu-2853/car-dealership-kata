import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ApiError, createVehicle, updateVehicle, type Vehicle, type VehicleInput } from "@/lib/api";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  vehicle?: Vehicle;
  onSaved: () => void | Promise<void>;
}

const emptyText = { make: "", model: "", category: "" };

function coercePrice(value: string): number {
  const n = Number(value);
  return Number.isFinite(n) && n >= 0 ? n : 0;
}

function coerceQuantity(value: string): number {
  if (value.trim() === "") return 1;
  const n = Math.trunc(Number(value));
  return Number.isFinite(n) && n >= 0 ? n : 1;
}

export default function VehicleFormDialog({ open, onOpenChange, vehicle, onSaved }: Props) {
  const editing = !!vehicle;
  const [form, setForm] = useState(emptyText);
  const [priceInput, setPriceInput] = useState("");
  const [quantityInput, setQuantityInput] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) {
      setForm(
        vehicle
          ? { make: vehicle.make, model: vehicle.model, category: vehicle.category }
          : emptyText,
      );
      setPriceInput(vehicle ? String(vehicle.price) : "");
      setQuantityInput(vehicle ? String(vehicle.quantity) : "");
    }
  }, [open, vehicle]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.make.trim() || !form.model.trim() || !form.category.trim()) {
      toast.error("Make, model and category are required");
      return;
    }
    const payload: VehicleInput = {
      make: form.make.trim(),
      model: form.model.trim(),
      category: form.category.trim(),
      price: coercePrice(priceInput),
      quantity: coerceQuantity(quantityInput),
    };
    setSaving(true);
    try {
      if (editing && vehicle) {
        await updateVehicle(vehicle.id, payload);
        toast.success("Vehicle updated");
      } else {
        await createVehicle(payload);
        toast.success("Vehicle added");
      }
      onOpenChange(false);
      await onSaved();
    } catch (err) {
      if (err instanceof ApiError && err.status !== 401 && err.status !== 403) {
        toast.error(err.message);
      }
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-sm">
        <DialogHeader>
          <DialogTitle className="text-base font-semibold">
            {editing ? "Edit Vehicle" : "Add Vehicle"}
          </DialogTitle>
          <DialogDescription className="text-sm text-muted-foreground">
            {editing
              ? "Update the details for this vehicle."
              : "Add a new vehicle to the dealership inventory."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={submit} className="mt-1 space-y-4">
          {/* Make */}
          <div className="space-y-1.5">
            <Label htmlFor="dlg-make" className="text-xs font-medium text-foreground">
              Make
            </Label>
            <Input
              id="dlg-make"
              placeholder="e.g. Toyota"
              value={form.make}
              onChange={(e) => setForm({ ...form, make: e.target.value })}
              className="h-9 text-sm"
              required
            />
          </div>

          {/* Model */}
          <div className="space-y-1.5">
            <Label htmlFor="dlg-model" className="text-xs font-medium text-foreground">
              Model
            </Label>
            <Input
              id="dlg-model"
              placeholder="e.g. Corolla"
              value={form.model}
              onChange={(e) => setForm({ ...form, model: e.target.value })}
              className="h-9 text-sm"
              required
            />
          </div>

          {/* Category */}
          <div className="space-y-1.5">
            <Label htmlFor="dlg-category" className="text-xs font-medium text-foreground">
              Category
            </Label>
            <Input
              id="dlg-category"
              placeholder="e.g. Sedan"
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
              className="h-9 text-sm"
              required
            />
          </div>

          {/* Price + Quantity */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="dlg-price" className="text-xs font-medium text-foreground">
                Price (₹)
              </Label>
              <Input
                id="dlg-price"
                type="number"
                step="0.01"
                min={0}
                placeholder="0"
                value={priceInput}
                onChange={(e) => setPriceInput(e.target.value)}
                onBlur={() => setPriceInput(priceInput.trim() === "" ? "0" : priceInput)}
                className="h-9 text-sm"
                required
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="dlg-quantity" className="text-xs font-medium text-foreground">
                Quantity
              </Label>
              <Input
                id="dlg-quantity"
                type="number"
                min={0}
                placeholder="1"
                value={quantityInput}
                onChange={(e) => setQuantityInput(e.target.value)}
                onBlur={() => setQuantityInput(quantityInput.trim() === "" ? "1" : quantityInput)}
                className="h-9 text-sm"
                required
              />
            </div>
          </div>

          <DialogFooter className="mt-2 gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              className="h-9 text-sm"
              onClick={() => onOpenChange(false)}
              disabled={saving}
            >
              Cancel
            </Button>
            <Button type="submit" size="sm" className="h-9 text-sm" disabled={saving}>
              {saving ? (
                <Loader2 className="h-3.5 w-3.5 animate-spin" />
              ) : editing ? (
                "Save changes"
              ) : (
                "Add vehicle"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
