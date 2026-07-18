import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  DialogContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

interface Props {
  data: any;
  onClose: () => void;
}

export default function ViewBillProductsDialog({ data, onClose }: Props) {
  let products: any[] = [];
  try {
    products = JSON.parse(data.productDetails);
  } catch {
    products = [];
  }

  const columns = ['Name', 'Category', 'Price', 'Quantity', 'Total'];

  return (
    <>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Typography sx={{ flex: 1 }}>View Bill</Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent>
        <table id="customers">
          <tbody>
            <tr>
              <td>
                <b>Name: </b>
                {data?.name}
              </td>
              <td>
                <b>Email: </b>
                {data?.email}
              </td>
            </tr>
            <tr>
              <td>
                <b>Contact Number: </b>
                {data?.contactNumber}
              </td>
              <td>
                <b>Payment Method: </b>
                {data?.paymentMethod}
              </td>
            </tr>
          </tbody>
        </table>

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
                {products.map((row, index) => (
                  <TableRow key={index}>
                    <TableCell align="center">{row.name}</TableCell>
                    <TableCell align="center">{row.category}</TableCell>
                    <TableCell align="center">{row.price}</TableCell>
                    <TableCell align="center">{row.quantity}</TableCell>
                    <TableCell align="center">{row.total}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      </DialogContent>
    </>
  );
}
