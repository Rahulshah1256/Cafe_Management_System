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
} from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import EditIcon from '@mui/icons-material/Edit';
import { CategoryService } from '../services/category.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import CategoryDialog from '../components/dialogs/CategoryDialog';

export default function ManageCategory() {
  const { openSnackBar } = useSnackbarService();
  const [rows, setRows] = useState<any[]>([]);
  const [filter, setFilter] = useState('');
  const [dialog, setDialog] = useState<{ action: 'Add' | 'Edit'; data?: any } | null>(null);

  const tableData = () => {
    CategoryService.getCategorys()
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
    (row.name || '').toLowerCase().includes(filter.trim().toLowerCase())
  );

  return (
    <>
      <Card sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
        <b>Manage Category</b>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddCircleIcon />}
          sx={{ ml: 'auto' }}
          onClick={() => setDialog({ action: 'Add' })}
        >
          Add Category
        </Button>
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
                <TableCell align="center">Name</TableCell>
                <TableCell align="center">Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.map((row) => (
                <TableRow key={row.id}>
                  <TableCell align="center">{row.name}</TableCell>
                  <TableCell align="center">
                    <Tooltip title="Edit">
                      <IconButton
                        color="primary"
                        onClick={() => setDialog({ action: 'Edit', data: row })}
                      >
                        <EditIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </div>

      <Dialog open={dialog !== null} onClose={() => setDialog(null)} fullWidth maxWidth="md">
        {dialog && (
          <CategoryDialog
            action={dialog.action}
            data={dialog.data}
            onClose={() => setDialog(null)}
            onSuccess={tableData}
          />
        )}
      </Dialog>
    </>
  );
}
