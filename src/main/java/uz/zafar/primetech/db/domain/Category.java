package uz.zafar.primetech.db.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Entity

@Table(name = "categories")
public class Category {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter
    @Setter
    @Column(unique = true)
    private String nameUz;
    @Getter
    @Setter
    @Column(unique = true)
    private String nameEn;
    @Getter
    @Setter
    @Column(unique = true)
    private String nameRu;

    @Setter
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Product> products;
    @Getter
    @Setter
    private String status;
    @Getter
    @Setter
    private Boolean success;
/*
    @ManyToOne
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;
*/

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", nameUz='" + nameUz + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", nameRu='" + nameRu + '\'' +
                ", types=" + products +
                '}';
    }

    public List<Product> getProducts() {
        products.sort(Comparator.comparingLong(Product::getId));
        return products;
    }
}
