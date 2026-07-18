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
  MenuItem,
  Box,
  FormControlLabel,
  Switch,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { CouponService } from '../../services/coupon.service';
import { GlobalConstants } from '../../shared/globalConstants';
import { useSnackbarService } from '../../shared/useSnackbarService';

interface Props {
  action: 'Add' | 'Edit';
  data?: any;
  onClose: () => void;
  onSuccess: () => void;
}

export default function CouponDialog({ action, data, onClose, onSuccess }: Props) {
  const { openSnackBar } = useSnackbarService();
  const isEdit = action === 'Edit';
  const [form, setForm] = useState<any>({
    code: data?.code ?? '',
    description: data?.description ?? '',
    discountType: data?.discountType ?? 'PERCENTAGE',
    discountValue: data?.discountValue ?? '',
    maxDiscountAmount: data?.maxDiscountAmount ?? '',
    minOrderAmount: data?.minOrderAmount ?? 0,
    expiryDate: data?.expiryDate ?? '',
    active: data?.active ?? true,
    usageLimit: data?.usageLimit ?? '',
  });

  const update = (patch: any) => setForm((prev: any) => ({ ...prev, ...patch }));

  const validate = () => {
    return !form.code || !form.discountType || form.discountValue === '' || form.discountValue === null;
  };

  const payload = () => ({
    code: form.code,
    description: form.description,
    discountType: form.discountType,
    discountValue: Number(form.discountValue),
    maxDiscountAmount: form.maxDiscountAmount === '' ? null : Number(form.maxDiscountAmount),
    minOrderAmount: form.minOrderAmount === '' ? 0 : Number(form.minOrderAmount),
    expiryDate: form.expiryDate || null,
    active: form.active,
    usageLimit: form.usageLimit === '' ? null : Number(form.usageLimit),
  });

  const handleSubmit = () => {
    const request = isEdit
      ? CouponService.updateCoupon(data.id, payload())
      : CouponService.addCoupon(payload());

    request
      .then((response: any) => {
        onClose();
        onSuccess();
        openSnackBar(response?.data?.message || 'Success', 'success');
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
          <Typography sx={{ flex: 1 }}>{action} Coupon</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        <Box display="flex" flexDirection="column" gap={2} sx={{ mt: 1 }}>
          <TextField
            label="Coupon Code"
            variant="standard"
            required
            value={form.code}
            onChange={(e) => update({ code: e.target.value.toUpperCase() })}
          />
          <TextField
            label="Description"
            variant="standard"
            value={form.description}
            onChange={(e) => update({ description: e.target.value })}
          />
          <TextField
            label="Discount Type"
            variant="standard"
            select
            value={form.discountType}
            onChange={(e) => update({ discountType: e.target.value })}
          >
            <MenuItem value="PERCENTAGE">Percentage</MenuItem>
            <MenuItem value="FLAT">Flat</MenuItem>
          </TextField>
          <TextField
            label="Discount Value"
            variant="standard"
            required
            type="number"
            value={form.discountValue}
            onChange={(e) => update({ discountValue: e.target.value })}
          />
          <TextField
            label="Max Discount Amount (for percentage)"
            variant="standard"
            type="number"
            value={form.maxDiscountAmount}
            onChange={(e) => update({ maxDiscountAmount: e.target.value })}
          />
          <TextField
            label="Minimum Order Amount"
            variant="standard"
            type="number"
            value={form.minOrderAmount}
            onChange={(e) => update({ minOrderAmount: e.target.value })}
          />
          <TextField
            label="Expiry Date"
            variant="standard"
            type="date"
            InputLabelProps={{ shrink: true }}
            value={form.expiryDate}
            onChange={(e) => update({ expiryDate: e.target.value })}
          />
          <TextField
            label="Usage Limit"
            variant="standard"
            type="number"
            value={form.usageLimit}
            onChange={(e) => update({ usageLimit: e.target.value })}
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
