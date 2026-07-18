import { Navigate, Route, Routes } from 'react-router-dom';
import Home from './pages/Home';
import FullLayout from './layout/FullLayout';
import Dashboard from './pages/Dashboard';
import ManageCategory from './pages/ManageCategory';
import ManageProduct from './pages/ManageProduct';
import ManageOrder from './pages/ManageOrder';
import ViewBill from './pages/ViewBill';
import ManageUser from './pages/ManageUser';
import Menu from './pages/Menu';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import ManageCoupon from './pages/ManageCoupon';
import KitchenDashboard from './pages/KitchenDashboard';
import ManageDelivery from './pages/ManageDelivery';
import DeliveryDashboard from './pages/DeliveryDashboard';
import StoreLocator from './pages/StoreLocator';
import ManageStores from './pages/ManageStores';
import RouteGuard from './components/RouteGuard';
import { getHomePathForRole, getUserRole } from './auth/auth';

function IndexRedirect() {
  return <Navigate to={getHomePathForRole(getUserRole())} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/cafe" element={<FullLayout />}>
        <Route index element={<IndexRedirect />} />
        <Route
          path="dashboard"
          element={
            <RouteGuard expectedRole={['admin', 'user']}>
              <Dashboard />
            </RouteGuard>
          }
        />
        <Route
          path="category"
          element={
            <RouteGuard expectedRole={['admin', 'user']}>
              <ManageCategory />
            </RouteGuard>
          }
        />
        <Route
          path="product"
          element={
            <RouteGuard expectedRole={['admin']}>
              <ManageProduct />
            </RouteGuard>
          }
        />
        <Route
          path="order"
          element={
            <RouteGuard expectedRole={['admin', 'user']}>
              <ManageOrder />
            </RouteGuard>
          }
        />
        <Route
          path="menu"
          element={
            <RouteGuard expectedRole={['admin', 'user']}>
              <Menu />
            </RouteGuard>
          }
        />
        <Route
          path="cart"
          element={
            <RouteGuard expectedRole={['admin', 'user']}>
              <CartPage />
            </RouteGuard>
          }
        />
        <Route
          path="checkout"
          element={
            <RouteGuard expectedRole={['admin', 'user']}>
              <CheckoutPage />
            </RouteGuard>
          }
        />
        <Route
          path="coupon"
          element={
            <RouteGuard expectedRole={['admin']}>
              <ManageCoupon />
            </RouteGuard>
          }
        />
        <Route
          path="kitchen"
          element={
            <RouteGuard expectedRole={['admin']}>
              <KitchenDashboard />
            </RouteGuard>
          }
        />
        <Route
          path="delivery-management"
          element={
            <RouteGuard expectedRole={['admin']}>
              <ManageDelivery />
            </RouteGuard>
          }
        />
        <Route
          path="delivery"
          element={
            <RouteGuard expectedRole={['delivery']}>
              <DeliveryDashboard />
            </RouteGuard>
          }
        />
        <Route
          path="stores"
          element={
            <RouteGuard expectedRole={['admin', 'user']}>
              <StoreLocator />
            </RouteGuard>
          }
        />
        <Route
          path="manage-stores"
          element={
            <RouteGuard expectedRole={['admin']}>
              <ManageStores />
            </RouteGuard>
          }
        />
        <Route
          path="bill"
          element={
            <RouteGuard expectedRole={['admin', 'user']}>
              <ViewBill />
            </RouteGuard>
          }
        />
        <Route
          path="user"
          element={
            <RouteGuard expectedRole={['admin']}>
              <ManageUser />
            </RouteGuard>
          }
        />
      </Route>
      <Route path="*" element={<Home />} />
    </Routes>
  );
}
