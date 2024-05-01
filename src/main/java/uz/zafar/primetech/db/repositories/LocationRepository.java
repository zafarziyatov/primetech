package uz.zafar.primetech.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.zafar.primetech.db.domain.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = "select * from locations where user_id=?1", nativeQuery = true)
    List<Location> findAllByUserId(Long userId);
}
