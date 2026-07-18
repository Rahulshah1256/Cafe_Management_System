package com.inn.cafe.POJO;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import java.io.Serializable;

@NamedQuery(name = "Product.getAllProduct", query = "select new com.inn.cafe.wrapper.ProductWrapper(u.id, u.name, u.description, u.price, u.category.id, u.category.name, u.status, u.isVeg, u.spicyLevel, u.bestSeller, u.newArrival, u.rating, u.ratingCount, u.imageUrl, u.prepTimeMinutes) from Product u")

@NamedQuery(name = "Product.updateProductStatus" , query = "update Product u set u.status =:status where u.id =:id")

@NamedQuery(name = "Product.getByCategory", query = "select new com.inn.cafe.wrapper.ProductWrapper(u.id, u.name, u.description, u.price, u.category.id, u.category.name, u.status, u.isVeg, u.spicyLevel, u.bestSeller, u.newArrival, u.rating, u.ratingCount, u.imageUrl, u.prepTimeMinutes) from Product u where u.category.id=:id and u.status='true'")

@NamedQuery(name = "Product.getProductById", query = "select new com.inn.cafe.wrapper.ProductWrapper(u.id, u.name, u.description, u.price, u.category.id, u.category.name, u.status, u.isVeg, u.spicyLevel, u.bestSeller, u.newArrival, u.rating, u.ratingCount, u.imageUrl, u.prepTimeMinutes) from Product u where u.id=:id")

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "product")
public class Product implements Serializable {
    private static final long serialVersionUID = 123456L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_fk", nullable = false)
    private Category category;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Integer price;

    @Column(name = "status")
    private String status;

    @Column(name = "is_veg")
    private Boolean isVeg = Boolean.TRUE;

    @Column(name = "spicy_level")
    private String spicyLevel = "NONE";

    @Column(name = "best_seller")
    private Boolean bestSeller = Boolean.FALSE;

    @Column(name = "new_arrival")
    private Boolean newArrival = Boolean.FALSE;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;


    public Product() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }


    public String getstatus() {
        return status;
    }

    public void setstatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                '}';
    }
}
