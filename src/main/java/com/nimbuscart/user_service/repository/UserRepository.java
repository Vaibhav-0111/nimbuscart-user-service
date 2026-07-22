package com.nimbuscart.user_service.repository;
import com.nimbuscart.user_service.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {

}