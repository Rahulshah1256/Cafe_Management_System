export interface Menu {
  state: string;
  name: string;
  type: string;
  icon: string;
  role: string;
}

export const MENUITEMS: Menu[] = [
  { state: 'dashboard', name: 'Dashboard', type: 'link', icon: 'dashboard', role: '' },
  { state: 'menu', name: 'Menu', type: 'link', icon: 'restaurant_menu', role: '' },
  { state: 'cart', name: 'My Cart', type: 'link', icon: 'shopping_cart', role: '' },
  { state: 'category', name: 'Manage Category', type: 'link', icon: 'category', role: 'admin' },
  { state: 'product', name: 'Manage Product', type: 'link', icon: 'inventory_2', role: 'admin' },
  { state: 'coupon', name: 'Manage Coupons', type: 'link', icon: 'local_offer', role: 'admin' },
  { state: 'order', name: 'Manage Order', type: 'link', icon: 'point_of_sale', role: '' },
  { state: 'kitchen', name: 'Kitchen Dashboard', type: 'link', icon: 'soup_kitchen', role: 'admin' },
  { state: 'delivery-management', name: 'Manage Delivery', type: 'link', icon: 'delivery_dining', role: 'admin' },
  { state: 'delivery', name: 'My Deliveries', type: 'link', icon: 'two_wheeler', role: 'delivery' },
  { state: 'stores', name: 'Store Locator', type: 'link', icon: 'place', role: '' },
  { state: 'manage-stores', name: 'Manage Stores', type: 'link', icon: 'store', role: 'admin' },
  { state: 'bill', name: 'View Bill', type: 'link', icon: 'backup_table', role: '' },
  { state: 'user', name: 'Manage User', type: 'link', icon: 'people', role: 'admin' },
];
