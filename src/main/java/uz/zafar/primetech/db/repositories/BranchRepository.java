package uz.zafar.primetech.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.zafar.primetech.db.domain.Branch;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Branch findByName(String name);
}
