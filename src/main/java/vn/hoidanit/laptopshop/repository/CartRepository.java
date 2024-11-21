package vn.hoidanit.laptopshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.User;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart save(Cart cart);

    Cart findByUser(User user);

    Cart deleteById(long id);

}
