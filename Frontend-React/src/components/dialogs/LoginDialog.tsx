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
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import LoginIcon from '@mui/icons-material/Login';
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

export default function LoginDialog({ onClose }: Props) {
  const navigate = useNavigate();
  const { openSnackBar } = useSnackbarService();
  const [hide, setHide] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const dirty = email !== '' || password !== '';

  const handleSubmit = () => {
    UserService.login({ email, password })
      .then((response: any) => {
        onClose();
        localStorage.setItem('token', response.data.token);
        navigate('/cafe/dashboard');
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
          <Typography sx={{ flex: 1 }}>Login</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        <Box display="flex" flexDirection="column">
          <TextField
            label="Email"
            variant="standard"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <TextField
            label="Password"
            variant="standard"
            required
            type={hide ? 'password' : 'text'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton onClick={() => setHide(!hide)}>
                    {hide ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button
          variant="contained"
          color="primary"
          startIcon={<LoginIcon />}
          disabled={!dirty}
          onClick={handleSubmit}
        >
          Login
        </Button>
        <Button variant="contained" color="error" startIcon={<CancelIcon />} onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </>
  );
}
