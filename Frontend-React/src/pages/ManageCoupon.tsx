import { useEffect, useState } from 'react';
import {
  Card,
  Button,
  TextField,
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
import AddCircleIcon from '@mui/icons-material/AddCircle';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { CouponService } from '../services/coupon.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import CouponDialog from '../components/dialogs/CouponDialog';
import ConfirmationDialog from '../components/dialogs/ConfirmationDialog';

export default function ManageCoupon() {
  const { openSnackBar } = useSnackbarService();
  const [rows, setRows] = useState<any[]>([]);
  const [filter, setFilter] = useState('');
  const [dialog, setDialog] = useState<{ action: 'Add' | 'Edit'; data?: any } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<any>(null);

  const tableData = () => {
    CouponService.getAllCoupons()
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

  const filtered = rows.filter((row) =>
    (row.code || '').toLowerCase().includes(filter.trim().toLowerCase())
  );

  const handleDelete = () => {
    CouponService.deleteCoupon(deleteTarget.id)
      .then(() => {
        setDeleteTarget(null);
        tableData();
        openSnackBar('Coupon deleted', 'Success');
      })
      .catch((error: any) => {
        setDeleteTarget(null);
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  return (
    <>
      <Card sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
        <b>Manage Coupons</b>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddCircleIcon />}
          sx={{ ml: 'auto' }}
          onClick={() => setDialog({ action: 'Add' })}
        >
          Add Coupon
        </Button>
      </Card>
      <Divider sx={{ my: 2 }} />
      <Card sx={{ p: 2 }}>
        <TextField
          label="Filter by code"
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
                <TableCell align="center">Code</TableCell>
                <TableCell align="center">Description</TableCell>
                <TableCell align="center">Type</TableCell>
                <TableCell align="center">Value</TableCell>
                <TableCell align="center">Min Order</TableCell>
                <TableCell align="center">Expiry</TableCell>
                <TableCell align="center">Usage</TableCell>
                <TableCell align="center">Status</TableCell>
                <TableCell align="center">Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.map((row) => (
                <TableRow key={row.id}>
                  <TableCell align="center">{row.code}</TableCell>
                  <TableCell align="center">{row.description}</TableCell>
                  <TableCell align="center">{row.discountType}</TableCell>
                  <TableCell align="center">
                    {row.discountType === 'PERCENTAGE' ? `${row.discountValue}%` : `₹${row.discountValue}`}
                  </TableCell>
                  <TableCell align="center">₹{row.minOrderAmount}</TableCell>
                  <TableCell align="center">{row.expiryDate || '-'}</TableCell>
                  <TableCell align="center">
                    {row.usedCount ?? 0}
                    {row.usageLimit ? ` / ${row.usageLimit}` : ''}
                  </TableCell>
                  <TableCell align="center">
                    <Chip
                      size="small"
                      label={row.active ? 'Active' : 'Inactive'}
                      color={row.active ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="center">
                    <Tooltip title="Edit">
                      <IconButton
                        color="primary"
                        onClick={() => setDialog({ action: 'Edit', data: row })}
                      >
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton color="error" onClick={() => setDeleteTarget(row)}>
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

      <Dialog open={dialog !== null} onClose={() => setDialog(null)} fullWidth maxWidth="sm">
        {dialog && (
          <CouponDialog
            action={dialog.action}
            data={dialog.data}
            onClose={() => setDialog(null)}
            onSuccess={tableData}
          />
        )}
      </Dialog>

      <Dialog open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} fullWidth maxWidth="xs">
        <ConfirmationDialog
          message={`delete coupon ${deleteTarget?.code}`}
          onConfirm={handleDelete}
          onClose={() => setDeleteTarget(null)}
        />
      </Dialog>
    </>
  );
}
