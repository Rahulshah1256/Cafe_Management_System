const items = [
  {
    img: '/assets/img/1.jpg',
    title: 'Pizza',
    body:
      'Pizza is an Italian dish consisting of a usually round, flattened base of leavened wheat-based dough topped with tomatoes, cheese, and often various other ingredients, which is then baked at a high temperature, traditionally in a wood-fired oven. A small pizza is sometimes called a pizzetta.',
  },
  {
    img: '/assets/img/2.jpg',
    title: 'Biryani',
    body:
      'Biryani is a mixed rice dish. It is made with Indian spices, rice, and meat usually that of chicken, fish, and sometimes, in addition, eggs or vegetables such as potatoes in certain regional varieties.',
  },
  {
    img: '/assets/img/3.jpg',
    title: 'Pasta',
    body:
      'Pasta is a type of food typically made from an unleavened dough of wheat flour mixed with water or eggs, and formed into sheets or other shapes, then cooked by boiling or baking.',
  },
  {
    img: '/assets/img/4.jpg',
    title: 'Molten chocolate cake',
    body:
      "Molten chocolate cake is a popular dessert that combines the elements of a chocolate cake and a soufflé. Its name derives from the dessert's liquid chocolate center, and it is also known as chocolate moelleux, chocolate lava cake, or simply lava cake.",
  },
];

export default function BestSeller() {
  return (
    <div className="best-seller">
      <div className="container">
        <div className="text-center">
          <h2 className="section-heading">Best Sellers</h2>
          <p className="section-subheading">Crowd favorites, freshly made every day</p>
        </div>
        <ul className="food-grid">
          {items.map((item) => (
            <li className="food-card" key={item.title}>
              <div className="food-card-image">
                <img src={item.img} alt={item.title} />
              </div>
              <div className="food-card-body">
                <h4>{item.title}</h4>
                <p>{item.body}</p>
              </div>
            </li>
          ))}
          <li className="food-card-cta" onClick={() => window.location.assign('#signup')}>
            Be Part
            <br />
            Of Our
            <br />
            Cafe!
          </li>
        </ul>
      </div>
    </div>
  );
}
