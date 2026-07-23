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
import {
  ApiError,
  createVehicle,
  updateVehicle,
  type Vehicle,
  type VehicleInput,
} from "@/lib/api";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  vehicle?: Vehicle;
  onSaved: () => void | Promise<void>;
}

const empty: VehicleInput = {
  make: "",
  model: "",
  category: "",
  price: 0,
  quantity: 0,
};

export default function VehicleFormDialog({
  open,
  onOpenChange,
  vehicle,
  onSaved,
}: Props) {
  const editing = !!vehicle;
  const [form, setForm] = useState<VehicleInput>(empty);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) {
      setForm(
        vehicle
          ? {
              make: vehicle.make,
              model: vehicle.model,
              category: vehicle.category,
              price: vehicle.price,
              quantity: vehicle.quantity,
            }
          : empty,
      );
    }
  }, [open, vehicle]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.make.trim() || !form.model.trim() || !form.category.trim()) {
      toast.error("Make, model and category are required");
      return;
    }
    setSaving(true);
    try {
      if (editing && vehicle) {
        await updateVehicle(vehicle.id, form);
        toast.success("Vehicle updated");
      } else {
        await createVehicle(form);
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
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{editing ? "Edit Vehicle" : "Add Vehicle"}</DialogTitle>
          <DialogDescription>
            {editing
              ? "Update the details for this vehicle."
              : "Add a new vehicle to the dealership inventory."}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={submit} className="space-y-4">
          <div className="grid gap-2">
            <Label htmlFor="make">Make</Label>
            <Input
              id="make"
              value={form.make}
              onChange={(e) => setForm({ ...form, make: e.target.value })}
              required
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="model">Model</Label>
            <Input
              id="model"
              value={form.model}
              onChange={(e) => setForm({ ...form, model: e.target.value })}
              required
            />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="category">Category</Label>
            <Input
              id="category"
              value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })}
              required
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="grid gap-2">
              <Label htmlFor="price">Price (₹)</Label>
              <Input
                id="price"
                type="number"
                step="0.01"
                min={0}
                value={form.price}
                onChange={(e) => setForm({ ...form, price: Number(e.target.value) })}
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="quantity">Quantity</Label>
              <Input
                id="quantity"
                type="number"
                min={0}
                value={form.quantity}
                onChange={(e) =>
                  setForm({ ...form, quantity: Number(e.target.value) })
                }
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={saving}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? (
                <Loader2 className="h-4 w-4 animate-spin" />
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