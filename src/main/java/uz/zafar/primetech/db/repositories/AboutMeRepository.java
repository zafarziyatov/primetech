package uz.zafar.primetech.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.zafar.primetech.db.domain.AboutMe;

public interface AboutMeRepository extends JpaRepository<AboutMe,Integer> {
}
