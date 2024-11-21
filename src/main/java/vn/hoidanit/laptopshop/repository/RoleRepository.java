package vn.hoidanit.laptopshop.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.Role;

// crud: create,read,update,delete
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);

}
