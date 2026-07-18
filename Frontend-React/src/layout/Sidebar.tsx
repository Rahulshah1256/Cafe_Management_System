import { List, ListItemButton, ListItemIcon, ListItemText, Icon } from '@mui/material';
import { NavLink } from 'react-router-dom';
import { MENUITEMS } from '../shared/menuItems';
import { getUserRole } from '../auth/auth';

export default function Sidebar() {
  const userRole = getUserRole();

  return (
    <List sx={{ px: 1.5 }}>
      {MENUITEMS.filter(
        (item) =>
          item.type === 'link' &&
          (item.role === userRole || (item.role === '' && userRole !== 'delivery'))
      ).map((item) => (
        <ListItemButton
          key={item.state}
          component={NavLink}
          to={`/cafe/${item.state}`}
          sx={{
            borderRadius: 999,
            mb: 0.5,
            color: 'text.primary',
            '&:hover': {
              bgcolor: 'rgba(252, 106, 3, 0.08)',
            },
            '&.active': {
              backgroundImage: 'linear-gradient(90deg, #FC6A03, #E91E63)',
              color: '#fff',
              boxShadow: '0 4px 14px rgba(252,106,3,0.3)',
              '& .MuiListItemIcon-root': { color: '#fff' },
            },
          }}
        >
          <ListItemIcon sx={{ minWidth: 40 }}>
            <Icon>{item.icon}</Icon>
          </ListItemIcon>
          <ListItemText primary={item.name} primaryTypographyProps={{ fontWeight: 600, fontSize: '0.92rem' }} />
        </ListItemButton>
      ))}
    </List>
  );
}
