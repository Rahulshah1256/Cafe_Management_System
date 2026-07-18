import { useEffect, useState } from 'react';
import {
  Card,
  TextField,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Tooltip,
  Dialog,
  Paper,
  Divider,
  Chip,
} from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import SummarizeIcon from '@mui/icons-material/Summarize';
import DeleteIcon from '@mui/icons-material/Delete';
import ReplayIcon from '@mui/icons-material/Replay';
import CurrencyExchangeIcon from '@mui/icons-material/CurrencyExchange';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import { saveAs } from 'file-saver';
import { BillService } from '../services/bill.service';
import { PaymentService } from '../services/payment.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import { getUserRole, getTokenPayload } from '../auth/auth';
import { openRazorpayCheckout } from '../shared/razorpay';
import ViewBillProductsDialog from '../components/dialogs/ViewBillProductsDialog';
import ConfirmationDialog from '../components/dialogs/ConfirmationDialog';
import OrderTrackingDialog from '../components/dialogs/OrderTrackingDialog';

const ORDER_STATUSES = ['PLACED', 'ACCEPTED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];

const paymentStatusColor = (status: string): 'success' | 'error' | 'warning' | 'default' => {
  switch (status) {
    case 'SUCCESS':
      return 'success';
    case 'FAILED':
      return 'error';
    case 'PENDING':
      return 'warning';
    case 'REFUNDED':
      return 'default';
    default:
      return 'default';
  }
};

export default function ViewBill() {
  const { openSnackBar } = useSnackbarService();
  const isAdmin = getUserRole() === 'admin';
  const [rows, setRows] = useState<any[]>([]);
  const [filter, setFilter] = useState('');
  const [viewTarget, setViewTarget] = useState<any | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<any | null>(null);
  const [trackTarget, setTrackTarget] = useState<any | null>(null);

  const tableData = () => {
    BillService.getBills()
      .then((response: any) => setRows(response.data || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  useEffect(() => {
    tableData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const deleteBill = (id: any) => {
    BillService.delete(id)
      .then((response: any) => {
        tableData();
        openSnackBar(response?.data?.message, 'success');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const downloadReport = (values: any) => {
    const data = {
      name: values.name,
      email: values.email,
      uuid: values.uuid,
      contactNumber: values.contactNumber,
      paymentMethod: values.paymentMethod,
      totalAmount: values.total.toString(),
      productDetails: values.productDetails,
    };
    BillService.getPdf(data).then((response: any) => {
      saveAs(response.data, values.uuid + '.pdf');
    });
  };

  const retryPayment = (row: any) => {
    PaymentService.retryPayment(row.uuid)
      .then((response: any) => {
        const wrapper = response?.data || {};
        if (wrapper.razorpayOrderId) {
          // Reopen the real Razorpay Checkout widget against the freshly created order.
          const tokenPayload = getTokenPayload();
          openRazorpayCheckout({
            keyId: wrapper.razorpayKeyId,
            orderId: wrapper.razorpayOrderId,
            amountInPaise: wrapper.razorpayAmount,
            currency: wrapper.razorpayCurrency || 'INR',
            description: 'Order ' + row.uuid,
            prefillName: row.name,
            prefillEmail: tokenPayload?.sub || row.email || '',
            prefillContact: row.contactNumber || '',
          })
            .then((paymentResponse) =>
              PaymentService.verifyPayment({
                billUuid: row.uuid,
                razorpayOrderId: paymentResponse.razorpay_order_id,
                razorpayPaymentId: paymentResponse.razorpay_payment_id,
                razorpaySignature: paymentResponse.razorpay_signature,
              })
            )
            .then(() => {
              openSnackBar('Payment successful!', 'success');
              tableData();
            })
            .catch((error: any) => {
              const message = error?.response?.data?.message || error?.message || 'Payment could not be completed.';
              openSnackBar(message, GlobalConstants.error);
              tableData();
            });
        } else {
          openSnackBar(wrapper.message || 'Payment retried', 'success');
          tableData();
        }
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const refundPayment = (row: any) => {
    PaymentService.refund(row.uuid)
      .then((response: any) => {
        openSnackBar(response?.data?.message || 'Refund issued', 'success');
        tableData();
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const updateOrderStatus = (row: any, status: string) => {
    BillService.updateOrderStatus(row.id, status)
      .then(() => {
        openSnackBar('Order status updated', 'success');
        tableData();
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const cancelOrder = (row: any) => {
    BillService.cancelOrder(row.id)
      .then(() => {
        openSnackBar('Order cancelled', 'success');
        setTrackTarget(null);
        tableData();
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const filtered = rows.filter((row) =>
    JSON.stringify(row).toLowerCase().includes(filter.trim().toLowerCase())
  );

  const columns = [
    'Name',
    'Email',
    'Contact Number',
    'Payment Method',
    'Payment Status',
    'Order Status',
    'Total',
    'Action',
  ];

  return (
    <>
      <Card sx={{ p: 2 }}>
        <b>View Bill</b>
      </Card>
      <Divider sx={{ my: 2 }} />
      <Card sx={{ p: 2 }}>
        <TextField
          label="Filter"
          variant="standard"
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        />
      </Card>
      <Divider sx={{ my: 2 }} />

      <div className="responsive_table">
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                {columns.map((col) => (
                  <TableCell key={col} align="center">
                    {col}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.map((row) => (
                <TableRow key={row.id}>
                  <TableCell align="center">{row.name}</TableCell>
                  <TableCell align="center">{row.email}</TableCell>
                  <TableCell align="center">{row.contactNumber}</TableCell>
                  <TableCell align="center">{row.paymentMethod}</TableCell>
                  <TableCell align="center">
                    {row.paymentStatus ? (
                      <Chip size="small" label={row.paymentStatus} color={paymentStatusColor(row.paymentStatus)} />
                    ) : (
                      '-'
                    )}
                  </TableCell>
                  <TableCell align="center">
                    {isAdmin && row.orderStatus ? (
                      <TextField
                        select
                        variant="standard"
                        size="small"
                        value={row.orderStatus}
                        onChange={(e) => updateOrderStatus(row, e.target.value)}
                      >
                        {ORDER_STATUSES.map((s) => (
                          <MenuItem key={s} value={s}>
                            {s.replace(/_/g, ' ')}
                          </MenuItem>
                        ))}
                      </TextField>
                    ) : (
                      row.orderStatus?.replace(/_/g, ' ') || '-'
                    )}
                  </TableCell>
                  <TableCell align="center">{row.total}</TableCell>
                  <TableCell align="center">
                    <Tooltip title="View">
                      <IconButton color="primary" onClick={() => setViewTarget(row)}>
                        <VisibilityIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Track Order">
                      <IconButton color="primary" onClick={() => setTrackTarget(row)}>
                        <LocalShippingIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Download Bill">
                      <IconButton color="primary" onClick={() => downloadReport(row)}>
                        <SummarizeIcon />
                      </IconButton>
                    </Tooltip>
                    {row.paymentStatus === 'FAILED' && (
                      <Tooltip title="Retry Payment">
                        <IconButton color="warning" onClick={() => retryPayment(row)}>
                          <ReplayIcon />
                        </IconButton>
                      </Tooltip>
                    )}
                    {isAdmin && row.paymentStatus === 'SUCCESS' && (
                      <Tooltip title="Issue Refund">
                        <IconButton color="secondary" onClick={() => refundPayment(row)}>
                          <CurrencyExchangeIcon />
                        </IconButton>
                      </Tooltip>
                    )}
                    <Tooltip title="Delete">
                      <IconButton color="primary" onClick={() => setDeleteTarget(row)}>
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </div>

      <Dialog open={viewTarget !== null} onClose={() => setViewTarget(null)} fullWidth maxWidth="lg">
        {viewTarget && (
          <ViewBillProductsDialog data={viewTarget} onClose={() => setViewTarget(null)} />
        )}
      </Dialog>

      <Dialog open={trackTarget !== null} onClose={() => setTrackTarget(null)} fullWidth maxWidth="sm">
        {trackTarget && (
          <OrderTrackingDialog
            data={trackTarget}
            onClose={() => setTrackTarget(null)}
            onCancelOrder={cancelOrder}
          />
        )}
      </Dialog>

      <Dialog open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} fullWidth maxWidth="xs">
        {deleteTarget && (
          <ConfirmationDialog
            message={`delete ${deleteTarget.name} bill`}
            onConfirm={() => {
              deleteBill(deleteTarget.id);
              setDeleteTarget(null);
            }}
            onClose={() => setDeleteTarget(null)}
          />
        )}
      </Dialog>
    </>
  );
}

