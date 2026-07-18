import { Card, Box, Skeleton, Grid } from '@mui/material';

// Skeleton placeholder shown while menu items load, mirroring FoodCard's layout
// so there's no layout shift when real content arrives.
export default function FoodCardSkeleton({ count = 6 }: { count?: number }) {
  return (
    <Grid container spacing={2}>
      {Array.from({ length: count }).map((_, i) => (
        <Grid item xs={12} sm={6} md={4} key={i}>
          <Card sx={{ overflow: 'hidden' }}>
            <Skeleton variant="rectangular" height={160} animation="wave" />
            <Box sx={{ p: 2 }}>
              <Skeleton variant="text" width="60%" height={28} animation="wave" />
              <Skeleton variant="text" width="90%" animation="wave" />
              <Skeleton variant="text" width="70%" animation="wave" />
              <Box display="flex" justifyContent="space-between" alignItems="center" mt={2}>
                <Skeleton variant="text" width={60} height={32} animation="wave" />
                <Skeleton variant="rounded" width={80} height={32} animation="wave" />
              </Box>
            </Box>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
}
