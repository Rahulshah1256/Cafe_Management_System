import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Dialog, Icon, Button, Chip } from '@mui/material';
import DeliveryDiningIcon from '@mui/icons-material/DeliveryDining';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import RoomIcon from '@mui/icons-material/Room';
import BoltIcon from '@mui/icons-material/Bolt';
import BestSeller from '../components/BestSeller';
import LoginDialog from '../components/dialogs/LoginDialog';
import SignupDialog from '../components/dialogs/SignupDialog';
import { UserService } from '../services/user.service';

type DialogType = 'login' | 'signup' | null;

const FEATURES = [
  {
    icon: <BoltIcon />,
    title: 'Lightning Fast Delivery',
    body: 'Track your order in real time from kitchen to doorstep.',
  },
  {
    icon: <LocalOfferIcon />,
    title: 'Loyalty Rewards',
    body: 'Earn points on every order and redeem them for discounts.',
  },
  {
    icon: <RoomIcon />,
    title: 'Store Locator',
    body: 'Find the nearest outlet and get directions instantly.',
  },
  {
    icon: <DeliveryDiningIcon />,
    title: 'Live Order Tracking',
    body: 'Know exactly when your delivery partner will arrive.',
  },
];

export default function Home() {
  const navigate = useNavigate();
  const [openDialog, setOpenDialog] = useState<DialogType>(null);

  useEffect(() => {
    UserService.checkToken()
      .then(() => {
        navigate('/cafe/dashboard');
      })
      .catch((error: any) => {
        console.log(error);
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const close = () => setOpenDialog(null);

  return (
    <>
      <div className="home-wrapper sticky">
        <nav>
          <a href="#" className="logo">
            <Icon>storefront</Icon>&nbsp;Cafe Management System
          </a>
          <ul>
            <li>
              <a onClick={() => setOpenDialog('login')}>Login</a>
              <a className="home-nav-btn" onClick={() => setOpenDialog('signup')}>
                Sign Up
              </a>
            </li>
          </ul>
        </nav>
      </div>

      <section className="home-hero">
        <div className="home-hero-content">
          <div className="home-hero-text">
            <span className="home-hero-badge">🔥 Now delivering across your city</span>
            <h1 className="home-hero-title">
              Delicious food,
              <br />
              delivered fast.
            </h1>
            <p className="home-hero-subtitle">
              Order from our curated menu of pizzas, biryanis, pastas &amp; desserts. Real-time
              tracking, loyalty rewards, and unbeatable taste - all in one place.
            </p>
            <div className="home-hero-actions">
              <Button
                variant="contained"
                size="large"
                color="inherit"
                sx={{ bgcolor: '#fff', color: '#E5202A', '&:hover': { bgcolor: '#fff' } }}
                onClick={() => setOpenDialog('signup')}
              >
                Order Now
              </Button>
              <Button
                variant="outlined"
                size="large"
                sx={{ color: '#fff', borderColor: 'rgba(255,255,255,0.7)' }}
                onClick={() => setOpenDialog('login')}
              >
                I already have an account
              </Button>
            </div>
          </div>
          <div className="home-hero-visual">
            <div className="home-hero-card">
              <img
                src="/assets/img/food1.jpg"
                alt="Signature dish"
                style={{ width: '100%', borderRadius: 16, display: 'block', marginBottom: 14 }}
              />
              <Chip label="⭐ 4.8 rated by 2,000+ foodies" color="primary" sx={{ mb: 1 }} />
              <p style={{ margin: 0, color: '#2A1F1A', fontWeight: 600 }}>
                Chef's Special Margherita Pizza
              </p>
            </div>
          </div>
        </div>
      </section>

      <div className="home-features">
        {FEATURES.map((feature) => (
          <div className="home-feature-card" key={feature.title}>
            <div className="home-feature-icon">{feature.icon}</div>
            <div>
              <h4 style={{ margin: '0 0 6px', fontFamily: 'Poppins, sans-serif' }}>
                {feature.title}
              </h4>
              <p style={{ margin: 0, color: '#7A6E66', fontSize: '0.9rem' }}>{feature.body}</p>
            </div>
          </div>
        ))}
      </div>

      <BestSeller />

      <div className="home-footer" id="signup">
        <Icon sx={{ verticalAlign: 'middle', mr: 1 }}>storefront</Icon>
        Cafe Management System &copy; {new Date().getFullYear()} - Crafted with ❤️ for food lovers.
      </div>

      <Dialog open={openDialog === 'login'} onClose={close} fullWidth maxWidth="sm">
        <LoginDialog onClose={close} />
      </Dialog>
      <Dialog open={openDialog === 'signup'} onClose={close} fullWidth maxWidth="sm">
        <SignupDialog onClose={close} />
      </Dialog>
    </>
  );
}
