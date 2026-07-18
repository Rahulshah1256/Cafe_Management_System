import { useEffect, useState } from 'react';
import { Card, Typography, Grid, Chip, List, ListItem, ListItemText, Divider, Box, Avatar } from '@mui/material';
import CategoryIcon from '@mui/icons-material/Category';
import FastfoodIcon from '@mui/icons-material/Fastfood';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import { useNavigate } from 'react-router-dom';
import { DashboardService } from '../services/dashboard.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import { getUserRole } from '../auth/auth';

const STATUS_COLORS: Record<string, 'default' | 'info' | 'warning' | 'primary' | 'success' | 'error'> = {
  PLACED: 'info',
  ACCEPTED: 'primary',
  PREPARING: 'warning',
  OUT_FOR_DELIVERY: 'warning',
  DELIVERED: 'success',
  CANCELLED: 'error',
};

const STAT_GRADIENTS = [
  'linear-gradient(135deg, #E5202A, #FF5B52)',
  'linear-gradient(135deg, #FF8A00, #FFB74D)',
  'linear-gradient(135deg, #7C3AED, #A78BFA)',
];

export default function Dashboard() {
  const navigate = useNavigate();
  const { openSnackBar } = useSnackbarService();
  const isAdmin = getUserRole() === 'admin';
  const [data, setData] = useState<any>(null);
  const [analytics, setAnalytics] = useState<any>(null);

  useEffect(() => {
    DashboardService.getDetails()
      .then((response: any) => setData(response.data))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });

    if (isAdmin) {
      DashboardService.getAnalytics()
        .then((response: any) => setAnalytics(response.data))
        .catch(() => {
          // Analytics are a nice-to-have on this page; a failure here shouldn't block the rest of the dashboard.
        });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const cards = [
    { label: 'Our Categories', value: data?.category, action: 'View Category', path: '/cafe/category', icon: <CategoryIcon /> },
    { label: 'Our Products', value: data?.product, action: 'View Product', path: '/cafe/product', icon: <FastfoodIcon /> },
    { label: 'Our Bills', value: data?.bill, action: 'View Bill', path: '/cafe/bill', icon: <ReceiptLongIcon /> },
  ];

  return (
    <div className="dashboard">
      <Box
        sx={{
          p: 3,
          mb: 3,
          borderRadius: 4,
          color: '#fff',
          backgroundImage: 'linear-gradient(120deg, #E5202A 0%, #FF8A00 100%)',
          boxShadow: '0 12px 30px rgba(233,30,99,0.25)',
        }}
      >
        <Typography variant="h5" fontWeight={800} sx={{ fontFamily: 'Poppins, sans-serif' }}>
          Welcome back! 👋
        </Typography>
        <Typography variant="body2" sx={{ opacity: 0.9, mt: 0.5 }}>
          Here's what's happening with your cafe today.
        </Typography>
      </Box>

      <div className="row">
        {cards.map((card, i) => (
          <div className="column" key={card.label}>
            <div className="stat-card" style={{ backgroundImage: STAT_GRADIENTS[i % STAT_GRADIENTS.length] }}>
              <div className="stat-icon">{card.icon}</div>
              <p className="stat-label">{card.label}</p>
              <p className="stat-value">{card.value ?? '-'}</p>
              <button className="dash-button" onClick={() => navigate(card.path)}>
                {card.action} →
              </button>
            </div>
          </div>
        ))}
      </div>

      {isAdmin && analytics && (
        <>
          <br />
          <Card sx={{ p: 3 }}>
            <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
              <Avatar sx={{ bgcolor: 'primary.main', width: 32, height: 32 }}>
                <TrendingUpIcon fontSize="small" />
              </Avatar>
              <Typography fontWeight={700}>Sales Analytics</Typography>
            </Box>
            <Grid container spacing={2}>
              {[
                { label: 'Today', orders: analytics.todayOrders, revenue: analytics.todayRevenue },
                { label: 'Last 7 Days', orders: analytics.weekOrders, revenue: analytics.weekRevenue },
                { label: 'Last 30 Days', orders: analytics.monthOrders, revenue: analytics.monthRevenue },
              ].map((period) => (
                <Grid item xs={12} sm={4} key={period.label}>
                  <Card
                    variant="outlined"
                    sx={{ p: 2, textAlign: 'center', borderColor: 'rgba(252,106,3,0.2)', bgcolor: 'rgba(252,106,3,0.04)' }}
                  >
                    <Typography variant="subtitle2" color="text.secondary">
                      {period.label}
                    </Typography>
                    <Typography variant="h5" color="primary.dark" fontWeight={800}>
                      Rs. {period.revenue?.toFixed(0)}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {period.orders} order{period.orders === 1 ? '' : 's'}
                    </Typography>
                  </Card>
                </Grid>
              ))}
            </Grid>

            <Divider sx={{ my: 2 }} />

            <Typography fontWeight={700} gutterBottom>
              Orders by Status
            </Typography>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {Object.entries(analytics.orderStatusCounts || {}).map(([status, count]) => (
                <Chip
                  key={status}
                  label={`${status.replace(/_/g, ' ')}: ${count}`}
                  color={STATUS_COLORS[status] || 'default'}
                  size="small"
                />
              ))}
            </div>

            <Divider sx={{ my: 2 }} />

            <Typography fontWeight={700} gutterBottom>
              Top Selling Products
            </Typography>
            <List dense>
              {(analytics.topProducts || []).length === 0 && (
                <Typography variant="body2" color="text.secondary">
                  No sales data yet.
                </Typography>
              )}
              {(analytics.topProducts || []).map((p: any) => (
                <ListItem key={p.name} disableGutters>
                  <ListItemText primary={p.name} secondary={`${p.quantitySold} sold - Rs. ${p.revenue?.toFixed(0)} revenue`} />
                </ListItem>
              ))}
            </List>
          </Card>
        </>
      )}
    </div>
  );
}
