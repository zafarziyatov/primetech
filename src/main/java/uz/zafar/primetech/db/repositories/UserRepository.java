package uz.zafar.primetech.db.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.zafar.primetech.db.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByChatId(Long chatId);
    @Query(value = "SELECT * FROM users WHERE LOWER(role) LIKE LOWER(CONCAT('%', :role, '%'))", nativeQuery = true)
    Page<User> findAllByRole(@Param("role") String role , Pageable pageable);

    @Query(value = """
            SELECT * from users where role=?1
            """,nativeQuery=true)
    List<User> findAllByRole(String role);

    @Query(value = """
            SELECT * from users order by id
            """,nativeQuery=true)
    Page<User> findAll(Pageable pageable);
    List<User> findAll();
    @Query(value = "SELECT * FROM users WHERE LOWER(username) LIKE LOWER(CONCAT('%', :username, '%'))", nativeQuery = true)
    Page<User>findAllByUsername(@Param("username") String username, Pageable pageable);
    @Query(value = "SELECT * FROM users WHERE LOWER(nickname) LIKE LOWER(CONCAT('%', :nickname, '%'))", nativeQuery = true)
    Page<User>findAllByNickname(@Param("nickname") String nickname, Pageable pageable);
}
