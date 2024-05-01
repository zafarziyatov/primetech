package uz.zafar.primetech.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.zafar.primetech.db.domain.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying
    @Query(value = "DELETE FROM products WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") Long id);
}
