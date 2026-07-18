// Loads the Razorpay Checkout widget script on demand (once) and exposes a small
// promise-based wrapper around it. The widget itself is what supports UPI, Cards,
// Net Banking and Wallets in a single hosted flow - we don't need separate integrations
// per payment method.

const RAZORPAY_SCRIPT_SRC = 'https://checkout.razorpay.com/v1/checkout.js';

let scriptPromise: Promise<boolean> | null = null;

function loadRazorpayScript(): Promise<boolean> {
  if ((window as any).Razorpay) {
    return Promise.resolve(true);
  }
  if (scriptPromise) {
    return scriptPromise;
  }
  scriptPromise = new Promise((resolve) => {
    const script = document.createElement('script');
    script.src = RAZORPAY_SCRIPT_SRC;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
  return scriptPromise;
}

export interface RazorpayCheckoutOptions {
  keyId: string;
  orderId: string;
  amountInPaise: number;
  currency: string;
  name?: string;
  description?: string;
  prefillName?: string;
  prefillEmail?: string;
  prefillContact?: string;
  themeColor?: string;
}

export interface RazorpaySuccessResponse {
  razorpay_order_id: string;
  razorpay_payment_id: string;
  razorpay_signature: string;
}

/**
 * Opens the Razorpay hosted Checkout widget for the given order and resolves with the
 * signed payment confirmation once the customer completes payment, or rejects if the
 * widget is dismissed/cancelled or the script fails to load.
 */
export async function openRazorpayCheckout(
  options: RazorpayCheckoutOptions
): Promise<RazorpaySuccessResponse> {
  const loaded = await loadRazorpayScript();
  if (!loaded) {
    throw new Error('Unable to load the payment gateway. Please check your internet connection and try again.');
  }

  return new Promise((resolve, reject) => {
    const razorpay = new (window as any).Razorpay({
      key: options.keyId,
      order_id: options.orderId,
      amount: options.amountInPaise,
      currency: options.currency || 'INR',
      name: options.name || 'Cafe Management System',
      description: options.description || 'Order Payment',
      theme: { color: options.themeColor || '#e23744' },
      prefill: {
        name: options.prefillName || '',
        email: options.prefillEmail || '',
        contact: options.prefillContact || '',
      },
      // Covers UPI, Cards, Net Banking and Wallets in a single hosted widget.
      handler: (response: RazorpaySuccessResponse) => resolve(response),
      modal: {
        ondismiss: () => reject(new Error('Payment cancelled')),
      },
    });
    razorpay.on('payment.failed', (response: any) => {
      reject(new Error(response?.error?.description || 'Payment failed'));
    });
    razorpay.open();
  });
}
