package com.nimbuscart.user_service.repository;
import com.nimbuscart.user_service.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User > findByEmail(String email);
}