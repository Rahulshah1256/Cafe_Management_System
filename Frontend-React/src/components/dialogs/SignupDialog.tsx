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
import { UserService } from '../../services/user.service';
import { GlobalConstants } from '../../shared/globalConstants';
import { useSnackbarService } from '../../shared/useSnackbarService';

interface Props {
  onClose: () => void;
}

export default function SignupDialog({ onClose }: Props) {
  const { openSnackBar } = useSnackbarService();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [step, setStep] = useState<'form' | 'otp'>('form');
  const [otp, setOtp] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [resending, setResending] = useState(false);
  const [form, setForm] = useState({
    name: '',
    email: '',
    contactNumber: '',
    password: '',
    confirmPassword: '',
  });

  const update = (key: string, value: string) => setForm({ ...form, [key]: value });

  const passwordMismatch = form.password !== form.confirmPassword;
  const dirty = Object.values(form).some((v) => v !== '');
  const valid =
    form.name !== '' &&
    form.email !== '' &&
    form.contactNumber !== '' &&
    form.password !== '' &&
    form.confirmPassword !== '';

  const handleSubmit = () => {
    setSubmitting(true);
    UserService.signup({
      name: form.name,
      email: form.email,
      contactNumber: form.contactNumber,
      password: form.password,
    })
      .then((response: any) => {
        openSnackBar(response?.data?.message, '');
        setStep('otp');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      })
      .finally(() => setSubmitting(false));
  };

  const handleVerifyOtp = () => {
    setSubmitting(true);
    UserService.verifySignupOtp({ email: form.email, otp })
      .then((response: any) => {
        openSnackBar(response?.data?.message, '');
        onClose();
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      })
      .finally(() => setSubmitting(false));
  };

  const handleResendOtp = () => {
    setResending(true);
    UserService.resendSignupOtp({ email: form.email })
      .then((response: any) => {
        openSnackBar(response?.data?.message, '');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      })
      .finally(() => setResending(false));
  };

  return (
    <>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography sx={{ flex: 1 }}>SignUp</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      {step === 'form' ? (
        <>
          <DialogContent>
            <Box display="flex" flexDirection="column">
              <TextField
                label="Name"
                variant="standard"
                required
                value={form.name}
                onChange={(e) => update('name', e.target.value)}
              />
              <TextField
                label="Contact Number"
                variant="standard"
                required
                value={form.contactNumber}
                onChange={(e) => update('contactNumber', e.target.value)}
              />
              <TextField
                label="Email"
                variant="standard"
                required
                value={form.email}
                onChange={(e) => update('email', e.target.value)}
              />
              <TextField
                label="Password"
                variant="standard"
                required
                type={showPassword ? 'text' : 'password'}
                value={form.password}
                onChange={(e) => update('password', e.target.value)}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton onClick={() => setShowPassword(!showPassword)}>
                        {showPassword ? <Visibility /> : <VisibilityOff />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
              <TextField
                label="confirmPassword"
                variant="standard"
                required
                type={showConfirm ? 'text' : 'password'}
                value={form.confirmPassword}
                onChange={(e) => update('confirmPassword', e.target.value)}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton onClick={() => setShowConfirm(!showConfirm)}>
                        {showConfirm ? <Visibility /> : <VisibilityOff />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
              {passwordMismatch && form.confirmPassword !== '' && (
                <FormHelperText>New Password &amp; Confirm Password does not match.</FormHelperText>
              )}
            </Box>
          </DialogContent>
          <DialogActions>
            <Button
              variant="contained"
              color="primary"
              startIcon={<SaveIcon />}
              disabled={passwordMismatch || !(valid && dirty) || submitting}
              onClick={handleSubmit}
            >
              {submitting ? 'Sending OTP...' : 'Signup'}
            </Button>
            <Button variant="contained" color="error" startIcon={<CancelIcon />} onClick={onClose}>
              Close
            </Button>
          </DialogActions>
        </>
      ) : (
        <>
          <DialogContent>
            <Box display="flex" flexDirection="column" gap={1}>
              <Typography variant="body2" color="text.secondary">
                We've sent a 6-digit verification code to <strong>{form.email}</strong>. Enter it
                below to complete your registration. The code expires in 10 minutes.
              </Typography>
              <TextField
                label="Enter OTP"
                variant="standard"
                required
                inputProps={{ maxLength: 6, inputMode: 'numeric', pattern: '[0-9]*' }}
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
              />
              <Button size="small" onClick={handleResendOtp} disabled={resending}>
                {resending ? 'Resending...' : "Didn't get the code? Resend OTP"}
              </Button>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button
              variant="contained"
              color="primary"
              startIcon={<SaveIcon />}
              disabled={otp.length !== 6 || submitting}
              onClick={handleVerifyOtp}
            >
              {submitting ? 'Verifying...' : 'Verify & Create Account'}
            </Button>
            <Button variant="outlined" color="secondary" onClick={() => setStep('form')}>
              Back
            </Button>
            <Button variant="contained" color="error" startIcon={<CancelIcon />} onClick={onClose}>
              Close
            </Button>
          </DialogActions>
        </>
      )}
    </>
  );
}
