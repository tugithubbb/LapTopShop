package vn.hoidanit.laptopshop.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.User;
import java.util.List;

// crud: create,read,update,delete
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

    void deleteById(long id);

    // List<User> findByEmail(String email);

    User findById(long id);

    List<User> findOneByEmail(String email);

    boolean existsByEmail(String email);

    User findByEmail(String email);

}
