import { useEffect, useState } from 'react';
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
  MenuItem,
  FormControlLabel,
  Switch,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
import { GlobalConstants } from '../../shared/globalConstants';
import { useSnackbarService } from '../../shared/useSnackbarService';

interface Props {
  action: 'Add' | 'Edit';
  data?: any;
  onClose: () => void;
  onSuccess: () => void;
}

const SPICY_LEVELS = ['NONE', 'MILD', 'MEDIUM', 'HOT'];

export default function ProductDialog({ action, data, onClose, onSuccess }: Props) {
  const { openSnackBar } = useSnackbarService();
  const [categorys, setCategorys] = useState<any[]>([]);
  const [form, setForm] = useState({
    name: data?.name ?? '',
    categoryId: data?.categoryId ?? '',
    price: data?.price ?? '',
    description: data?.description ?? '',
    isVeg: data?.isVeg ?? true,
    spicyLevel: data?.spicyLevel ?? 'NONE',
    bestSeller: data?.bestSeller ?? false,
    newArrival: data?.newArrival ?? false,
    imageUrl: data?.imageUrl ?? '',
    prepTimeMinutes: data?.prepTimeMinutes ?? '',
  });
  const [dirty, setDirty] = useState(false);

  const isEdit = action === 'Edit';
  const buttonLabel = isEdit ? 'Update' : 'Add';

  useEffect(() => {
    CategoryService.getCategorys()
      .then((response: any) => setCategorys(response.data || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const update = (key: string, value: any) => {
    setForm({ ...form, [key]: value });
    setDirty(true);
  };

  const valid =
    form.name !== '' && form.categoryId !== '' && form.price !== '' && form.description !== '';

  const handleSubmit = () => {
    const payload: any = {
      name: form.name,
      categoryId: form.categoryId,
      price: form.price,
      description: form.description,
      isVeg: String(form.isVeg),
      spicyLevel: form.spicyLevel,
      bestSeller: String(form.bestSeller),
      newArrival: String(form.newArrival),
      imageUrl: form.imageUrl,
      prepTimeMinutes: form.prepTimeMinutes,
    };
    const request = isEdit
      ? ProductService.update({ ...payload, id: data.id })
      : ProductService.add(payload);

    request
      .then((response: any) => {
        onClose();
        onSuccess();
        openSnackBar(response?.data?.message, 'success');
      })
      .catch((error: any) => {
        onClose();
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  return (
    <>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography sx={{ flex: 1 }}>{action} Product</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        <Box display="flex" flexDirection="column" gap={1}>
          <TextField
            label="Name"
            variant="standard"
            required
            value={form.name}
            onChange={(e) => update('name', e.target.value)}
          />
          <TextField
            label="Price"
            variant="standard"
            required
            value={form.price}
            onChange={(e) => update('price', e.target.value)}
          />
          <TextField
            label="Category"
            variant="standard"
            select
            value={form.categoryId}
            onChange={(e) => update('categoryId', e.target.value)}
          >
            {categorys.map((category) => (
              <MenuItem key={category.id} value={category.id}>
                {category.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Description"
            variant="standard"
            required
            value={form.description}
            onChange={(e) => update('description', e.target.value)}
          />
          <TextField
            label="Image URL"
            variant="standard"
            value={form.imageUrl}
            onChange={(e) => update('imageUrl', e.target.value)}
          />
          <TextField
            label="Preparation Time (minutes)"
            variant="standard"
            value={form.prepTimeMinutes}
            onChange={(e) => update('prepTimeMinutes', e.target.value)}
          />
          <TextField
            label="Spicy Level"
            variant="standard"
            select
            value={form.spicyLevel}
            onChange={(e) => update('spicyLevel', e.target.value)}
          >
            {SPICY_LEVELS.map((level) => (
              <MenuItem key={level} value={level}>
                {level}
              </MenuItem>
            ))}
          </TextField>
          <Box display="flex" gap={2} flexWrap="wrap">
            <FormControlLabel
              control={
                <Switch
                  checked={form.isVeg}
                  onChange={(e) => update('isVeg', e.target.checked)}
                />
              }
              label="Vegetarian"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={form.bestSeller}
                  onChange={(e) => update('bestSeller', e.target.checked)}
                />
              }
              label="Best Seller"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={form.newArrival}
                  onChange={(e) => update('newArrival', e.target.checked)}
                />
              }
              label="New Arrival"
            />
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button
          variant="contained"
          color="primary"
          startIcon={<SaveIcon />}
          disabled={!(valid && dirty)}
          onClick={handleSubmit}
        >
          {buttonLabel}
        </Button>
        <Button variant="contained" color="error" startIcon={<CancelIcon />} onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </>
  );
}
