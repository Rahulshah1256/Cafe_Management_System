import { useEffect, useState } from 'react';
import {
  Card,
  Button,
  TextField,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Divider,
  Chip,
  Dialog,
  Typography,
} from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import { DeliveryService } from '../services/delivery.service';
import { BillService } from '../services/bill.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import RegisterDeliveryPartnerDialog from '../components/dialogs/RegisterDeliveryPartnerDialog';

// Orders eligible for a rider assignment - matches CafeConstants.KITCHEN_QUEUE_STATUSES minus
// PLACED/ACCEPTED (those are still being prepared, too early to assign a rider).
const ASSIGNABLE_STATUSES = ['PREPARING', 'OUT_FOR_DELIVERY'];

const AVAILABILITY_COLOR: Record<string, 'success' | 'warning' | 'default'> = {
  AVAILABLE: 'success',
  BUSY: 'warning',
  OFFLINE: 'default',
};

export default function ManageDelivery() {
  const { openSnackBar } = useSnackbarService();
  const [partners, setPartners] = useState<any[]>([]);
  const [orders, setOrders] = useState<any[]>([]);
  const [registerOpen, setRegisterOpen] = useState(false);

  const loadPartners = () => {
    DeliveryService.getAllPartners()
      .then((response: any) => setPartners(response.data || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const loadOrders = () => {
    BillService.getBills()
      .then((response: any) => {
        const assignable = (response.data || []).filter((b: any) =>
          ASSIGNABLE_STATUSES.includes(b.orderStatus)
        );
        setOrders(assignable);
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  useEffect(() => {
    loadPartners();
    loadOrders();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const assign = (billId: any, partnerEmail: string) => {
    if (!partnerEmail) return;
    DeliveryService.assignPartner(billId, partnerEmail)
      .then(() => {
        openSnackBar('Delivery partner assigned', 'success');
        loadOrders();
        loadPartners();
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  return (
    <>
      <Card sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
        <b>Delivery Management</b>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddCircleIcon />}
          sx={{ ml: 'auto' }}
          onClick={() => setRegisterOpen(true)}
        >
          Register Delivery Partner
        </Button>
      </Card>
      <Divider sx={{ my: 2 }} />

      <Card sx={{ p: 2 }}>
        <Typography fontWeight="bold" gutterBottom component="div">
          Orders Awaiting Rider Assignment
        </Typography>
        <div className="responsive_table">
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell align="center">Order</TableCell>
                  <TableCell align="center">Customer</TableCell>
                  <TableCell align="center">Delivery Address</TableCell>
                  <TableCell align="center">Status</TableCell>
                  <TableCell align="center">Assigned Rider</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {orders.map((order) => (
                  <TableRow key={order.id}>
                    <TableCell align="center">{order.uuid}</TableCell>
                    <TableCell align="center">{order.name}</TableCell>
                    <TableCell align="center">{order.deliveryAddress || '-'}</TableCell>
                    <TableCell align="center">{order.orderStatus?.replace(/_/g, ' ')}</TableCell>
                    <TableCell align="center">
                      <TextField
                        select
                        variant="standard"
                        size="small"
                        value={order.assignedDeliveryPartner || ''}
                        onChange={(e) => assign(order.id, e.target.value)}
                        sx={{ minWidth: 180 }}
                      >
                        <MenuItem value="" disabled>
                          Select rider
                        </MenuItem>
                        {partners
                          .filter(
                            (p) =>
                              p.availability === 'AVAILABLE' || p.email === order.assignedDeliveryPartner
                          )
                          .map((p) => (
                            <MenuItem key={p.email} value={p.email}>
                              {p.name} ({p.vehicleNumber})
                            </MenuItem>
                          ))}
                      </TextField>
                    </TableCell>
                  </TableRow>
                ))}
                {orders.length === 0 && (
                  <TableRow>
                    <TableCell align="center" colSpan={5}>
                      No orders currently need rider assignment.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      </Card>
      <Divider sx={{ my: 2 }} />

      <Card sx={{ p: 2 }}>
        <Typography fontWeight="bold" gutterBottom component="div">
          Delivery Partners
        </Typography>
        <div className="responsive_table">
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell align="center">Name</TableCell>
                  <TableCell align="center">Email</TableCell>
                  <TableCell align="center">Contact Number</TableCell>
                  <TableCell align="center">Vehicle Number</TableCell>
                  <TableCell align="center">Availability</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {partners.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell align="center">{p.name}</TableCell>
                    <TableCell align="center">{p.email}</TableCell>
                    <TableCell align="center">{p.contactNumber}</TableCell>
                    <TableCell align="center">{p.vehicleNumber}</TableCell>
                    <TableCell align="center">
                      <Chip
                        size="small"
                        label={p.availability}
                        color={AVAILABILITY_COLOR[p.availability] || 'default'}
                      />
                    </TableCell>
                  </TableRow>
                ))}
                {partners.length === 0 && (
                  <TableRow>
                    <TableCell align="center" colSpan={5}>
                      No delivery partners registered yet.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      </Card>

      <Dialog open={registerOpen} onClose={() => setRegisterOpen(false)} fullWidth maxWidth="sm">
        {registerOpen && (
          <RegisterDeliveryPartnerDialog
            onClose={() => setRegisterOpen(false)}
            onSuccess={loadPartners}
          />
        )}
      </Dialog>
    </>
  );
}
