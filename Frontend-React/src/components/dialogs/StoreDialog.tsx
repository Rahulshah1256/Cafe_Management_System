import { useState } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
  FormControlLabel,
  Switch,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { StoreService } from '../../services/store.service';
import { GlobalConstants } from '../../shared/globalConstants';
import { useSnackbarService } from '../../shared/useSnackbarService';

interface Props {
  action: 'Add' | 'Edit';
  data?: any;
  onClose: () => void;
  onSuccess: () => void;
}

export default function StoreDialog({ action, data, onClose, onSuccess }: Props) {
  const { openSnackBar } = useSnackbarService();
  const isEdit = action === 'Edit';
  const [form, setForm] = useState<any>({
    name: data?.name ?? '',
    addressLine1: data?.addressLine1 ?? '',
    addressLine2: data?.addressLine2 ?? '',
    city: data?.city ?? '',
    state: data?.state ?? '',
    pincode: data?.pincode ?? '',
    latitude: data?.latitude ?? '',
    longitude: data?.longitude ?? '',
    phone: data?.phone ?? '',
    openingHours: data?.openingHours ?? '',
    active: data?.active ?? true,
  });

  const update = (patch: any) => setForm((prev: any) => ({ ...prev, ...patch }));

  const validate = () => !form.name || !form.addressLine1 || !form.city || !form.state || !form.pincode;

  const payload = () => ({
    name: form.name,
    addressLine1: form.addressLine1,
    addressLine2: form.addressLine2 || null,
    city: form.city,
    state: form.state,
    pincode: form.pincode,
    latitude: form.latitude === '' ? null : Number(form.latitude),
    longitude: form.longitude === '' ? null : Number(form.longitude),
    phone: form.phone || null,
    openingHours: form.openingHours || null,
    active: form.active,
  });

  const handleSubmit = () => {
    const request = isEdit
      ? StoreService.updateStore(data.id, payload())
      : StoreService.addStore(payload());

    request
      .then(() => {
        onClose();
        onSuccess();
        openSnackBar(isEdit ? 'Store updated' : 'Store added', 'success');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  return (
    <>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography sx={{ flex: 1 }}>{action} Store</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        <Box display="flex" flexDirection="column" gap={2} sx={{ mt: 1 }}>
          <TextField
            label="Store Name"
            variant="standard"
            required
            value={form.name}
            onChange={(e) => update({ name: e.target.value })}
          />
          <TextField
            label="Address Line 1"
            variant="standard"
            required
            value={form.addressLine1}
            onChange={(e) => update({ addressLine1: e.target.value })}
          />
          <TextField
            label="Address Line 2"
            variant="standard"
            value={form.addressLine2}
            onChange={(e) => update({ addressLine2: e.target.value })}
          />
          <Box display="flex" gap={2}>
            <TextField
              label="City"
              variant="standard"
              required
              fullWidth
              value={form.city}
              onChange={(e) => update({ city: e.target.value })}
            />
            <TextField
              label="State"
              variant="standard"
              required
              fullWidth
              value={form.state}
              onChange={(e) => update({ state: e.target.value })}
            />
          </Box>
          <TextField
            label="Pincode"
            variant="standard"
            required
            value={form.pincode}
            onChange={(e) => update({ pincode: e.target.value })}
          />
          <Box display="flex" gap={2}>
            <TextField
              label="Latitude"
              variant="standard"
              type="number"
              fullWidth
              value={form.latitude}
              onChange={(e) => update({ latitude: e.target.value })}
            />
            <TextField
              label="Longitude"
              variant="standard"
              type="number"
              fullWidth
              value={form.longitude}
              onChange={(e) => update({ longitude: e.target.value })}
            />
          </Box>
          <TextField
            label="Phone"
            variant="standard"
            value={form.phone}
            onChange={(e) => update({ phone: e.target.value })}
          />
          <TextField
            label="Opening Hours"
            variant="standard"
            placeholder="e.g. 9:00 AM - 11:00 PM"
            value={form.openingHours}
            onChange={(e) => update({ openingHours: e.target.value })}
          />
          <FormControlLabel
            control={
              <Switch checked={form.active} onChange={(e) => update({ active: e.target.checked })} />
            }
            label="Active"
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button
          variant="contained"
          color="primary"
          startIcon={<SaveIcon />}
          disabled={validate()}
          onClick={handleSubmit}
        >
          {isEdit ? 'Update' : 'Add'}
        </Button>
        <Button variant="contained" color="error" startIcon={<CancelIcon />} onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </>
  );
}
