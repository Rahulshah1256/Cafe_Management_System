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
  InputAdornment,
  Box,
  FormHelperText,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { useNavigate } from 'react-router-dom';
import { UserService } from '../../services/user.service';
import { GlobalConstants } from '../../shared/globalConstants';
import { useSnackbarService } from '../../shared/useSnackbarService';

interface Props {
  onClose: () => void;
}

export default function ChangePasswordDialog({ onClose }: Props) {
  const navigate = useNavigate();
  const { openSnackBar } = useSnackbarService();
  const [showOld, setShowOld] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [form, setForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const update = (key: string, value: string) => setForm({ ...form, [key]: value });

  const mismatch = form.newPassword !== form.confirmPassword;
  const valid =
    form.oldPassword !== '' && form.newPassword !== '' && form.confirmPassword !== '';

  const handleSubmit = () => {
    UserService.changePassword(form)
      .then((response: any) => {
        onClose();
        navigate('/cafe/dashboard');
        openSnackBar(response?.data?.message, 'success');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message;
        if (message) {
          openSnackBar(message, GlobalConstants.error);
        } else {
          openSnackBar(GlobalConstants.genericError, GlobalConstants.error);
        }
      });
  };

  const passwordField = (
    label: string,
    key: 'oldPassword' | 'newPassword' | 'confirmPassword',
    show: boolean,
    setShow: (v: boolean) => void
  ) => (
    <TextField
      label={label}
      variant="standard"
      required
      type={show ? 'text' : 'password'}
      value={form[key]}
      onChange={(e) => update(key, e.target.value)}
      InputProps={{
        endAdornment: (
          <InputAdornment position="end">
            <IconButton onClick={() => setShow(!show)}>
              {show ? <Visibility /> : <VisibilityOff />}
            </IconButton>
          </InputAdornment>
        ),
      }}
    />
  );

  return (
    <>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography sx={{ flex: 1 }}>Change Password</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        <Box display="flex" flexDirection="column">
          {passwordField('Old Password', 'oldPassword', showOld, setShowOld)}
          {passwordField('New Password', 'newPassword', showNew, setShowNew)}
          {passwordField('Confirm Password', 'confirmPassword', showConfirm, setShowConfirm)}
          {mismatch && form.confirmPassword !== '' && (
            <FormHelperText>New Password &amp; Confirm Password does not match.</FormHelperText>
          )}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button
          variant="contained"
          color="primary"
          startIcon={<SaveIcon />}
          disabled={mismatch || !valid}
          onClick={handleSubmit}
        >
          Update
        </Button>
        <Button variant="contained" color="error" startIcon={<CancelIcon />} onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </>
  );
}
