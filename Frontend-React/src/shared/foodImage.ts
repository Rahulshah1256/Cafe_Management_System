// Resolves a polished, relevant food photo for a menu item.
// Priority: exact dish-name match -> category fallback -> the item's own
// imageUrl (e.g. a custom photo an admin uploaded) -> generic fallback photo.

const NAME_IMAGES: Record<string, string> = {
  'margherita pizza': '/assets/img/margherita.jpg',
  'farmhouse pizza': '/assets/img/1.jpg',
  'masala chai': '/assets/img/masala-chai.jpg',
  'cold coffee': '/assets/img/cold-coffee.jpg',
  'chicken biryani': '/assets/img/2.jpg',
  'veg biryani': '/assets/img/2.jpg',
  'gulab jamun': '/assets/img/4.jpg',
  'chocolate lava cake': '/assets/img/4.jpg',
};

const CATEGORY_IMAGES: Record<string, string> = {
  pizza: '/assets/img/1.jpg',
  biryani: '/assets/img/2.jpg',
  beverages: '/assets/img/cold-coffee.jpg',
  desserts: '/assets/img/4.jpg',
};

export const DEFAULT_FOOD_IMAGE = '/assets/img/food1.jpg';

export function getFoodImage(item: { name?: string; productName?: string; categoryName?: string; imageUrl?: string }): string {
  const name = (item.name || item.productName || '').trim().toLowerCase();
  if (name && NAME_IMAGES[name]) {
    return NAME_IMAGES[name];
  }
  const category = (item.categoryName || '').trim().toLowerCase();
  if (category && CATEGORY_IMAGES[category]) {
    return CATEGORY_IMAGES[category];
  }
  if (item.imageUrl) {
    return item.imageUrl;
  }
  return DEFAULT_FOOD_IMAGE;
}
