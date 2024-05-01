package uz.zafar.primetech.db.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private String address;
    private String location;
    private String phone;
    private String landmark;//orientr
    private Integer begin;
    private Integer last;
    private Double lat;
    private Double lon;
}
