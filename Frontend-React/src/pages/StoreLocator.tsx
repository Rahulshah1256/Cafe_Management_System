import { useEffect, useState } from 'react';
import { Card, Grid, Typography, Button, Box, Divider } from '@mui/material';
import PlaceIcon from '@mui/icons-material/Place';
import PhoneIcon from '@mui/icons-material/Phone';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import DirectionsIcon from '@mui/icons-material/Directions';
import { StoreService } from '../services/store.service';
import { GlobalConstants } from '../shared/globalConstants';
import { useSnackbarService } from '../shared/useSnackbarService';

export default function StoreLocator() {
  const { openSnackBar } = useSnackbarService();
  const [stores, setStores] = useState<any[]>([]);

  useEffect(() => {
    StoreService.getActiveStores()
      .then((response: any) => setStores(response.data || []))
      .catch((error: any) => {
        const message = error?.response?.data?.message || GlobalConstants.genericError;
        openSnackBar(message, GlobalConstants.error);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const directionsUrl = (store: any) => {
    if (store.latitude != null && store.longitude != null) {
      return `https://www.google.com/maps/search/?api=1&query=${store.latitude},${store.longitude}`;
    }
    const address = [store.addressLine1, store.addressLine2, store.city, store.state, store.pincode]
      .filter(Boolean)
      .join(', ');
    return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address)}`;
  };

  return (
    <>
      <Card sx={{ p: 2 }}>
        <Typography variant="h6" fontWeight="bold">
          Store Locator
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Find a cafe outlet near you.
        </Typography>
      </Card>
      <Divider sx={{ my: 2 }} />

      <Grid container spacing={2}>
        {stores.map((store) => (
          <Grid item xs={12} sm={6} md={4} key={store.id}>
            <Card sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Typography variant="subtitle1" fontWeight="bold">
                {store.name}
              </Typography>
              <Box display="flex" gap={1} alignItems="flex-start">
                <PlaceIcon fontSize="small" color="action" sx={{ mt: 0.3 }} />
                <Typography variant="body2" color="text.secondary">
                  {[store.addressLine1, store.addressLine2, store.city, store.state, store.pincode]
                    .filter(Boolean)
                    .join(', ')}
                </Typography>
              </Box>
              {store.phone && (
                <Box display="flex" gap={1} alignItems="center">
                  <PhoneIcon fontSize="small" color="action" />
                  <Typography variant="body2" color="text.secondary">
                    {store.phone}
                  </Typography>
                </Box>
              )}
              {store.openingHours && (
                <Box display="flex" gap={1} alignItems="center">
                  <AccessTimeIcon fontSize="small" color="action" />
                  <Typography variant="body2" color="text.secondary">
                    {store.openingHours}
                  </Typography>
                </Box>
              )}
              <Button
                variant="contained"
                size="small"
                startIcon={<DirectionsIcon />}
                sx={{ mt: 'auto', alignSelf: 'flex-start' }}
                href={directionsUrl(store)}
                target="_blank"
                rel="noopener noreferrer"
              >
                Get Directions
              </Button>
            </Card>
          </Grid>
        ))}
        {stores.length === 0 && (
          <Grid item xs={12}>
            <Typography align="center" color="text.secondary" sx={{ py: 4 }}>
              No store locations available yet.
            </Typography>
          </Grid>
        )}
      </Grid>
    </>
  );
}
