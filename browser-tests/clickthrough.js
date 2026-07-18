// Full browser click-through test for the Cafe Management System React app.
// Runs headless Chromium via Playwright, exercising admin / user / delivery-partner
// flows end-to-end against the live dev servers (frontend :4200, backend :8081).
// Each step is independently try/caught so one failure doesn't abort the rest of the run.
const { chromium } = require('playwright');
const path = require('path');

const BASE = 'http://localhost:4200';
const SHOT_DIR = path.join(__dirname, 'screenshots');
const results = [];

function record(name, ok, detail) {
  results.push({ name, ok, detail: detail || '' });
  console.log(`${ok ? 'PASS' : 'FAIL'} - ${name}${detail ? ' :: ' + detail : ''}`);
}

async function step(name, fn) {
  try {
    await fn();
  } catch (e) {
    record(name, false, e.message.split('\n')[0]);
  }
}

async function shot(page, name) {
  await page.screenshot({ path: path.join(SHOT_DIR, name), fullPage: true }).catch(() => {});
}

async function login(page, email, password) {
  await page.goto(BASE + '/', { waitUntil: 'networkidle' });
  await page.getByText('Login', { exact: true }).first().click();
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Login' }).click();
  await page.waitForTimeout(1500);
}

async function logout(page) {
  await page.locator('button:has(svg[data-testid="AccountCircleIcon"])').click();
  await page.getByText('Logout', { exact: true }).click();
  await page.waitForTimeout(300);
  await page.getByRole('button', { name: /yes|confirm|ok/i }).click();
  await page.waitForTimeout(1000);
}

(async () => {
  const browser = await chromium.launch({ headless: true });
  const consoleErrors = [];

  // ---------------- ADMIN FLOW ----------------
  let context = await browser.newContext();
  let page = await context.newPage();
  page.on('console', (msg) => {
    if (msg.type() === 'error') consoleErrors.push(`[admin] ${msg.text()}`);
  });
  page.on('pageerror', (err) => consoleErrors.push(`[admin:pageerror] ${err.message}`));

  await step('Admin login redirects to dashboard', async () => {
    await login(page, 'admin@cafe.com', 'admin@123');
    await page.waitForURL('**/cafe/dashboard', { timeout: 10000 });
    record('Admin login redirects to dashboard', true);
    await shot(page, '01-admin-dashboard.png');
  });

  await step('Admin dashboard shows Sales Analytics section', async () => {
    const salesVisible = await page.getByText('Sales Analytics', { exact: false }).count();
    record('Admin dashboard shows Sales Analytics section', salesVisible > 0);
  });

  await step('Manage Category: add category', async () => {
    await page.goto(BASE + '/cafe/category', { waitUntil: 'networkidle' });
    const catAddBtn = page.getByRole('button', { name: /add category/i });
    if (await catAddBtn.count()) {
      await catAddBtn.click();
      await page.getByLabel(/^name/i).fill('Browser Test Category');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);
      const catRow = await page.getByText('Browser Test Category').count();
      record('Manage Category: add category', catRow > 0);
    } else {
      record('Manage Category: add category', false, 'add button not found');
    }
    await shot(page, '02-admin-category.png');
  });

  await step('Manage Product: add product', async () => {
    await page.goto(BASE + '/cafe/product', { waitUntil: 'networkidle' });
    const prodAddBtn = page.getByRole('button', { name: /add product/i });
    if (await prodAddBtn.count()) {
      await prodAddBtn.click();
      await page.waitForTimeout(300);
      await page.getByLabel(/^name/i).fill('Browser Test Snack');
      await page.getByLabel(/description/i).fill('Created by automated browser test');
      await page.getByLabel(/price/i).fill('99');
      await page.waitForTimeout(200);
      const categorySelect = page.getByLabel(/^category$/i);
      if (await categorySelect.count()) {
        await categorySelect.click();
        await page.getByRole('option').first().click().catch(() => {});
      }
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);
      const prodRow = await page.getByText('Browser Test Snack').count();
      record('Manage Product: add product', prodRow > 0);
    } else {
      record('Manage Product: add product', false, 'add button not found');
    }
    await shot(page, '03-admin-product.png');
  });

  await step('Manage Coupon: add coupon', async () => {
    await page.goto(BASE + '/cafe/coupon', { waitUntil: 'networkidle' });
    const couponAddBtn = page.getByRole('button', { name: /add coupon/i });
    if (await couponAddBtn.count()) {
      await couponAddBtn.click();
      await page.getByLabel(/coupon code/i).fill('BROWSERTEST10');
      await page.getByLabel(/discount value/i).fill('10');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);
      const couponRow = await page.getByText('BROWSERTEST10').count();
      record('Manage Coupon: add coupon', couponRow > 0);
    } else {
      record('Manage Coupon: add coupon', false, 'add button not found');
    }
    await shot(page, '04-admin-coupon.png');
  });

  await step('Manage Order page loads', async () => {
    await page.goto(BASE + '/cafe/order', { waitUntil: 'networkidle' });
    record('Manage Order page loads', page.url().includes('/cafe/order'));
    await shot(page, '05-admin-order.png');
  });

  await step('Kitchen Dashboard loads', async () => {
    await page.goto(BASE + '/cafe/kitchen', { waitUntil: 'networkidle' });
    const kitchenHeading = await page.getByText(/kitchen/i).count();
    record('Kitchen Dashboard loads', kitchenHeading > 0);
    await shot(page, '06-admin-kitchen.png');
  });

  await step('Manage Delivery shows registered rider', async () => {
    await page.goto(BASE + '/cafe/delivery-management', { waitUntil: 'networkidle' });
    const riderRow = await page.getByText('browsertest.rider@cafe.com').count();
    record('Manage Delivery shows registered rider', riderRow > 0);
    await shot(page, '07-admin-delivery.png');
  });

  await step('Manage Stores: add store', async () => {
    await page.goto(BASE + '/cafe/manage-stores', { waitUntil: 'networkidle' });
    const storeAddBtn = page.getByRole('button', { name: /add store/i });
    if (await storeAddBtn.count()) {
      await storeAddBtn.click();
      await page.getByLabel(/store name/i).fill('Browser Test Outlet');
      await page.getByLabel(/address line 1/i).fill('Test Street 1');
      await page.getByLabel(/^city/i).fill('TestCity');
      await page.getByLabel(/^state/i).fill('TestState');
      await page.getByLabel(/pincode/i).fill('123456');
      await page.getByRole('button', { name: /^add$/i }).click();
      await page.waitForTimeout(1000);
      const newStoreRow = await page.getByText('Browser Test Outlet').count();
      record('Manage Stores: add store', newStoreRow > 0);
    } else {
      record('Manage Stores: add store', false, 'add button not found');
    }
    await shot(page, '08-admin-stores.png');
  });

  await step('Manage Stores: edit store (PUT request over real CORS)', async () => {
    const editBtn = page.locator('button:has(svg[data-testid="EditIcon"])').first();
    if (await editBtn.count()) {
      await editBtn.click();
      await page.waitForTimeout(300);
      const nameField = page.getByLabel(/store name/i);
      await nameField.fill('Browser Test Outlet Updated');
      await page.getByRole('button', { name: /update/i }).click();
      await page.waitForTimeout(1000);
      const updatedRow = await page.getByText('Browser Test Outlet Updated').count();
      record('Manage Stores: edit store (PUT request over real CORS)', updatedRow > 0);
    } else {
      record('Manage Stores: edit store (PUT request over real CORS)', false, 'edit button not found');
    }
  });

  await step('Manage User shows existing users', async () => {
    await page.goto(BASE + '/cafe/user', { waitUntil: 'networkidle' });
    const userRow = await page.getByText('user@cafe.com').count();
    record('Manage User shows existing users', userRow > 0);
    await shot(page, '09-admin-users.png');
  });

  await step('Admin logout returns to home', async () => {
    await logout(page);
    record('Admin logout returns to home', page.url() === BASE + '/' || page.url() === BASE);
  });
  await context.close();

  // ---------------- REGULAR USER FLOW ----------------
  context = await browser.newContext();
  page = await context.newPage();
  page.on('console', (msg) => {
    if (msg.type() === 'error') consoleErrors.push(`[user] ${msg.text()}`);
  });
  page.on('pageerror', (err) => consoleErrors.push(`[user:pageerror] ${err.message}`));

  await step('User login redirects to dashboard', async () => {
    await login(page, 'user@cafe.com', 'user@123');
    await page.waitForURL('**/cafe/dashboard', { timeout: 10000 });
    record('User login redirects to dashboard', true);
    await shot(page, '10-user-dashboard.png');
  });

  await step('Menu: recommendations + voice search + add to cart', async () => {
    await page.goto(BASE + '/cafe/menu', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1500);
    const recommendedVisible = await page.getByText('Recommended for You').count();
    record('Menu shows Recommended for You section', recommendedVisible > 0);
    const micIcon = await page.locator('svg[data-testid="MicNoneIcon"], svg[data-testid="MicIcon"]').count();
    record('Menu shows voice-search mic icon', micIcon > 0);
    await shot(page, '11-user-menu.png');

    const addButtons = page.getByRole('button', { name: /^add$/i });
    const addCount = await addButtons.count();
    if (addCount > 0) {
      await addButtons.first().click();
      await page.waitForTimeout(800);
      record('Menu: add item to cart', true);
    } else {
      record('Menu: add item to cart', false, 'no Add buttons found (all items may already be in cart)');
    }
  });

  await step('Cart page shows subtotal/items', async () => {
    await page.goto(BASE + '/cafe/cart', { waitUntil: 'networkidle' });
    const cartHasItems = await page.getByText(/subtotal/i).count();
    record('Cart page shows subtotal/items', cartHasItems > 0);
    await shot(page, '12-user-cart.png');
  });

  await step('Cart: proceed to checkout navigates', async () => {
    const checkoutBtn = page.getByRole('button', { name: /checkout|proceed/i });
    if (await checkoutBtn.count()) {
      await checkoutBtn.first().click();
      await page.waitForTimeout(1000);
      record('Cart: proceed to checkout navigates', page.url().includes('/cafe/checkout'));
      await shot(page, '13-user-checkout.png');
    } else {
      record('Cart: proceed to checkout navigates', false, 'checkout button not found (cart may be empty)');
    }
  });

  await step('Loyalty points widget on checkout (informational)', async () => {
    await page.waitForTimeout(500);
    const loyaltyVisible = await page.getByText(/loyalty/i).count();
    console.log(`INFO - Loyalty points widget visible on checkout: ${loyaltyVisible > 0} (only shown when balance > 0)`);
  });

  await step('View Bill shows past orders', async () => {
    await page.goto(BASE + '/cafe/bill', { waitUntil: 'networkidle' });
    await page.waitForTimeout(800);
    const billRows = await page.locator('table tbody tr').count();
    record('View Bill shows past orders', billRows > 0);
    await shot(page, '14-user-bill.png');
  });

  await step('Notification bell opens dropdown', async () => {
    await page.goto(BASE + '/cafe/dashboard', { waitUntil: 'networkidle' });
    const bell = page.locator(
      'button:has(svg[data-testid="NotificationsIcon"]), button:has(svg[data-testid="NotificationsNoneIcon"])'
    );
    if (await bell.count()) {
      await bell.first().click();
      await page.waitForTimeout(500);
      record('Notification bell opens dropdown', true);
      await shot(page, '15-user-notifications.png');
      await page.keyboard.press('Escape');
    } else {
      record('Notification bell opens dropdown', false, 'bell icon not found');
    }
  });

  await step('Store Locator shows stores with Get Directions', async () => {
    await page.goto(BASE + '/cafe/stores', { waitUntil: 'networkidle' });
    await page.waitForTimeout(500);
    const directionsBtn = await page.getByText(/get directions/i).count();
    record('Store Locator shows stores with Get Directions', directionsBtn > 0);
    await shot(page, '16-user-stores.png');
  });

  await step('User logout returns to home', async () => {
    await logout(page);
    record('User logout returns to home', page.url() === BASE + '/' || page.url() === BASE);
  });
  await context.close();

  // ---------------- DELIVERY PARTNER FLOW ----------------
  context = await browser.newContext();
  page = await context.newPage();
  page.on('console', (msg) => {
    if (msg.type() === 'error') consoleErrors.push(`[delivery] ${msg.text()}`);
  });
  page.on('pageerror', (err) => consoleErrors.push(`[delivery:pageerror] ${err.message}`));

  await step('Delivery login lands on /cafe/delivery (no infinite redirect loop)', async () => {
    await login(page, 'browsertest.rider@cafe.com', 'rider123');
    await page.waitForTimeout(1500);
    // Login always navigates to /cafe/dashboard first; RouteGuard should then bounce
    // a delivery-role account to /cafe/delivery without an infinite loop.
    record(
      'Delivery login eventually lands on /cafe/delivery (no infinite redirect loop)',
      page.url().includes('/cafe/delivery')
    );
    await shot(page, '20-delivery-dashboard.png');
  });

  await step('Delivery Dashboard shows assigned order', async () => {
    const assignedOrderVisible = await page.getByText('BILL1784366843953').count();
    record('Delivery Dashboard shows assigned order', assignedOrderVisible > 0);
  });

  await step('Delivery Dashboard: mark as delivered (order leaves the queue)', async () => {
    const deliveredBtn = page.getByRole('button', { name: /mark as delivered/i });
    if (await deliveredBtn.count()) {
      await deliveredBtn.first().click();
      await page.waitForTimeout(1500);
      const stillListed = await page.getByText('BILL1784366843953').count();
      record('Delivery Dashboard: mark as delivered (order leaves the queue)', stillListed === 0);
    } else {
      record('Delivery Dashboard: mark as delivered (order leaves the queue)', false, 'button not found');
    }
    await shot(page, '21-delivery-completed.png');
  });

  await step('RouteGuard redirects delivery role away from /cafe/dashboard without logging out', async () => {
    await page.goto(BASE + '/cafe/dashboard', { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);
    const stillHasToken = await page.evaluate(() => localStorage.getItem('token'));
    record(
      'RouteGuard redirects delivery role away from /cafe/dashboard without logging out',
      page.url().includes('/cafe/delivery') && !!stillHasToken
    );
    await shot(page, '22-delivery-routeguard-check.png');
  });

  await step('Delivery availability can be updated (PUT request over real CORS)', async () => {
    await page.goto(BASE + '/cafe/delivery', { waitUntil: 'networkidle' });
    const availSelect = page.getByLabel(/availability/i);
    if (await availSelect.count()) {
      await availSelect.click();
      const offlineOption = page.getByRole('option', { name: /offline/i });
      if (await offlineOption.count()) {
        await offlineOption.click();
        await page.waitForTimeout(800);
        record('Delivery availability can be updated (PUT request over real CORS)', true);
      } else {
        await page.keyboard.press('Escape');
        record('Delivery availability can be updated (PUT request over real CORS)', false, 'offline option not found');
      }
    } else {
      record('Delivery availability can be updated (PUT request over real CORS)', false, 'availability control not found');
    }
  });

  await step('Delivery logout returns to home', async () => {
    await logout(page);
    record('Delivery logout returns to home', page.url() === BASE + '/' || page.url() === BASE);
  });
  await context.close();

  await browser.close();

  console.log('\n=== SUMMARY ===');
  const passed = results.filter((r) => r.ok).length;
  console.log(`${passed}/${results.length} checks passed`);
  const failed = results.filter((r) => !r.ok);
  if (failed.length) {
    console.log('\nFAILURES:');
    failed.forEach((f) => console.log(` - ${f.name}: ${f.detail}`));
  }
  if (consoleErrors.length) {
    console.log('\nBROWSER CONSOLE ERRORS:');
    consoleErrors.forEach((e) => console.log(' - ' + e));
  } else {
    console.log('\nNo browser console errors captured.');
  }

  process.exit(failed.length > 0 ? 1 : 0);
})();
