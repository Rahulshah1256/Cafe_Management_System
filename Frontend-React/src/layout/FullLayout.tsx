import { useState } from 'react';
import {
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Drawer,
  Box,
  Icon,
  useMediaQuery,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import { Outlet } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';

const drawerWidth = 250;

export default function FullLayout() {
  const isDesktop = useMediaQuery('(min-width:768px)');
  const [mobileOpen, setMobileOpen] = useState(false);

  const toggle = () => setMobileOpen((prev) => !prev);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="fixed" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar>
          <IconButton color="inherit" edge="start" onClick={toggle} sx={{ mr: 1 }}>
            <MenuIcon />
          </IconButton>
          <Icon sx={{ mr: 1 }}>storefront</Icon>
          <Typography variant="h6" noWrap fontWeight={700} sx={{ flexGrow: 1, fontFamily: 'Poppins, sans-serif' }}>
            Cafe Management System
          </Typography>
          <Header />
        </Toolbar>
      </AppBar>

      <Drawer
        variant={isDesktop ? 'persistent' : 'temporary'}
        open={isDesktop ? true : mobileOpen}
        onClose={toggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
            border: 'none',
            bgcolor: '#fff',
            boxShadow: '2px 0 16px rgba(42,31,26,0.06)',
          },
        }}
      >
        <Toolbar />
        <Box sx={{ overflow: 'auto', py: 1 }}>
          <Sidebar />
        </Box>
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          mt: 8,
          ml: isDesktop ? `${drawerWidth}px` : 0,
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
}
