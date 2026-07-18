import { useSnackbar } from 'notistack';

/**
 * Mirrors the Angular SnackbarService.openSnackBar(message, action).
 * action === 'error' -> black snackbar (error variant), otherwise green success.
 */
export function useSnackbarService() {
  const { enqueueSnackbar } = useSnackbar();

  const openSnackBar = (message: string, action: string) => {
    enqueueSnackbar(message, {
      variant: action === 'error' ? 'error' : 'success',
      anchorOrigin: { horizontal: 'center', vertical: 'top' },
      autoHideDuration: 2000,
    });
  };

  return { openSnackBar };
}
