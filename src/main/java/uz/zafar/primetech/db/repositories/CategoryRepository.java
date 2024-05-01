package uz.zafar.primetech.db.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.zafar.primetech.db.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query(value = """
            select * from categories 
            where name_uz=?1 or name_ru=?1 or name_en=?1
            """, nativeQuery = true)
    Category findByName(String name);

}
