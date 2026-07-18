import { useEffect, useState } from 'react';
import {
  Card,
  Box,
  TextField,
  IconButton,
  Tooltip,
  Divider,
  Typography,
  Button,
  Grid,
  Chip,
  Avatar,
} from '@mui/material';
import RemoveIcon from '@mui/icons-material/Remove';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { CartService } from '../services/cart.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import { getFoodImage, DEFAULT_FOOD_IMAGE } from '../shared/foodImage';

export default function CartPage() {
  const navigate = useNavigate();
  const { openSnackBar } = useSnackbarService();
  const [cart, setCart] = useState<any>(null);
  const [couponCode, setCouponCode] = useState('');

  useEffect(() => {
    loadCart();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadCart = () => {
    CartService.getCart()
      .then((response: any) => setCart(response.data))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const updateQuantity = (item: any, delta: number) => {
    const next = item.quantity + delta;
    if (next <= 0) {
      CartService.removeItem(item.id).then(loadCart);
      return;
    }
    CartService.updateItem(item.id, { quantity: next }).then(loadCart);
  };

  const removeItem = (item: any) => {
    CartService.removeItem(item.id)
      .then(() => {
        openSnackBar('Item removed', 'Success');
        loadCart();
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const applyCoupon = () => {
    if (!couponCode.trim()) {
      return;
    }
    CartService.applyCoupon(couponCode.trim())
      .then((response: any) => {
        setCart(response.data);
        openSnackBar('Coupon applied', 'Success');
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const removeCoupon = () => {
    CartService.removeCoupon()
      .then((response: any) => {
        setCart(response.data);
        setCouponCode('');
      })
      .catch(() => undefined);
  };

  const items = cart?.items || [];

  return (
    <Box sx={{ pb: 4 }}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          mb: 3,
          flexWrap: 'wrap',
        }}
      >
        <Avatar sx={{ bgcolor: 'primary.main', width: 44, height: 44 }}>
          <ShoppingCartIcon />
        </Avatar>
        <Box sx={{ flex: 1 }}>
          <Typography variant="h5" fontWeight={800}>
            My Cart
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {items.length} item{items.length !== 1 ? 's' : ''} in your cart
          </Typography>
        </Box>
        <Button
          variant="outlined"
          sx={{ borderRadius: 999 }}
          onClick={() => navigate('/cafe/menu')}
        >
          Continue Shopping
        </Button>
      </Box>

      {items.length === 0 ? (
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
        >
          <Card
            sx={{
              p: 6,
              textAlign: 'center',
              borderRadius: 5,
              background: 'linear-gradient(180deg, rgba(229,32,42,0.04), transparent)',
            }}
          >
            <motion.div
              animate={{ y: [0, -10, 0] }}
              transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
            >
              <ShoppingCartIcon sx={{ fontSize: 88, color: 'primary.light', mb: 1 }} />
            </motion.div>
            <Typography variant="h6" fontWeight={700} sx={{ mt: 1 }}>
              Your cart feels lonely!
            </Typography>
            <Typography color="text.secondary" sx={{ mb: 3 }}>
              Add some delicious food to make it happy.
            </Typography>
            <Button
              variant="contained"
              size="large"
              sx={{ borderRadius: 999, px: 4 }}
              onClick={() => navigate('/cafe/menu')}
            >
              Browse Menu
            </Button>
          </Card>
        </motion.div>
      ) : (
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <Box display="flex" flexDirection="column" gap={2}>
              <AnimatePresence>
                {items.map((item: any, i: number) => (
                  <motion.div
                    key={item.id}
                    layout
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 30, height: 0 }}
                    transition={{ duration: 0.3, delay: Math.min(i * 0.05, 0.3) }}
                  >
                    <Card
                      sx={{
                        p: 2,
                        borderRadius: 4,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 2,
                        flexWrap: 'wrap',
                        transition: 'box-shadow .2s ease',
                        '&:hover': { boxShadow: '0 10px 24px rgba(0,0,0,0.08)' },
                      }}
                    >
                      <Box
                        component="img"
                        src={getFoodImage(item)}
                        alt={item.productName}
                        loading="lazy"
                        onError={(e: any) => {
                          e.target.src = DEFAULT_FOOD_IMAGE;
                        }}
                        sx={{
                          width: 64,
                          height: 64,
                          objectFit: 'cover',
                          borderRadius: 3,
                          flexShrink: 0,
                        }}
                      />

                      <Box sx={{ flex: 1, minWidth: 140 }}>
                        <Typography fontWeight={700}>{item.productName}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          ₹{item.unitPrice} each
                        </Typography>
                        {item.available === false && (
                          <Chip
                            size="small"
                            color="error"
                            label="Currently unavailable"
                            sx={{ mt: 0.5 }}
                          />
                        )}
                      </Box>

                      <Box
                        display="flex"
                        alignItems="center"
                        gap={0.5}
                        sx={{
                          bgcolor: 'action.hover',
                          borderRadius: 999,
                          px: 0.5,
                          py: 0.5,
                        }}
                      >
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => updateQuantity(item, -1)}
                          component={motion.button}
                          whileTap={{ scale: 0.85 }}
                        >
                          <RemoveIcon fontSize="small" />
                        </IconButton>
                        <Typography sx={{ minWidth: 22, textAlign: 'center' }} fontWeight={700}>
                          {item.quantity}
                        </Typography>
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => updateQuantity(item, 1)}
                          component={motion.button}
                          whileTap={{ scale: 0.85 }}
                        >
                          <AddIcon fontSize="small" />
                        </IconButton>
                      </Box>

                      <Typography fontWeight={800} sx={{ minWidth: 70, textAlign: 'right' }}>
                        ₹{item.subtotal}
                      </Typography>

                      <Tooltip title="Remove">
                        <IconButton color="error" onClick={() => removeItem(item)}>
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip>
                    </Card>
                  </motion.div>
                ))}
              </AnimatePresence>
            </Box>
          </Grid>

          <Grid item xs={12} md={4}>
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.1 }}
            >
              <Card sx={{ p: 3, borderRadius: 4, position: 'sticky', top: 88 }}>
                <Box display="flex" alignItems="center" gap={1} mb={2}>
                  <LocalOfferIcon color="secondary" fontSize="small" />
                  <Typography variant="subtitle1" fontWeight={700}>
                    Apply Coupon
                  </Typography>
                </Box>

                {cart?.appliedCouponCode ? (
                  <Box
                    display="flex"
                    alignItems="center"
                    gap={1}
                    sx={{
                      bgcolor: 'success.light',
                      color: 'success.dark',
                      borderRadius: 3,
                      px: 2,
                      py: 1,
                    }}
                  >
                    <Chip
                      label={cart.appliedCouponCode}
                      color="success"
                      size="small"
                      sx={{ fontWeight: 700 }}
                    />
                    <Typography variant="body2" sx={{ flex: 1 }}>
                      applied!
                    </Typography>
                    <Button size="small" color="error" onClick={removeCoupon}>
                      Remove
                    </Button>
                  </Box>
                ) : (
                  <Box display="flex" gap={1}>
                    <TextField
                      label="Coupon code"
                      size="small"
                      fullWidth
                      value={couponCode}
                      onChange={(e) => setCouponCode(e.target.value)}
                    />
                    <Button variant="contained" onClick={applyCoupon} sx={{ borderRadius: 999 }}>
                      Apply
                    </Button>
                  </Box>
                )}

                <Divider sx={{ my: 2.5 }} />

                <Box display="flex" alignItems="center" gap={1} mb={1.5}>
                  <ReceiptLongIcon color="primary" fontSize="small" />
                  <Typography variant="subtitle1" fontWeight={700}>
                    Price Summary
                  </Typography>
                </Box>

                <Box display="flex" flexDirection="column" gap={0.75}>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">
                      Subtotal
                    </Typography>
                    <Typography variant="body2">₹{cart?.subtotal}</Typography>
                  </Box>
                  {cart?.discount > 0 && (
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="body2" color="success.main">
                        Discount
                      </Typography>
                      <Typography variant="body2" color="success.main" fontWeight={700}>
                        -₹{cart.discount}
                      </Typography>
                    </Box>
                  )}
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">
                      <LocalShippingIcon
                        sx={{ fontSize: 14, verticalAlign: 'middle', mr: 0.5 }}
                      />
                      Delivery Charge
                    </Typography>
                    <Typography
                      variant="body2"
                      color={cart?.deliveryCharge === 0 ? 'success.main' : 'text.primary'}
                      fontWeight={cart?.deliveryCharge === 0 ? 700 : 400}
                    >
                      {cart?.deliveryCharge === 0 ? 'FREE' : `₹${cart?.deliveryCharge}`}
                    </Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">
                      Packing Charge
                    </Typography>
                    <Typography variant="body2">₹{cart?.packingCharge}</Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">
                      Platform Fee
                    </Typography>
                    <Typography variant="body2">₹{cart?.platformFee}</Typography>
                  </Box>
                  <Box display="flex" justifyContent="space-between">
                    <Typography variant="body2" color="text.secondary">
                      Tax
                    </Typography>
                    <Typography variant="body2">₹{cart?.tax}</Typography>
                  </Box>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6" fontWeight={800}>
                    Total
                  </Typography>
                  <Typography variant="h6" fontWeight={800} color="primary.main">
                    ₹{cart?.total}
                  </Typography>
                </Box>

                <Button
                  component={motion.button}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.97 }}
                  variant="contained"
                  fullWidth
                  size="large"
                  sx={{ borderRadius: 999, py: 1.3, fontWeight: 700 }}
                  onClick={() => navigate('/cafe/checkout')}
                >
                  Proceed to Checkout
                </Button>
              </Card>
            </motion.div>
          </Grid>
        </Grid>
      )}
    </Box>
  );
}
