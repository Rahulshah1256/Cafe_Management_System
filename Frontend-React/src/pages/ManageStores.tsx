import { useEffect, useState } from 'react';
import {
  Card,
  Button,
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
import { StoreService } from '../services/store.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import StoreDialog from '../components/dialogs/StoreDialog';
import ConfirmationDialog from '../components/dialogs/ConfirmationDialog';

export default function ManageStores() {
  const { openSnackBar } = useSnackbarService();
  const [rows, setRows] = useState<any[]>([]);
  const [dialog, setDialog] = useState<{ action: 'Add' | 'Edit'; data?: any } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<any>(null);

  const tableData = () => {
    StoreService.getAllStores()
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

  const handleDelete = () => {
    StoreService.deleteStore(deleteTarget.id)
      .then(() => {
        setDeleteTarget(null);
        tableData();
        openSnackBar('Store deleted', 'Success');
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
        <b>Manage Stores</b>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddCircleIcon />}
          sx={{ ml: 'auto' }}
          onClick={() => setDialog({ action: 'Add' })}
        >
          Add Store
        </Button>
      </Card>
      <Divider sx={{ my: 2 }} />

      <div className="responsive_table">
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell align="center">Name</TableCell>
                <TableCell align="center">Address</TableCell>
                <TableCell align="center">Phone</TableCell>
                <TableCell align="center">Hours</TableCell>
                <TableCell align="center">Status</TableCell>
                <TableCell align="center">Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell align="center">{row.name}</TableCell>
                  <TableCell align="center">
                    {[row.addressLine1, row.addressLine2, row.city, row.state, row.pincode]
                      .filter(Boolean)
                      .join(', ')}
                  </TableCell>
                  <TableCell align="center">{row.phone || '-'}</TableCell>
                  <TableCell align="center">{row.openingHours || '-'}</TableCell>
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
              {rows.length === 0 && (
                <TableRow>
                  <TableCell align="center" colSpan={6}>
                    No stores added yet.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </div>

      <Dialog open={dialog !== null} onClose={() => setDialog(null)} fullWidth maxWidth="sm">
        {dialog && (
          <StoreDialog
            action={dialog.action}
            data={dialog.data}
            onClose={() => setDialog(null)}
            onSuccess={tableData}
          />
        )}
      </Dialog>

      <Dialog open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} fullWidth maxWidth="xs">
        <ConfirmationDialog
          message={`delete store ${deleteTarget?.name}`}
          onConfirm={handleDelete}
          onClose={() => setDeleteTarget(null)}
        />
      </Dialog>
    </>
  );
}
