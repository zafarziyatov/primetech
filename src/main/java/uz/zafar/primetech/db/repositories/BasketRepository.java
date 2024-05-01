package uz.zafar.primetech.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.zafar.primetech.db.domain.Basket;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {
}
