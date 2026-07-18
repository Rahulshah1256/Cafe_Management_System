import { useState } from 'react';
import { AppBar, Toolbar, Typography, IconButton, DialogContent, DialogActions, Button, TextField, Box } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { DeliveryService } from '../../services/delivery.service';
import { GlobalConstants } from '../../shared/globalConstants';
import { useSnackbarService } from '../../shared/useSnackbarService';

interface Props {
  onClose: () => void;
  onSuccess: () => void;
}

export default function RegisterDeliveryPartnerDialog({ onClose, onSuccess }: Props) {
  const { openSnackBar } = useSnackbarService();
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    contactNumber: '',
    vehicleNumber: '',
  });

  const update = (patch: Partial<typeof form>) => setForm((prev) => ({ ...prev, ...patch }));

  const validate = () =>
    !form.name || !form.email || form.password.length < 6 || !form.contactNumber || !form.vehicleNumber;

  const handleSubmit = () => {
    DeliveryService.registerPartner(form)
      .then(() => {
        onClose();
        onSuccess();
        openSnackBar('Delivery partner registered', 'success');
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
          <Typography sx={{ flex: 1 }}>Register Delivery Partner</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        <Box display="flex" flexDirection="column" gap={2} sx={{ mt: 1 }}>
          <TextField
            label="Name"
            variant="standard"
            required
            value={form.name}
            onChange={(e) => update({ name: e.target.value })}
          />
          <TextField
            label="Email"
            variant="standard"
            required
            value={form.email}
            onChange={(e) => update({ email: e.target.value })}
          />
          <TextField
            label="Password"
            variant="standard"
            type="password"
            required
            helperText="At least 6 characters"
            value={form.password}
            onChange={(e) => update({ password: e.target.value })}
          />
          <TextField
            label="Contact Number"
            variant="standard"
            required
            value={form.contactNumber}
            onChange={(e) => update({ contactNumber: e.target.value })}
          />
          <TextField
            label="Vehicle Number"
            variant="standard"
            required
            value={form.vehicleNumber}
            onChange={(e) => update({ vehicleNumber: e.target.value })}
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
          Register
        </Button>
        <Button variant="contained" color="error" startIcon={<CancelIcon />} onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </>
  );
}
