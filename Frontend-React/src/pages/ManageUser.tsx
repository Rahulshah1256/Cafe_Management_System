import { useEffect, useState } from 'react';
import {
  Card,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Paper,
  Divider,
  Switch,
} from '@mui/material';
import { UserService } from '../services/user.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';

export default function ManageUser() {
  const { openSnackBar } = useSnackbarService();
  const [rows, setRows] = useState<any[]>([]);
  const [filter, setFilter] = useState('');

  const tableData = () => {
    UserService.getUsers()
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

  const onToggleStatus = (checked: boolean, id: any) => {
    UserService.update({ status: checked.toString(), id })
      .then((response: any) => openSnackBar(response?.data?.message, 'success'))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const filtered = rows.filter((row) =>
    JSON.stringify(row).toLowerCase().includes(filter.trim().toLowerCase())
  );

  const columns = ['Name', 'Email', 'Contact Number', 'Action'];

  return (
    <>
      <Card sx={{ p: 2 }}>
        <b>Manage Users</b>
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
                  <TableCell align="center">
                    <Tooltip title="Activate or Deactivate User">
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
    </>
  );
}
