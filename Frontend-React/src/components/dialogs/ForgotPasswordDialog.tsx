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
import SendIcon from '@mui/icons-material/Send';
import CancelIcon from '@mui/icons-material/Cancel';
import { UserService } from '../../services/user.service';
import { GlobalConstants } from '../../shared/globalConstants';
import { useSnackbarService } from '../../shared/useSnackbarService';

interface Props {
  onClose: () => void;
}

export default function ForgotPasswordDialog({ onClose }: Props) {
  const { openSnackBar } = useSnackbarService();
  const [email, setEmail] = useState('');
  const [registerSuccess, setRegisterSuccess] = useState(false);

  const handleSubmit = () => {
    UserService.forgotPassword({ email })
      .then((response: any) => {
        onClose();
        openSnackBar(response?.data?.message, '');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
    setRegisterSuccess(true);
  };

  return (
    <>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography sx={{ flex: 1 }}>Forgot Password</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        {!registerSuccess && (
          <Box display="flex" flexDirection="column">
            <TextField
              label="Email"
              variant="standard"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </Box>
        )}
        {registerSuccess && <h3>Email is Send Successfully</h3>}
      </DialogContent>
      <DialogActions>
        {!registerSuccess && (
          <Button
            variant="contained"
            color="primary"
            startIcon={<SendIcon />}
            disabled={email === ''}
            onClick={handleSubmit}
          >
            Send Password
          </Button>
        )}
        <Button variant="contained" color="error" startIcon={<CancelIcon />} onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </>
  );
}
