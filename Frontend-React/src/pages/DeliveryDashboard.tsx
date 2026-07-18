import { useEffect, useState } from 'react';
import { Card, Typography, Grid, Chip, Button, Divider, TextField, MenuItem, Box } from '@mui/material';
import { DeliveryService } from '../services/delivery.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';

const AVAILABILITY_OPTIONS = ['AVAILABLE', 'BUSY', 'OFFLINE'];

const STATUS_COLORS: Record<string, 'info' | 'primary' | 'warning' | 'default'> = {
  PLACED: 'info',
  ACCEPTED: 'primary',
  PREPARING: 'warning',
  OUT_FOR_DELIVERY: 'warning',
};

export default function DeliveryDashboard() {
  const { openSnackBar } = useSnackbarService();
  const [deliveries, setDeliveries] = useState<any[]>([]);
  const [availability, setAvailability] = useState('OFFLINE');

  const load = () => {
    DeliveryService.getMyDeliveries()
      .then((response: any) => setDeliveries(response.data || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  useEffect(() => {
    load();
    const interval = setInterval(load, 15000);
    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const changeAvailability = (status: string) => {
    DeliveryService.updateAvailability(status)
      .then((response: any) => {
        setAvailability(response?.data?.availability || status);
        openSnackBar('Availability updated to ' + status, 'success');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const completeDelivery = (billId: any) => {
    DeliveryService.completeDelivery(billId)
      .then(() => {
        openSnackBar('Order marked as delivered', 'success');
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
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
          <div>
            <b>My Deliveries</b>
            <Typography variant="body2" color="text.secondary">
              Your active assigned orders, oldest first. Refreshes automatically every 15s.
            </Typography>
          </div>
          <TextField
            select
            label="My Availability"
            variant="standard"
            size="small"
            value={availability}
            onChange={(e) => changeAvailability(e.target.value)}
            sx={{ minWidth: 160 }}
          >
            {AVAILABILITY_OPTIONS.map((s) => (
              <MenuItem key={s} value={s}>
                {s}
              </MenuItem>
            ))}
          </TextField>
        </Box>
      </Card>
      <Divider sx={{ my: 2 }} />

      {deliveries.length === 0 && (
        <Card sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="text.secondary">
            No active deliveries assigned to you right now.
          </Typography>
        </Card>
      )}

      <Grid container spacing={2}>
        {deliveries.map((order) => (
          <Grid item xs={12} sm={6} md={4} key={order.billId}>
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
                {order.customerName} - {order.contactNumber}
              </Typography>
              <Typography variant="body2" sx={{ mt: 1, flexGrow: 1 }}>
                {order.deliveryAddress}
              </Typography>
              <Typography variant="body2" fontWeight="bold">
                Total: Rs. {order.total}
              </Typography>
              <Button variant="contained" size="small" sx={{ mt: 1 }} onClick={() => completeDelivery(order.billId)}>
                Mark as Delivered
              </Button>
            </Card>
          </Grid>
        ))}
      </Grid>
    </>
  );
}
