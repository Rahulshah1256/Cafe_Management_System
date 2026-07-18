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
  IconButton,
  Tooltip,
  Paper,
  Divider,
  Box,
  FormControlLabel,
  Switch,
} from '@mui/material';
import PrintIcon from '@mui/icons-material/Print';
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn';
import DeleteIcon from '@mui/icons-material/Delete';
import { saveAs } from 'file-saver';
import { CategoryService } from '../services/category.service';
import { ProductService } from '../services/product.service';
import { BillService } from '../services/bill.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';

interface OrderItem {
  id: any;
  name: string;
  category: string;
  quantity: any;
  price: any;
  total: any;
}

export default function ManageOrder() {
  const { openSnackBar } = useSnackbarService();
  const [categorys, setCategorys] = useState<any[]>([]);
  const [products, setProducts] = useState<any[]>([]);
  const [dataSource, setDataSource] = useState<OrderItem[]>([]);
  const [totalAmount, setTotalAmount] = useState(0);
  const [vegOnly, setVegOnly] = useState(false);
  const [keyword, setKeyword] = useState('');

  const [form, setForm] = useState<any>({
    name: '',
    email: '',
    contactNumber: '',
    paymentMethod: '',
    categoryId: '',
    productId: '',
    price: '',
    quantity: '',
    total: 0,
  });

  const update = (patch: any) => setForm((prev: any) => ({ ...prev, ...patch }));

  useEffect(() => {
    getCategorys();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getCategorys = () => {
    CategoryService.getFilteredCategorys()
      .then((response: any) => setCategorys(response.data || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const fetchProducts = (categoryId: any, veg: boolean, search: string) => {
    if (!categoryId) {
      setProducts([]);
      return;
    }
    if (!veg && !search) {
      ProductService.getProductByCategory(categoryId)
        .then((response: any) => setProducts(response.data || []))
        .catch((error: any) => {
          const message = error?.response?.data?.message || GlobalConstants.genericError;
          openSnackBar(message, GlobalConstants.error);
        });
      return;
    }
    ProductService.search({
      categoryId,
      isVeg: veg ? true : undefined,
      keyword: search || undefined,
      status: 'true',
      size: 100,
    })
      .then((response: any) => setProducts(response.data?.content || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const getProductsByCategory = (categoryId: any) => {
    update({ categoryId, productId: '', price: '', quantity: '', total: 0 });
    fetchProducts(categoryId, vegOnly, keyword);
  };

  const onVegOnlyChange = (checked: boolean) => {
    setVegOnly(checked);
    update({ productId: '', price: '', quantity: '', total: 0 });
    fetchProducts(form.categoryId, checked, keyword);
  };

  const onKeywordChange = (value: string) => {
    setKeyword(value);
    update({ productId: '', price: '', quantity: '', total: 0 });
    fetchProducts(form.categoryId, vegOnly, value);
  };

  const getProductDetails = (productId: any) => {
    ProductService.getById(productId)
      .then((response: any) => {
        const price = response.data.price;
        update({ productId, price, quantity: '1', total: price * 1 });
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const setQuantity = (value: string) => {
    const qty = Number(value);
    if (qty > 0) {
      update({ quantity: value, total: qty * Number(form.price) });
    } else if (value !== '') {
      update({ quantity: '1', total: 1 * Number(form.price) });
    } else {
      update({ quantity: value });
    }
  };

  const validateProductAdd = () => {
    return (
      form.price === '' ||
      form.price === null ||
      form.productId === '' ||
      Number(form.quantity) <= 0
    );
  };

  const validateSubmit = () => {
    return (
      totalAmount === 0 ||
      form.name === '' ||
      form.email === '' ||
      form.contactNumber === '' ||
      form.paymentMethod === ''
    );
  };

  const add = () => {
    const product = products.find((p) => p.id === form.productId);
    const category = categorys.find((c) => c.id === form.categoryId);
    if (!product) {
      return;
    }
    const exists = dataSource.find((e) => e.id === product.id);
    if (exists === undefined) {
      setTotalAmount((prev) => prev + Number(form.total));
      setDataSource((prev) => [
        ...prev,
        {
          id: product.id,
          name: product.name,
          category: category?.name,
          quantity: form.quantity,
          price: form.price,
          total: form.total,
        },
      ]);
      openSnackBar(GlobalConstants.productAdded, 'Success');
    } else {
      openSnackBar(GlobalConstants.productExistError, GlobalConstants.error);
    }
  };

  const handleDelete = (index: number, element: OrderItem) => {
    setTotalAmount((prev) => prev - Number(element.total));
    setDataSource((prev) => prev.filter((_, i) => i !== index));
  };

  const submitAction = () => {
    const data = {
      name: form.name,
      email: form.email,
      contactNumber: form.contactNumber,
      paymentMethod: form.paymentMethod,
      totalAmount: totalAmount.toString(),
      productDetails: JSON.stringify(dataSource),
    };
    BillService.generateReport(data)
      .then((response: any) => {
        downloadFile(response?.data?.uuid);
        setForm({
          name: '',
          email: '',
          contactNumber: '',
          paymentMethod: '',
          categoryId: '',
          productId: '',
          price: '',
          quantity: '',
          total: 0,
        });
        setDataSource([]);
        setTotalAmount(0);
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const downloadFile = (fileName: string) => {
    BillService.getPdf({ uuid: fileName }).then((response: any) => {
      saveAs(response.data, fileName + '.pdf');
    });
  };

  const columns = ['Name', 'Category', 'Price', 'Quantity', 'Total', 'Delete'];

  return (
    <>
      <Card sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
        <b>Manage Order</b>
        <Button
          variant="contained"
          color="primary"
          startIcon={<PrintIcon />}
          sx={{ ml: 'auto' }}
          disabled={validateSubmit()}
          onClick={submitAction}
        >
          Submit &amp; Bill
        </Button>
      </Card>
      <Divider sx={{ my: 2 }} />

      <Card sx={{ p: 2 }}>
        <b>Customer Details</b>
        <Box display="flex" flexWrap="wrap" gap={2} sx={{ mt: 1 }}>
          <TextField
            label="Name"
            variant="standard"
            required
            sx={{ flex: 1, minWidth: 200 }}
            value={form.name}
            onChange={(e) => update({ name: e.target.value })}
          />
          <TextField
            label="Email"
            variant="standard"
            required
            sx={{ flex: 1, minWidth: 200 }}
            value={form.email}
            onChange={(e) => update({ email: e.target.value })}
          />
          <TextField
            label="Contact Number"
            variant="standard"
            required
            sx={{ flex: 1, minWidth: 200 }}
            value={form.contactNumber}
            onChange={(e) => update({ contactNumber: e.target.value })}
          />
          <TextField
            label="Payment Method"
            variant="standard"
            select
            required
            sx={{ flex: 1, minWidth: 200 }}
            value={form.paymentMethod}
            onChange={(e) => update({ paymentMethod: e.target.value })}
          >
            <MenuItem value="Cash">Cash</MenuItem>
            <MenuItem value="Credit Card">Credit Card</MenuItem>
            <MenuItem value="Debit Card">Debit Card</MenuItem>
          </TextField>
        </Box>
      </Card>
      <Divider sx={{ my: 2 }} />

      <Card sx={{ p: 2 }}>
        <b>Select Product</b>
        <Box display="flex" flexWrap="wrap" gap={2} sx={{ mt: 1 }}>
          <TextField
            label="Category"
            variant="standard"
            select
            sx={{ flex: 1, minWidth: 180 }}
            value={form.categoryId}
            onChange={(e) => getProductsByCategory(e.target.value)}
          >
            {categorys.map((category) => (
              <MenuItem key={category.id} value={category.id}>
                {category.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Search menu"
            variant="standard"
            sx={{ flex: 1, minWidth: 180 }}
            value={keyword}
            onChange={(e) => onKeywordChange(e.target.value)}
          />
          <FormControlLabel
            control={
              <Switch checked={vegOnly} onChange={(e) => onVegOnlyChange(e.target.checked)} />
            }
            label="Veg Only"
          />
          <TextField
            label="Product"
            variant="standard"
            select
            sx={{ flex: 1, minWidth: 180 }}
            value={form.productId}
            onChange={(e) => getProductDetails(e.target.value)}
          >
            {products.map((product) => (
              <MenuItem key={product.id} value={product.id}>
                {product.isVeg === false ? '🔴' : '🟢'} {product.name}
                {product.bestSeller ? ' ⭐' : ''}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Price"
            variant="standard"
            required
            sx={{ flex: 1, minWidth: 120 }}
            value={form.price}
            InputProps={{ readOnly: true }}
          />
          <TextField
            label="Quantity"
            variant="standard"
            required
            sx={{ flex: 1, minWidth: 120 }}
            value={form.quantity}
            onChange={(e) => setQuantity(e.target.value)}
          />
          <TextField
            label="Total"
            variant="standard"
            required
            sx={{ flex: 1, minWidth: 120 }}
            value={form.total}
            InputProps={{ readOnly: true }}
          />
        </Box>
        <Box sx={{ mt: 2, display: 'flex', alignItems: 'center' }}>
          <Button
            variant="contained"
            color="primary"
            disabled={validateProductAdd()}
            onClick={add}
          >
            Add
          </Button>
          <Button
            variant="contained"
            color="primary"
            startIcon={<MonetizationOnIcon />}
            sx={{ ml: 'auto' }}
          >
            Total Amount: {totalAmount}
          </Button>
        </Box>
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
              {dataSource.map((row, index) => (
                <TableRow key={index}>
                  <TableCell align="center">{row.name}</TableCell>
                  <TableCell align="center">{row.category}</TableCell>
                  <TableCell align="center">{row.price}</TableCell>
                  <TableCell align="center">{row.quantity}</TableCell>
                  <TableCell align="center">{row.total}</TableCell>
                  <TableCell align="center">
                    <Tooltip title="Delete">
                      <IconButton color="primary" onClick={() => handleDelete(index, row)}>
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
    </>
  );
}
