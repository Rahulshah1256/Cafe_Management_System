import { useState } from 'react';
import { IconButton, Menu, MenuItem, ListItemIcon, Dialog, Tooltip } from '@mui/material';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import PasswordIcon from '@mui/icons-material/Password';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import LightModeIcon from '@mui/icons-material/LightMode';
import { useNavigate } from 'react-router-dom';
import ChangePasswordDialog from '../components/dialogs/ChangePasswordDialog';
import ConfirmationDialog from '../components/dialogs/ConfirmationDialog';
import NotificationBell from '../components/NotificationBell';
import { useThemeMode } from '../shared/ThemeModeContext';

export default function Header() {
  const navigate = useNavigate();
  const { mode, toggleMode } = useThemeMode();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [changePasswordOpen, setChangePasswordOpen] = useState(false);
  const [logoutOpen, setLogoutOpen] = useState(false);

  const closeMenu = () => setAnchorEl(null);

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <>
      <Tooltip title={mode === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}>
        <IconButton color="inherit" onClick={toggleMode}>
          {mode === 'dark' ? <LightModeIcon /> : <DarkModeIcon />}
        </IconButton>
      </Tooltip>
      <NotificationBell />
      <IconButton color="inherit" onClick={(e) => setAnchorEl(e.currentTarget)}>
        <AccountCircleIcon />
      </IconButton>
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={closeMenu}>
        <MenuItem
          onClick={() => {
            closeMenu();
            setChangePasswordOpen(true);
          }}
        >
          <ListItemIcon>
            <PasswordIcon fontSize="small" />
          </ListItemIcon>
          change password
        </MenuItem>
        <MenuItem
          onClick={() => {
            closeMenu();
            setLogoutOpen(true);
          }}
        >
          <ListItemIcon>
            <ExitToAppIcon fontSize="small" />
          </ListItemIcon>
          Logout
        </MenuItem>
      </Menu>

      <Dialog
        open={changePasswordOpen}
        onClose={() => setChangePasswordOpen(false)}
        fullWidth
        maxWidth="sm"
      >
        <ChangePasswordDialog onClose={() => setChangePasswordOpen(false)} />
      </Dialog>

      <Dialog open={logoutOpen} onClose={() => setLogoutOpen(false)} fullWidth maxWidth="xs">
        <ConfirmationDialog
          message="Logout"
          onConfirm={handleLogout}
          onClose={() => setLogoutOpen(false)}
        />
      </Dialog>
    </>
  );
}
