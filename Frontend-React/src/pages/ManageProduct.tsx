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
  Switch,
  Chip,
  Box,
} from '@mui/material';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import StarIcon from '@mui/icons-material/Star';
import FiberNewIcon from '@mui/icons-material/FiberNew';
import { ProductService } from '../services/product.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import { getFoodImage, DEFAULT_FOOD_IMAGE } from '../shared/foodImage';
import ProductDialog from '../components/dialogs/ProductDialog';
import ConfirmationDialog from '../components/dialogs/ConfirmationDialog';

export default function ManageProduct() {
  const { openSnackBar } = useSnackbarService();
  const [rows, setRows] = useState<any[]>([]);
  const [filter, setFilter] = useState('');
  const [dialog, setDialog] = useState<{ action: 'Add' | 'Edit'; data?: any } | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<any | null>(null);

  const tableData = () => {
    ProductService.getProducts()
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

  const deleteProduct = (id: any) => {
    ProductService.delete(id)
      .then((response: any) => {
        tableData();
        openSnackBar(response?.data?.message, 'success');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const onToggleStatus = (checked: boolean, id: any) => {
    ProductService.updateStatus({ status: checked.toString(), id })
      .then((response: any) => {
        openSnackBar(response?.data?.message, 'success');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const filtered = rows.filter((row) =>
    JSON.stringify(row).toLowerCase().includes(filter.trim().toLowerCase())
  );

  const columns = ['Image', 'Name', 'Category', 'Description', 'Price', 'Veg/Non-Veg', 'Rating', 'Tags', 'Action'];

  return (
    <>
      <Card sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
        <b>Manage Product</b>
        <Button
          variant="contained"
          color="primary"
          startIcon={<AddCircleIcon />}
          sx={{ ml: 'auto' }}
          onClick={() => setDialog({ action: 'Add' })}
        >
          Add Product
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
                  <TableCell align="center">
                    <Box
                      component="img"
                      src={getFoodImage(row)}
                      alt={row.name}
                      loading="lazy"
                      onError={(e: any) => {
                        e.target.src = DEFAULT_FOOD_IMAGE;
                      }}
                      sx={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 2 }}
                    />
                  </TableCell>
                  <TableCell align="center">{row.name}</TableCell>
                  <TableCell align="center">{row.categoryName}</TableCell>
                  <TableCell align="center">{row.description}</TableCell>
                  <TableCell align="center">{row.price}</TableCell>
                  <TableCell align="center">
                    <Chip
                      size="small"
                      label={row.isVeg === false ? 'Non-Veg' : 'Veg'}
                      sx={{
                        backgroundColor: row.isVeg === false ? '#c0392b' : '#27ae60',
                        color: '#fff',
                      }}
                    />
                  </TableCell>
                  <TableCell align="center">
                    {row.rating ? (
                      <Box display="flex" alignItems="center" justifyContent="center" gap={0.5}>
                        <StarIcon fontSize="small" sx={{ color: '#f1c40f' }} />
                        {row.rating} ({row.ratingCount ?? 0})
                      </Box>
                    ) : (
                      '-'
                    )}
                  </TableCell>
                  <TableCell align="center">
                    <Box display="flex" gap={0.5} justifyContent="center" flexWrap="wrap">
                      {row.bestSeller && (
                        <Chip size="small" color="warning" label="Best Seller" />
                      )}
                      {row.newArrival && (
                        <Chip size="small" color="info" icon={<FiberNewIcon />} label="New" />
                      )}
                      {row.spicyLevel && row.spicyLevel !== 'NONE' && (
                        <Chip size="small" label={row.spicyLevel} />
                      )}
                    </Box>
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
                      <IconButton color="primary" onClick={() => setDeleteTarget(row)}>
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Activate or Deactivate Product">
                      <Switch
                        defaultChecked={row.status === 'true' || row.status === true}
                        onChange={(e) => onToggleStatus(e.target.checked, row.id)}
                      />
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
          <ProductDialog
            action={dialog.action}
            data={dialog.data}
            onClose={() => setDialog(null)}
            onSuccess={tableData}
          />
        )}
      </Dialog>

      <Dialog open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} fullWidth maxWidth="xs">
        {deleteTarget && (
          <ConfirmationDialog
            message={`delete ${deleteTarget.name} product `}
            onConfirm={() => {
              deleteProduct(deleteTarget.id);
              setDeleteTarget(null);
            }}
            onClose={() => setDeleteTarget(null)}
          />
        )}
      </Dialog>
    </>
  );
}
