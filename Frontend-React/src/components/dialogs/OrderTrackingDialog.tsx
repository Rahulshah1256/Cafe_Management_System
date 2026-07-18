import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  DialogContent,
  Alert,
  Button,
  Box,
  Avatar,
  Card,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import SoupKitchenIcon from '@mui/icons-material/SoupKitchen';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import DeliveryDiningIcon from '@mui/icons-material/DeliveryDining';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { motion } from 'framer-motion';

const TIMELINE_STEPS = ['PLACED', 'ACCEPTED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED'];

const STEP_META: Record<string, { label: string; icon: JSX.Element }> = {
  PLACED: { label: 'Order Placed', icon: <ReceiptLongIcon /> },
  ACCEPTED: { label: 'Accepted', icon: <ThumbUpIcon /> },
  PREPARING: { label: 'Preparing', icon: <SoupKitchenIcon /> },
  OUT_FOR_DELIVERY: { label: 'Out for Delivery', icon: <DeliveryDiningIcon /> },
  DELIVERED: { label: 'Delivered', icon: <CheckCircleIcon /> },
};

const CANCELLABLE_STATUSES = ['PLACED', 'ACCEPTED'];

interface Props {
  data: any;
  onClose: () => void;
  onCancelOrder: (row: any) => void;
}

export default function OrderTrackingDialog({ data, onClose, onCancelOrder }: Props) {
  const status: string = data?.orderStatus || 'PLACED';
  const isCancelled = status === 'CANCELLED';
  const activeIndex = TIMELINE_STEPS.indexOf(status);
  const canCancel = CANCELLABLE_STATUSES.includes(status);

  return (
    <>
      <AppBar position="static" color="primary" sx={{ borderRadius: 0 }}>
        <Toolbar>
          <Typography sx={{ flex: 1, fontWeight: 700 }}>
            Track Order {data?.uuid ? `#${data.uuid.slice(-6)}` : ''}
          </Typography>
          <IconButton color="inherit" onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Toolbar>
      </AppBar>
      <DialogContent sx={{ bgcolor: 'background.default' }}>
        {isCancelled ? (
          <Alert severity="error" sx={{ my: 2, borderRadius: 3 }}>
            This order has been cancelled.
          </Alert>
        ) : (
          <Box sx={{ my: 4, px: { xs: 0, sm: 2 } }}>
            <Box
              sx={{
                display: 'flex',
                alignItems: 'flex-start',
                justifyContent: 'space-between',
                position: 'relative',
              }}
            >
              {/* connecting track */}
              <Box
                sx={{
                  position: 'absolute',
                  top: 24,
                  left: '10%',
                  right: '10%',
                  height: 4,
                  bgcolor: 'action.disabledBackground',
                  borderRadius: 2,
                  zIndex: 0,
                }}
              />
              <motion.div
                initial={{ width: 0 }}
                animate={{
                  width: activeIndex <= 0 ? '0%' : `${(activeIndex / (TIMELINE_STEPS.length - 1)) * 80}%`,
                }}
                transition={{ duration: 0.8, ease: 'easeInOut' }}
                style={{
                  position: 'absolute',
                  top: 24,
                  left: '10%',
                  height: 4,
                  background: 'linear-gradient(90deg, #E5202A, #FF8A00)',
                  borderRadius: 2,
                  zIndex: 1,
                }}
              />

              {TIMELINE_STEPS.map((step, i) => {
                const done = i < activeIndex;
                const active = i === activeIndex;
                const meta = STEP_META[step];
                return (
                  <Box
                    key={step}
                    sx={{
                      position: 'relative',
                      zIndex: 2,
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      flex: 1,
                    }}
                  >
                    <motion.div
                      animate={active ? { scale: [1, 1.15, 1] } : {}}
                      transition={{ duration: 1.4, repeat: active ? Infinity : 0 }}
                    >
                      <Avatar
                        sx={{
                          width: 48,
                          height: 48,
                          bgcolor: done || active ? 'primary.main' : 'action.disabledBackground',
                          color: done || active ? '#fff' : 'text.disabled',
                          boxShadow: active ? '0 0 0 6px rgba(229,32,42,0.15)' : 'none',
                          transition: 'all .3s ease',
                        }}
                      >
                        {meta.icon}
                      </Avatar>
                    </motion.div>
                    <Typography
                      variant="caption"
                      align="center"
                      sx={{
                        mt: 1,
                        fontWeight: active ? 700 : 500,
                        color: done || active ? 'text.primary' : 'text.secondary',
                        maxWidth: 80,
                      }}
                    >
                      {meta.label}
                    </Typography>
                  </Box>
                );
              })}
            </Box>
          </Box>
        )}

        <Card sx={{ p: 2, mt: 2, borderRadius: 3 }}>
          <Box display="flex" justifyContent="space-between" sx={{ mb: 1 }}>
            <Typography variant="body2" color="text.secondary">
              Payment Status
            </Typography>
            <Typography variant="body2" fontWeight={700}>
              {data?.paymentStatus || '-'}
            </Typography>
          </Box>
          <Box display="flex" justifyContent="space-between">
            <Typography variant="body2" color="text.secondary">
              Total
            </Typography>
            <Typography variant="body2" fontWeight={700} color="primary.main">
              ₹{data?.total}
            </Typography>
          </Box>
        </Card>

        {canCancel && (
          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              color="error"
              variant="outlined"
              sx={{ borderRadius: 999 }}
              onClick={() => onCancelOrder(data)}
            >
              Cancel Order
            </Button>
          </Box>
        )}
      </DialogContent>
    </>
  );
}
