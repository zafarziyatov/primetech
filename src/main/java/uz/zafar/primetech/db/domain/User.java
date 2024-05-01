package uz.zafar.primetech.db.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Data
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true)
    private Long chatId;

    private String helper;

    private String username;


    private String nickname;

    private String phone;

    private String eventCode;

    private Integer level;

    private String role;

    private LocalDate day;

    private String lang;

    private Integer page;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Location> locations;

    private String currentLocation;

    private Double currentLat;

    private Double currentLon;

    private Long helperCategoryId;

    private Long helperProductId;

    private Integer countImg;

    private String helperImgName;

    private Double helperPrice;

    private String helperType;

    private Integer countProduct ;

    private Double helperProductPrice;
    private String paymentType;

    @ManyToMany(mappedBy = "users",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Basket> baskets ;
    private String eventCode2;
    private Long count ;
}

