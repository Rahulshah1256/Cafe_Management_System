import { useEffect, useState } from 'react';
import { Card, Typography, Grid, Chip, List, ListItem, ListItemText, Button, Divider } from '@mui/material';
import { DashboardService } from '../services/dashboard.service';
import { BillService } from '../services/bill.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';

// Kitchen queue moves forward through this sequence; the "advance" button always
// bumps an order to the next step (CANCELLED is a separate side-branch handled elsewhere).
const NEXT_STATUS: Record<string, string | null> = {
  PLACED: 'ACCEPTED',
  ACCEPTED: 'PREPARING',
  PREPARING: 'OUT_FOR_DELIVERY',
  OUT_FOR_DELIVERY: 'DELIVERED',
};

const STATUS_COLORS: Record<string, 'info' | 'primary' | 'warning' | 'success' | 'default'> = {
  PLACED: 'info',
  ACCEPTED: 'primary',
  PREPARING: 'warning',
  OUT_FOR_DELIVERY: 'warning',
};

function elapsedMinutes(createdAt: string | null): number | null {
  if (!createdAt) return null;
  return Math.max(0, Math.round((Date.now() - new Date(createdAt).getTime()) / 60000));
}

export default function KitchenDashboard() {
  const { openSnackBar } = useSnackbarService();
  const [queue, setQueue] = useState<any[]>([]);

  const load = () => {
    DashboardService.getKitchenQueue()
      .then((response: any) => setQueue(response.data || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  useEffect(() => {
    load();
    // Live-ish view: refresh the queue automatically so kitchen staff don't have to reload.
    const interval = setInterval(load, 15000);
    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const advance = (order: any) => {
    const next = NEXT_STATUS[order.orderStatus];
    if (!next) return;
    BillService.updateOrderStatus(order.id, next)
      .then(() => {
        openSnackBar(`Order ${order.uuid} moved to ${next.replace(/_/g, ' ')}`, 'success');
        load();
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  return (
    <>
      <Card sx={{ p: 2 }}>
        <b>Kitchen Dashboard</b>
        <Typography variant="body2" color="text.secondary">
          Live queue of in-progress orders, oldest first. Refreshes automatically every 15s.
        </Typography>
      </Card>
      <Divider sx={{ my: 2 }} />

      {queue.length === 0 && (
        <Card sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="text.secondary">No orders in the kitchen queue right now.</Typography>
        </Card>
      )}

      <Grid container spacing={2}>
        {queue.map((order) => {
          const next = NEXT_STATUS[order.orderStatus];
          const minutes = elapsedMinutes(order.createdAt);
          return (
            <Grid item xs={12} sm={6} md={4} key={order.id}>
              <Card sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="subtitle2">{order.uuid}</Typography>
                  <Chip
                    size="small"
                    label={order.orderStatus?.replace(/_/g, ' ')}
                    color={STATUS_COLORS[order.orderStatus] || 'default'}
                  />
                </div>
                <Typography variant="body2" color="text.secondary">
                  {order.createdBy} - {order.contactNumber}
                </Typography>
                {minutes !== null && (
                  <Typography variant="caption" color={minutes > 20 ? 'error' : 'text.secondary'}>
                    {minutes} min ago
                  </Typography>
                )}

                <List dense sx={{ flexGrow: 1 }}>
                  {order.items?.map((item: any, idx: number) => (
                    <ListItem key={idx} disableGutters>
                      <ListItemText primary={`${item.quantity} x ${item.name}`} />
                    </ListItem>
                  ))}
                </List>

                <Typography variant="body2" fontWeight="bold">
                  Total: Rs. {order.total}
                </Typography>

                {next && (
                  <Button variant="contained" size="small" sx={{ mt: 1 }} onClick={() => advance(order)}>
                    Mark as {next.replace(/_/g, ' ')}
                  </Button>
                )}
              </Card>
            </Grid>
          );
        })}
      </Grid>
    </>
  );
}
