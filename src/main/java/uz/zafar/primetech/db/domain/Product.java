package uz.zafar.primetech.db.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String nameUz;
    @Column(unique = true)
    private String nameEn;
    @Column(unique = true)
    private String nameRu;
    @ManyToOne
    @JoinColumn(
            name = "category_id",
            nullable = false
    )
    private Category category;
    private Double price;
    private String captionUz;
    private String captionEn;
    private String captionRu;

}
