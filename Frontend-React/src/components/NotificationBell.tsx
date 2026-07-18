import { useEffect, useState } from 'react';
import {
  IconButton,
  Menu,
  MenuItem,
  Badge,
  Typography,
  Box,
  Divider,
  Button,
} from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';
import { NotificationService } from '../services/notification.service';

const POLL_INTERVAL_MS = 30000;

export default function NotificationBell() {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const loadUnreadCount = () => {
    NotificationService.getUnreadCount()
      .then((response: any) => setUnreadCount(response?.data?.unreadCount || 0))
      .catch(() => {});
  };

  const loadNotifications = () => {
    NotificationService.getMyNotifications()
      .then((response: any) => setNotifications(response.data || []))
      .catch(() => {});
  };

  useEffect(() => {
    loadUnreadCount();
    const interval = setInterval(loadUnreadCount, POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, []);

  const openMenu = (e: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(e.currentTarget);
    loadNotifications();
  };

  const closeMenu = () => setAnchorEl(null);

  const markAsRead = (id: any) => {
    NotificationService.markAsRead(id).then(() => {
      loadNotifications();
      loadUnreadCount();
    });
  };

  const markAllAsRead = () => {
    NotificationService.markAllAsRead().then(() => {
      loadNotifications();
      loadUnreadCount();
    });
  };

  return (
    <>
      <IconButton color="inherit" onClick={openMenu}>
        <Badge badgeContent={unreadCount} color="error">
          <NotificationsIcon />
        </Badge>
      </IconButton>
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={closeMenu}
        PaperProps={{ sx: { width: 360, maxHeight: 420 } }}
      >
        <Box sx={{ px: 2, py: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="subtitle1">
            <b>Notifications</b>
          </Typography>
          {unreadCount > 0 && (
            <Button size="small" onClick={markAllAsRead}>
              Mark all read
            </Button>
          )}
        </Box>
        <Divider />
        {notifications.length === 0 ? (
          <MenuItem disabled>No notifications yet</MenuItem>
        ) : (
          notifications.map((n) => (
            <MenuItem
              key={n.id}
              onClick={() => !n.isRead && markAsRead(n.id)}
              sx={{ whiteSpace: 'normal', alignItems: 'flex-start', bgcolor: n.isRead ? 'transparent' : 'action.hover' }}
            >
              <Box>
                <Typography variant="body2" fontWeight={n.isRead ? 400 : 700}>
                  {n.title}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {n.message}
                </Typography>
              </Box>
            </MenuItem>
          ))
        )}
      </Menu>
    </>
  );
}
