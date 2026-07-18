import { Box, Card, Chip, IconButton, Typography } from '@mui/material';
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import RemoveIcon from '@mui/icons-material/Remove';
import AddIcon from '@mui/icons-material/Add';
import { motion } from 'framer-motion';
import { getFoodImage, DEFAULT_FOOD_IMAGE } from '../shared/foodImage';

interface FoodCardProps {
  product: any;
  quantity: number;
  onAdd: (id: number) => void;
  onChangeQuantity: (product: any, delta: number) => void;
}

// Reusable premium food card: banner strip with veg/best-seller/new badges,
// name/description/rating, and an animated add-to-cart / stepper control.
export default function FoodCard({ product, quantity, onAdd, onChangeQuantity }: FoodCardProps) {
  return (
    <motion.div
      whileHover={{ y: -8 }}
      transition={{ type: 'spring', stiffness: 300, damping: 20 }}
      style={{ height: '100%' }}
    >
      <Card
        sx={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          '&:hover': { boxShadow: '0 18px 34px rgba(33,33,33,0.16)' },
        }}
      >
        <Box sx={{ position: 'relative', height: 160, overflow: 'hidden' }}>
          <motion.img
            src={getFoodImage(product)}
            alt={product.name}
            loading="lazy"
            onError={(e) => {
              (e.target as HTMLImageElement).src = DEFAULT_FOOD_IMAGE;
            }}
            whileHover={{ scale: 1.12 }}
            transition={{ duration: 0.4, ease: 'easeOut' }}
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              display: 'block',
            }}
          />
          <Box
            sx={{
              position: 'absolute',
              inset: 0,
              background: 'linear-gradient(180deg, rgba(0,0,0,0) 40%, rgba(0,0,0,0.55) 100%)',
              display: 'flex',
              alignItems: 'flex-end',
              justifyContent: 'space-between',
              p: 1.5,
              pointerEvents: 'none',
            }}
          >
            <Box display="flex" gap={0.75} flexWrap="wrap">
              <Chip
                size="small"
                label={product.isVeg === false ? 'Non-Veg' : 'Veg'}
                sx={{ bgcolor: '#fff', color: product.isVeg === false ? 'error.main' : 'success.main' }}
              />
              {product.bestSeller && (
                <Chip size="small" label="🔥 Best Seller" sx={{ bgcolor: '#fff', color: 'secondary.dark' }} />
              )}
              {product.newArrival && (
                <Chip size="small" label="✨ New" sx={{ bgcolor: '#fff', color: 'info.dark' }} />
              )}
            </Box>
          </Box>
        </Box>
        <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1, flexGrow: 1 }}>
          <Typography variant="subtitle1" fontWeight={700}>
            {product.name}
          </Typography>
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
              minHeight: 40,
              display: '-webkit-box',
              WebkitLineClamp: 2,
              WebkitBoxOrient: 'vertical',
              overflow: 'hidden',
            }}
          >
            {product.description}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {product.categoryName}
            {product.rating ? ` • ⭐ ${product.rating} (${product.ratingCount || 0})` : ''}
          </Typography>
          <Box display="flex" alignItems="center" justifyContent="space-between" mt="auto">
            <Typography variant="h6" color="primary.dark" fontWeight={800}>
              ₹{product.price}
            </Typography>
            {quantity === 0 ? (
              <motion.div whileTap={{ scale: 0.92 }}>
                <IconButton
                  size="small"
                  onClick={() => onAdd(product.id)}
                  sx={{
                    bgcolor: 'primary.main',
                    color: '#fff',
                    borderRadius: 999,
                    px: 1.5,
                    '&:hover': { bgcolor: 'primary.dark' },
                  }}
                >
                  <AddShoppingCartIcon fontSize="small" sx={{ mr: 0.5 }} />
                  <Typography variant="button" sx={{ fontWeight: 700 }}>
                    Add
                  </Typography>
                </IconButton>
              </motion.div>
            ) : (
              <Box
                display="flex"
                alignItems="center"
                gap={1}
                sx={{ bgcolor: 'primary.main', borderRadius: 999, px: 0.5 }}
              >
                <IconButton size="small" sx={{ color: '#fff' }} onClick={() => onChangeQuantity(product, -1)}>
                  <RemoveIcon fontSize="small" />
                </IconButton>
                <Typography sx={{ color: '#fff', fontWeight: 700 }}>{quantity}</Typography>
                <IconButton size="small" sx={{ color: '#fff' }} onClick={() => onChangeQuantity(product, 1)}>
                  <AddIcon fontSize="small" />
                </IconButton>
              </Box>
            )}
          </Box>
        </Box>
      </Card>
    </motion.div>
  );
}
