import { createTheme } from '@mui/material/styles';

// Premium food-ordering design system: Domino's/Uber-Eats-inspired.
// Red = primary brand/CTA, Orange = secondary/highlight, Green = success/veg/offers.
export function buildTheme(mode: 'light' | 'dark') {
  const isDark = mode === 'dark';

  return createTheme({
    palette: {
      mode,
      primary: {
        main: '#E5202A',
        light: '#FF5B52',
        dark: '#B0000F',
        contrastText: '#fff',
      },
      secondary: {
        main: '#FF8A00',
        light: '#FFB74D',
        dark: '#C25E00',
        contrastText: '#fff',
      },
      error: {
        main: '#D32F2F',
      },
      warning: {
        main: '#FF8A00',
      },
      success: {
        main: '#1DB854',
      },
      background: {
        default: isDark ? '#121212' : '#F7F7F8',
        paper: isDark ? '#1E1E1E' : '#FFFFFF',
      },
      text: {
        primary: isDark ? '#F2F2F2' : '#212121',
        secondary: isDark ? '#B0B0B0' : '#6B6B6B',
      },
    },
    shape: {
      borderRadius: 16,
    },
    typography: {
      fontFamily: '"Inter", "Poppins", "Roboto", "Helvetica Neue", sans-serif',
      h1: { fontFamily: '"Poppins", sans-serif', fontWeight: 800, letterSpacing: -0.5 },
      h2: { fontFamily: '"Poppins", sans-serif', fontWeight: 800, letterSpacing: -0.5 },
      h3: { fontFamily: '"Poppins", sans-serif', fontWeight: 700 },
      h4: { fontFamily: '"Poppins", sans-serif', fontWeight: 700 },
      h5: { fontFamily: '"Poppins", sans-serif', fontWeight: 700 },
      h6: { fontFamily: '"Poppins", sans-serif', fontWeight: 700 },
      button: { fontWeight: 700, textTransform: 'none' },
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 999,
            paddingLeft: 22,
            paddingRight: 22,
            paddingTop: 10,
            paddingBottom: 10,
            boxShadow: 'none',
            transition: 'transform 0.18s ease, box-shadow 0.18s ease',
            '&:active': {
              transform: 'scale(0.97)',
            },
          },
          contained: {
            boxShadow: '0 6px 16px rgba(229,32,42,0.28)',
            '&:hover': {
              boxShadow: '0 10px 22px rgba(229,32,42,0.38)',
              transform: 'translateY(-1px)',
            },
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 20,
            boxShadow: isDark ? '0 2px 16px rgba(0,0,0,0.4)' : '0 4px 20px rgba(33,33,33,0.08)',
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: { fontWeight: 700, borderRadius: 8 },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundImage: isDark
              ? 'linear-gradient(90deg, #B0000F 0%, #7A1300 100%)'
              : 'linear-gradient(90deg, #E5202A 0%, #FF8A00 100%)',
            boxShadow: '0 2px 16px rgba(229,32,42,0.25)',
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          rounded: { borderRadius: 20 },
        },
      },
    },
  });
}

const theme = buildTheme('light');
export default theme;
