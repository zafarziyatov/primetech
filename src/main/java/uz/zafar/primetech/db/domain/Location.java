package uz.zafar.primetech.db.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "locations")
@Getter
@Setter

public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    private String fullAddress ;
    private Double latitude;
    private Double longitude ;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
