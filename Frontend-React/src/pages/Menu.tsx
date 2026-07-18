import { useEffect, useState } from 'react';
import {
  Card,
  Box,
  TextField,
  Chip,
  Grid,
  Typography,
  Button,
  IconButton,
  FormControlLabel,
  Switch,
  Badge,
  Tooltip,
  InputAdornment,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import MicIcon from '@mui/icons-material/Mic';
import MicNoneIcon from '@mui/icons-material/MicNone';
import SearchIcon from '@mui/icons-material/Search';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { CategoryService } from '../services/category.service';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';
import FoodCard from '../components/FoodCard';
import FoodCardSkeleton from '../components/FoodCardSkeleton';

// Voice Search: Web Speech API (built into Chrome/Edge). Feature-detected below so the mic
// button simply doesn't render in browsers without support (e.g. Firefox) rather than erroring.
const SpeechRecognitionCtor: any =
  (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

export default function Menu() {
  const navigate = useNavigate();
  const { openSnackBar } = useSnackbarService();
  const [categorys, setCategorys] = useState<any[]>([]);
  const [products, setProducts] = useState<any[]>([]);
  const [recommended, setRecommended] = useState<any[]>([]);
  const [categoryId, setCategoryId] = useState<any>('');
  const [vegOnly, setVegOnly] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [cartQuantities, setCartQuantities] = useState<Record<number, number>>({});
  const [cartItemCount, setCartItemCount] = useState(0);
  const [listening, setListening] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    CategoryService.getFilteredCategorys()
      .then((response: any) => setCategorys(response.data || []))
      .catch(() => undefined);
    ProductService.getRecommendations()
      .then((response: any) => setRecommended(response.data || []))
      .catch(() => undefined);
    refreshCart();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    fetchProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [categoryId, vegOnly, keyword]);

  const refreshCart = () => {
    CartService.getCart()
      .then((response: any) => {
        const items = response.data?.items || [];
        const map: Record<number, number> = {};
        items.forEach((item: any) => {
          map[item.productId] = item.quantity;
        });
        setCartQuantities(map);
        setCartItemCount(response.data?.itemCount || 0);
      })
      .catch(() => undefined);
  };

  const fetchProducts = () => {
    setLoading(true);
    ProductService.search({
      categoryId: categoryId || undefined,
      isVeg: vegOnly ? true : undefined,
      keyword: keyword || undefined,
      status: 'true',
      size: 100,
    })
      .then((response: any) => setProducts(response.data?.content || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      })
      .finally(() => setLoading(false));
  };

  const addToCart = (productId: number) => {
    CartService.addItem({ productId, quantity: 1 })
      .then(() => {
        openSnackBar('Added to cart', 'Success');
        refreshCart();
      })
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
  };

  const startVoiceSearch = () => {
    if (!SpeechRecognitionCtor) {
      openSnackBar('Voice search is not supported in this browser', GlobalConstants.error);
      return;
    }
    const recognition = new SpeechRecognitionCtor();
    recognition.lang = 'en-US';
    recognition.interimResults = false;
    recognition.maxAlternatives = 1;
    recognition.onstart = () => setListening(true);
    recognition.onend = () => setListening(false);
    recognition.onerror = () => {
      setListening(false);
      openSnackBar('Could not hear you, please try again', GlobalConstants.error);
    };
    recognition.onresult = (event: any) => {
      const transcript = event.results?.[0]?.[0]?.transcript || '';
      if (transcript) {
        setKeyword(transcript);
      }
    };
    recognition.start();
  };

  const changeQuantity = (product: any, delta: number) => {
    const current = cartQuantities[product.id] || 0;
    const next = current + delta;
    if (current === 0 && delta > 0) {
      addToCart(product.id);
      return;
    }
    if (next <= 0) {
      CartService.getCart().then((response: any) => {
        const item = (response.data?.items || []).find((i: any) => i.productId === product.id);
        if (item) {
          CartService.removeItem(item.id).then(refreshCart);
        }
      });
      return;
    }
    CartService.getCart().then((response: any) => {
      const item = (response.data?.items || []).find((i: any) => i.productId === product.id);
      if (item) {
        CartService.updateItem(item.id, { quantity: next }).then(refreshCart);
      }
    });
  };

  return (
    <>
      {/* Sticky search + category bar */}
      <Box
        sx={{
          position: 'sticky',
          top: 64,
          zIndex: 5,
          bgcolor: 'background.default',
          pt: 1,
          pb: 2,
        }}
      >
        <Card sx={{ p: 2, mb: 2 }}>
          <Box display="flex" alignItems="center" flexWrap="wrap" gap={2}>
            <Typography variant="h6" fontWeight={800} sx={{ fontFamily: 'Poppins, sans-serif' }}>
              🍽️ Our Menu
            </Typography>
            <Button
              variant="contained"
              startIcon={
                <Badge badgeContent={cartItemCount} color="secondary">
                  <ShoppingCartIcon />
                </Badge>
              }
              sx={{ ml: 'auto' }}
              onClick={() => navigate('/cafe/cart')}
            >
              View Cart
            </Button>
          </Box>

          <Box display="flex" flexWrap="wrap" gap={2} alignItems="center" mt={2}>
            <TextField
              placeholder="Search for dishes, cuisines..."
              variant="outlined"
              size="small"
              sx={{ flex: 1, minWidth: 220 }}
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon color="action" />
                  </InputAdornment>
                ),
                endAdornment: SpeechRecognitionCtor ? (
                  <InputAdornment position="end">
                    <Tooltip title={listening ? 'Listening…' : 'Search by voice'}>
                      <IconButton size="small" color={listening ? 'error' : 'primary'} onClick={startVoiceSearch}>
                        {listening ? <MicIcon /> : <MicNoneIcon />}
                      </IconButton>
                    </Tooltip>
                  </InputAdornment>
                ) : undefined,
              }}
            />
            <FormControlLabel
              control={<Switch checked={vegOnly} onChange={(e) => setVegOnly(e.target.checked)} color="success" />}
              label="Veg Only"
            />
          </Box>

          {/* Horizontal scrolling animated category chips */}
          <Box
            sx={{
              display: 'flex',
              gap: 1,
              mt: 2,
              overflowX: 'auto',
              pb: 0.5,
              '&::-webkit-scrollbar': { height: 6 },
              '&::-webkit-scrollbar-thumb': { bgcolor: 'divider', borderRadius: 3 },
            }}
          >
            <motion.div whileTap={{ scale: 0.95 }}>
              <Chip
                label="All"
                onClick={() => setCategoryId('')}
                color={categoryId === '' ? 'primary' : 'default'}
                variant={categoryId === '' ? 'filled' : 'outlined'}
                sx={{ px: 1 }}
              />
            </motion.div>
            {categorys.map((category) => (
              <motion.div whileTap={{ scale: 0.95 }} key={category.id}>
                <Chip
                  label={category.name}
                  onClick={() => setCategoryId(category.id)}
                  color={categoryId === category.id ? 'primary' : 'default'}
                  variant={categoryId === category.id ? 'filled' : 'outlined'}
                  sx={{ px: 1, whiteSpace: 'nowrap' }}
                />
              </motion.div>
            ))}
          </Box>
        </Card>
      </Box>

      {recommended.length > 0 && (
        <Card sx={{ p: 2, mb: 2 }}>
          <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1 }}>
            ⭐ Recommended for You
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, overflowX: 'auto', pb: 1 }}>
            {recommended.map((product) => (
              <Card
                key={`rec-${product.id}`}
                variant="outlined"
                sx={{ p: 1.5, minWidth: 200, display: 'flex', flexDirection: 'column', gap: 0.5, flexShrink: 0 }}
              >
                <Typography variant="body2" fontWeight={700} noWrap>
                  {product.name}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {product.categoryName}
                  {product.rating ? ` • ⭐ ${product.rating}` : ''}
                </Typography>
                <Box display="flex" alignItems="center" justifyContent="space-between">
                  <Typography variant="body2" fontWeight={700} color="primary.dark">
                    ₹{product.price}
                  </Typography>
                  <Button size="small" variant="contained" onClick={() => addToCart(product.id)}>
                    Add
                  </Button>
                </Box>
              </Card>
            ))}
          </Box>
        </Card>
      )}

      {loading ? (
        <FoodCardSkeleton count={6} />
      ) : (
        <Grid container spacing={2}>
          <AnimatePresence>
            {products.map((product, i) => (
              <Grid item xs={12} sm={6} md={4} key={product.id}>
                <motion.div
                  initial={{ opacity: 0, y: 16 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.3, delay: Math.min(i * 0.04, 0.4) }}
                  style={{ height: '100%' }}
                >
                  <FoodCard
                    product={product}
                    quantity={cartQuantities[product.id] || 0}
                    onAdd={addToCart}
                    onChangeQuantity={changeQuantity}
                  />
                </motion.div>
              </Grid>
            ))}
          </AnimatePresence>
          {products.length === 0 && (
            <Grid item xs={12}>
              <Box textAlign="center" sx={{ py: 6 }}>
                <Typography variant="h1" sx={{ fontSize: 64, mb: 1 }}>
                  🍽️
                </Typography>
                <Typography color="text.secondary">No items found. Try a different search or category.</Typography>
              </Box>
            </Grid>
          )}
        </Grid>
      )}
    </>
  );
}
