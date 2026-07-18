import { useEffect, useState } from 'react';
import {
  Card,
  Box,
  TextField,
  MenuItem,
  Divider,
  Typography,
  Button,
  Grid,
  Radio,
  RadioGroup,
  FormControlLabel,
  FormControl,
  Checkbox,
  Stepper,
  Step,
  StepLabel,
  Chip,
  Avatar,
  Paper,
} from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PaymentIcon from '@mui/icons-material/Payment';
import FactCheckIcon from '@mui/icons-material/FactCheck';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import HomeIcon from '@mui/icons-material/Home';
import WorkIcon from '@mui/icons-material/Work';
import PlaceIcon from '@mui/icons-material/Place';
import LocalAtmIcon from '@mui/icons-material/LocalAtm';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import QrCodeIcon from '@mui/icons-material/QrCode';
import StarsIcon from '@mui/icons-material/Stars';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { CartService } from '../services/cart.service';
import { AddressService } from '../services/address.service';
import { LoyaltyService } from '../services/loyalty.service';
import { PaymentService } from '../services/payment.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import { openRazorpayCheckout } from '../shared/razorpay';
import { getTokenPayload } from '../auth/auth';

const steps = ['Address', 'Payment', 'Review'];

const paymentOptions = [
  { value: 'UPI', label: 'UPI', icon: <QrCodeIcon /> },
  { value: 'Credit Card', label: 'Credit Card', icon: <CreditCardIcon /> },
  { value: 'Debit Card', label: 'Debit Card', icon: <CreditCardIcon /> },
  { value: 'Cash On Delivery', label: 'Cash on Delivery', icon: <LocalAtmIcon /> },
];

export default function CheckoutPage() {
  const navigate = useNavigate();
  const { openSnackBar } = useSnackbarService();
  const [cart, setCart] = useState<any>(null);
  const [addresses, setAddresses] = useState<any[]>([]);
  const [addressId, setAddressId] = useState<any>('');
  const [paymentMethod, setPaymentMethod] = useState('Cash On Delivery');
  const [deliveryInstructions, setDeliveryInstructions] = useState('');
  const [showAddAddress, setShowAddAddress] = useState(false);
  const [activeStep, setActiveStep] = useState(0);
  const [orderSuccess, setOrderSuccess] = useState(false);
  const [newAddress, setNewAddress] = useState<any>({
    label: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    pincode: '',
    landmark: '',
    contactNumber: '',
    addressType: 'HOME',
    isDefault: false,
  });
  const [placing, setPlacing] = useState(false);
  const [loyaltyBalance, setLoyaltyBalance] = useState<any>(null);
  const [useLoyaltyPoints, setUseLoyaltyPoints] = useState(false);

  useEffect(() => {
    loadCart();
    loadAddresses();
    loadLoyaltyBalance();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadLoyaltyBalance = () => {
    LoyaltyService.getBalance()
      .then((response: any) => setLoyaltyBalance(response.data))
      .catch(() => undefined);
  };

  const loadCart = () => {
    CartService.getCart()
      .then((response: any) => setCart(response.data))
      .catch(() => undefined);
  };

  const loadAddresses = () => {
    AddressService.getMyAddresses()
      .then((response: any) => {
        const list = response.data || [];
        setAddresses(list);
        const defaultAddr = list.find((a: any) => a.isDefault) || list[0];
        if (defaultAddr) {
          setAddressId(defaultAddr.id);
        } else {
          setShowAddAddress(true);
        }
      })
      .catch(() => undefined);
  };

  const updateNewAddress = (patch: any) => setNewAddress((prev: any) => ({ ...prev, ...patch }));

  const saveAddress = () => {
    AddressService.addAddress(newAddress)
      .then((response: any) => {
        openSnackBar('Address added', 'Success');
        setShowAddAddress(false);
        setNewAddress({
          label: '',
          addressLine1: '',
          addressLine2: '',
          city: '',
          state: '',
          pincode: '',
          landmark: '',
          contactNumber: '',
          addressType: 'HOME',
          isDefault: false,
        });
        loadAddresses();
        setAddressId(response.data?.id);
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const validateAddress = () => {
    return (
      !newAddress.addressLine1 ||
      !newAddress.city ||
      !newAddress.state ||
      !newAddress.pincode ||
      !newAddress.contactNumber
    );
  };

  const placeOrder = () => {
    if (!addressId || !paymentMethod) {
      return;
    }
    setPlacing(true);
    CartService.checkout({ addressId, paymentMethod, deliveryInstructions, useLoyaltyPoints })
      .then((response: any) => {
        let result: any = {};
        try {
          result = typeof response.data === 'string' ? JSON.parse(response.data) : response.data;
        } catch {
          result = {};
        }
        if (result.paymentStatus === 'FAILED') {
          openSnackBar(result.message || 'Payment failed. You can retry from View Bill.', GlobalConstants.error);
          setPlacing(false);
        } else if (result.paymentStatus === 'PENDING' && result.razorpayOrderId) {
          // Online payment methods (UPI/Cards/Net Banking/Wallets) go through the real
          // Razorpay Checkout widget - the order is placed, but payment only counts as
          // done once the customer completes it here and we verify the signature.
          openRazorpayCheckoutWidget(result);
        } else {
          const pointsMsg =
            result.loyaltyPointsEarned > 0 ? ` You earned ${result.loyaltyPointsEarned} loyalty points.` : '';
          openSnackBar((result.message || 'Order placed successfully') + pointsMsg, 'Success');
          setOrderSuccess(true);
          setTimeout(() => navigate('/cafe/bill'), 1600);
        }
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
        setPlacing(false);
      });
  };

  const openRazorpayCheckoutWidget = (result: any) => {
    const tokenPayload = getTokenPayload();
    openRazorpayCheckout({
      keyId: result.razorpayKeyId,
      orderId: result.razorpayOrderId,
      amountInPaise: result.razorpayAmount,
      currency: result.razorpayCurrency || 'INR',
      description: 'Order ' + result.uuid,
      prefillEmail: tokenPayload?.sub || '',
      prefillContact: selectedAddress?.contactNumber || '',
    })
      .then((paymentResponse) =>
        PaymentService.verifyPayment({
          billUuid: result.uuid,
          razorpayOrderId: paymentResponse.razorpay_order_id,
          razorpayPaymentId: paymentResponse.razorpay_payment_id,
          razorpaySignature: paymentResponse.razorpay_signature,
        })
      )
      .then(() => {
        const pointsMsg =
          result.loyaltyPointsEarned > 0 ? ` You earned ${result.loyaltyPointsEarned} loyalty points.` : '';
        openSnackBar('Payment successful!' + pointsMsg, 'Success');
        setOrderSuccess(true);
        setTimeout(() => navigate('/cafe/bill'), 1600);
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || error?.message || 'Payment could not be completed.';
        openSnackBar(message + ' You can retry from View Bill.', GlobalConstants.error);
        setPlacing(false);
        navigate('/cafe/bill');
      });
  };

  const selectedAddress = addresses.find((a) => a.id === addressId);

  const addressIcon = (type: string) => {
    if (type === 'HOME') return <HomeIcon fontSize="small" />;
    if (type === 'WORK') return <WorkIcon fontSize="small" />;
    return <PlaceIcon fontSize="small" />;
  };

  if (cart && (!cart.items || cart.items.length === 0) && !orderSuccess) {
    return (
      <Card sx={{ p: 4, textAlign: 'center', borderRadius: 4 }}>
        <Typography color="text.secondary">Your cart is empty. Add items before checkout.</Typography>
        <Button variant="contained" sx={{ mt: 2, borderRadius: 999 }} onClick={() => navigate('/cafe/menu')}>
          Browse Menu
        </Button>
      </Card>
    );
  }

  if (orderSuccess) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" sx={{ py: 10 }}>
        <motion.div
          initial={{ opacity: 0, scale: 0.7 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ type: 'spring', stiffness: 200, damping: 14 }}
        >
          <Card sx={{ p: 6, textAlign: 'center', borderRadius: 5, maxWidth: 420 }}>
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.2, type: 'spring', stiffness: 260, damping: 12 }}
            >
              <CheckCircleIcon sx={{ fontSize: 96, color: 'success.main' }} />
            </motion.div>
            <Typography variant="h5" fontWeight={800} sx={{ mt: 2 }}>
              Order Placed!
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 1 }}>
              Redirecting you to your order details...
            </Typography>
          </Card>
        </motion.div>
      </Box>
    );
  }

  return (
    <Box sx={{ pb: 4 }}>
      <Typography variant="h5" fontWeight={800} mb={3}>
        Checkout
      </Typography>

      <Stepper activeStep={activeStep} sx={{ mb: 4 }} alternativeLabel>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <AnimatePresence mode="wait">
            {activeStep === 0 && (
              <motion.div
                key="address"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.25 }}
              >
                <Card sx={{ p: 3, borderRadius: 4 }}>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <LocationOnIcon color="primary" />
                    <Typography variant="subtitle1" fontWeight={700} sx={{ flex: 1 }}>
                      Delivery Address
                    </Typography>
                    <Button size="small" onClick={() => setShowAddAddress((prev) => !prev)}>
                      {showAddAddress ? 'Cancel' : 'Add New Address'}
                    </Button>
                  </Box>

                  {addresses.length > 0 && !showAddAddress && (
                    <FormControl sx={{ mt: 1, width: '100%' }}>
                      <RadioGroup value={addressId} onChange={(e) => setAddressId(Number(e.target.value))}>
                        <Grid container spacing={1.5}>
                          {addresses.map((address) => (
                            <Grid item xs={12} key={address.id}>
                              <Paper
                                variant="outlined"
                                onClick={() => setAddressId(address.id)}
                                sx={{
                                  p: 1.5,
                                  borderRadius: 3,
                                  cursor: 'pointer',
                                  borderColor: addressId === address.id ? 'primary.main' : 'divider',
                                  borderWidth: addressId === address.id ? 2 : 1,
                                  transition: 'all .2s ease',
                                  '&:hover': { boxShadow: '0 6px 18px rgba(0,0,0,0.06)' },
                                }}
                              >
                                <FormControlLabel
                                  value={address.id}
                                  control={<Radio />}
                                  sx={{ m: 0, width: '100%', alignItems: 'flex-start' }}
                                  label={
                                    <Box sx={{ ml: 0.5 }}>
                                      <Box display="flex" alignItems="center" gap={0.75}>
                                        {addressIcon(address.addressType)}
                                        <Typography variant="body2" fontWeight={700}>
                                          {address.label || address.addressType}
                                        </Typography>
                                        {address.isDefault && (
                                          <Chip label="Default" size="small" color="primary" sx={{ height: 20 }} />
                                        )}
                                      </Box>
                                      <Typography variant="body2" color="text.secondary">
                                        {address.addressLine1}
                                        {address.addressLine2 ? `, ${address.addressLine2}` : ''}, {address.city},{' '}
                                        {address.state} - {address.pincode}
                                      </Typography>
                                      <Typography variant="caption" color="text.secondary">
                                        Contact: {address.contactNumber}
                                      </Typography>
                                    </Box>
                                  }
                                />
                              </Paper>
                            </Grid>
                          ))}
                        </Grid>
                      </RadioGroup>
                    </FormControl>
                  )}

                  {showAddAddress && (
                    <Box display="flex" flexWrap="wrap" gap={2} sx={{ mt: 2 }}>
                      <TextField
                        label="Label (Home/Work)"
                        sx={{ flex: 1, minWidth: 200 }}
                        value={newAddress.label}
                        onChange={(e) => updateNewAddress({ label: e.target.value })}
                      />
                      <TextField
                        label="Address Line 1"
                        required
                        sx={{ flex: 1, minWidth: 200 }}
                        value={newAddress.addressLine1}
                        onChange={(e) => updateNewAddress({ addressLine1: e.target.value })}
                      />
                      <TextField
                        label="Address Line 2"
                        sx={{ flex: 1, minWidth: 200 }}
                        value={newAddress.addressLine2}
                        onChange={(e) => updateNewAddress({ addressLine2: e.target.value })}
                      />
                      <TextField
                        label="City"
                        required
                        sx={{ flex: 1, minWidth: 150 }}
                        value={newAddress.city}
                        onChange={(e) => updateNewAddress({ city: e.target.value })}
                      />
                      <TextField
                        label="State"
                        required
                        sx={{ flex: 1, minWidth: 150 }}
                        value={newAddress.state}
                        onChange={(e) => updateNewAddress({ state: e.target.value })}
                      />
                      <TextField
                        label="Pincode"
                        required
                        sx={{ flex: 1, minWidth: 150 }}
                        value={newAddress.pincode}
                        onChange={(e) => updateNewAddress({ pincode: e.target.value })}
                      />
                      <TextField
                        label="Landmark"
                        sx={{ flex: 1, minWidth: 150 }}
                        value={newAddress.landmark}
                        onChange={(e) => updateNewAddress({ landmark: e.target.value })}
                      />
                      <TextField
                        label="Contact Number"
                        required
                        sx={{ flex: 1, minWidth: 150 }}
                        value={newAddress.contactNumber}
                        onChange={(e) => updateNewAddress({ contactNumber: e.target.value })}
                      />
                      <TextField
                        label="Address Type"
                        select
                        sx={{ flex: 1, minWidth: 150 }}
                        value={newAddress.addressType}
                        onChange={(e) => updateNewAddress({ addressType: e.target.value })}
                      >
                        <MenuItem value="HOME">Home</MenuItem>
                        <MenuItem value="WORK">Work</MenuItem>
                        <MenuItem value="OTHER">Other</MenuItem>
                      </TextField>
                      <Box width="100%">
                        <Button
                          variant="contained"
                          sx={{ borderRadius: 999 }}
                          disabled={validateAddress()}
                          onClick={saveAddress}
                        >
                          Save Address
                        </Button>
                      </Box>
                    </Box>
                  )}

                  <TextField
                    label="Delivery Instructions (optional)"
                    fullWidth
                    multiline
                    minRows={2}
                    sx={{ mt: 3 }}
                    value={deliveryInstructions}
                    onChange={(e) => setDeliveryInstructions(e.target.value)}
                  />
                </Card>
              </motion.div>
            )}

            {activeStep === 1 && (
              <motion.div
                key="payment"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.25 }}
              >
                <Card sx={{ p: 3, borderRadius: 4 }}>
                  <Box display="flex" alignItems="center" gap={1} mb={2}>
                    <PaymentIcon color="primary" />
                    <Typography variant="subtitle1" fontWeight={700}>
                      Payment Method
                    </Typography>
                  </Box>
                  <Grid container spacing={1.5}>
                    {paymentOptions.map((opt) => (
                      <Grid item xs={12} sm={6} key={opt.value}>
                        <Paper
                          component={motion.div}
                          whileHover={{ y: -3 }}
                          whileTap={{ scale: 0.97 }}
                          variant="outlined"
                          onClick={() => setPaymentMethod(opt.value)}
                          sx={{
                            p: 2,
                            borderRadius: 3,
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 1.5,
                            borderColor: paymentMethod === opt.value ? 'primary.main' : 'divider',
                            borderWidth: paymentMethod === opt.value ? 2 : 1,
                            bgcolor: paymentMethod === opt.value ? 'action.selected' : 'transparent',
                          }}
                        >
                          <Avatar sx={{ bgcolor: 'primary.light', width: 36, height: 36 }}>{opt.icon}</Avatar>
                          <Typography fontWeight={600}>{opt.label}</Typography>
                          {paymentMethod === opt.value && (
                            <CheckCircleIcon color="primary" sx={{ ml: 'auto' }} fontSize="small" />
                          )}
                        </Paper>
                      </Grid>
                    ))}
                  </Grid>

                  {loyaltyBalance?.points > 0 && (
                    <Paper
                      variant="outlined"
                      sx={{
                        mt: 2.5,
                        p: 2,
                        borderRadius: 3,
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1,
                        bgcolor: 'rgba(255,138,0,0.06)',
                      }}
                    >
                      <StarsIcon color="secondary" />
                      <FormControlLabel
                        sx={{ flex: 1, m: 0 }}
                        control={
                          <Checkbox
                            checked={useLoyaltyPoints}
                            onChange={(e) => setUseLoyaltyPoints(e.target.checked)}
                          />
                        }
                        label={
                          <Typography variant="body2">
                            Redeem {loyaltyBalance.points} loyalty points (₹{loyaltyBalance.redeemableValue} off)
                          </Typography>
                        }
                      />
                    </Paper>
                  )}
                </Card>
              </motion.div>
            )}

            {activeStep === 2 && (
              <motion.div
                key="review"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 20 }}
                transition={{ duration: 0.25 }}
              >
                <Card sx={{ p: 3, borderRadius: 4 }}>
                  <Box display="flex" alignItems="center" gap={1} mb={2}>
                    <FactCheckIcon color="primary" />
                    <Typography variant="subtitle1" fontWeight={700}>
                      Review Your Order
                    </Typography>
                  </Box>

                  <Typography variant="body2" fontWeight={700} sx={{ mb: 0.5 }}>
                    Deliver to:
                  </Typography>
                  {selectedAddress && (
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      {selectedAddress.addressLine1}, {selectedAddress.city}, {selectedAddress.state} -{' '}
                      {selectedAddress.pincode}
                    </Typography>
                  )}

                  <Typography variant="body2" fontWeight={700} sx={{ mb: 0.5 }}>
                    Payment Method:
                  </Typography>
                  <Chip label={paymentMethod} color="primary" size="small" sx={{ mb: 2 }} />

                  {deliveryInstructions && (
                    <>
                      <Typography variant="body2" fontWeight={700} sx={{ mb: 0.5 }}>
                        Delivery Instructions:
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        {deliveryInstructions}
                      </Typography>
                    </>
                  )}

                  <Divider sx={{ my: 1.5 }} />
                  <Typography variant="body2" fontWeight={700} sx={{ mb: 1 }}>
                    Items ({cart?.items?.length || 0})
                  </Typography>
                  {cart?.items?.map((item: any) => (
                    <Box key={item.id} display="flex" justifyContent="space-between" sx={{ mb: 0.5 }}>
                      <Typography variant="body2">
                        {item.productName} × {item.quantity}
                      </Typography>
                      <Typography variant="body2">₹{item.subtotal}</Typography>
                    </Box>
                  ))}
                </Card>
              </motion.div>
            )}
          </AnimatePresence>

          <Box display="flex" justifyContent="space-between" sx={{ mt: 3 }}>
            <Button
              disabled={activeStep === 0}
              onClick={() => setActiveStep((s) => s - 1)}
              sx={{ borderRadius: 999 }}
            >
              Back
            </Button>
            {activeStep < steps.length - 1 ? (
              <Button
                variant="contained"
                sx={{ borderRadius: 999, px: 4 }}
                disabled={activeStep === 0 && !addressId}
                onClick={() => setActiveStep((s) => s + 1)}
              >
                Continue
              </Button>
            ) : (
              <Button
                component={motion.button}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.97 }}
                variant="contained"
                sx={{ borderRadius: 999, px: 4 }}
                disabled={!addressId || !paymentMethod || placing}
                onClick={placeOrder}
              >
                {placing ? 'Placing Order...' : 'Place Order'}
              </Button>
            )}
          </Box>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ p: 3, borderRadius: 4, position: 'sticky', top: 88 }}>
            <Typography variant="subtitle1" fontWeight={700} mb={1.5}>
              Order Summary
            </Typography>
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
                <Typography variant="body2" color="success.main">
                  -₹{cart.discount}
                </Typography>
              </Box>
            )}
            <Box display="flex" justifyContent="space-between">
              <Typography variant="body2" color="text.secondary">
                Delivery Charge
              </Typography>
              <Typography variant="body2">
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

            <Divider sx={{ my: 2 }} />
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Typography variant="h6" fontWeight={800}>
                Total
              </Typography>
              <Typography variant="h6" fontWeight={800} color="primary.main">
                ₹{cart?.total}
              </Typography>
            </Box>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
