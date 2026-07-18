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
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { CategoryService } from '../../services/category.service';
import { GlobalConstants } from '../../shared/globalConstants';
import { useSnackbarService } from '../../shared/useSnackbarService';

interface Props {
  action: 'Add' | 'Edit';
  data?: any;
  onClose: () => void;
  onSuccess: () => void;
}

export default function CategoryDialog({ action, data, onClose, onSuccess }: Props) {
  const { openSnackBar } = useSnackbarService();
  const [name, setName] = useState(data?.name ?? '');
  const [dirty, setDirty] = useState(false);

  const isEdit = action === 'Edit';
  const buttonLabel = isEdit ? 'Update' : 'Add';

  const handleSubmit = () => {
    if (isEdit) {
      edit();
    } else {
      add();
    }
  };

  const add = () => {
    CategoryService.add({ name })
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

  const edit = () => {
    CategoryService.update({ id: data.id, name })
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
          <Typography sx={{ flex: 1 }}>{action} Category</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        <Box display="flex" flexDirection="column">
          <TextField
            label="Name"
            variant="standard"
            required
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              setDirty(true);
            }}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button
          variant="contained"
          color="primary"
          startIcon={<SaveIcon />}
          disabled={!(name !== '' && dirty)}
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
