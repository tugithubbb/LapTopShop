package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ProductCriteriaDTO;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailRepository cartDetailRepository, UserService userService, OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Product handleSaveProduct(Product product) {
        return this.productRepository.save(product);
    }

    private Specification<Product> nameLike(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.NAME), "%" + name + "%");
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return this.productRepository.findAll(pageable);

    }

    public Page<Product> getAllProductsWithSpec(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {

        if (productCriteriaDTO.getTarget() == null && productCriteriaDTO.getFactory() == null
                && productCriteriaDTO.getPrice() == null) {
            return this.productRepository.findAll(pageable);
        }

        Specification<Product> combinedSpec = Specification.where(null);
        if (productCriteriaDTO.getTarget() != null && productCriteriaDTO.getTarget().isPresent()) {
            Specification currentSpec = ProductSpecs.matchListTarget(productCriteriaDTO.getTarget().get());

            combinedSpec = combinedSpec.and(currentSpec);
        }
        if (productCriteriaDTO.getFactory() != null && productCriteriaDTO.getFactory().isPresent()) {
            Specification currentSpec = ProductSpecs.matchFactorys(productCriteriaDTO.getFactory().get());
            combinedSpec = combinedSpec.and(currentSpec);
        }

        if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
            Specification<Product> currentSpecs = this.buildPriceSpecification(productCriteriaDTO.getPrice().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        return this.productRepository.findAll(combinedSpec, pageable);

    }

    // public Page<Product> getAllProductsWithMinPrice(Pageable pageable, double
    // price) {
    // return this.productRepository.findAll(ProductSpecs.priceMin(price),
    // pageable);

    // }

    // public Page<Product> getAllProductsWithMaxPrice(Pageable pageable, double
    // price) {
    // return this.productRepository.findAll(ProductSpecs.priceMax(price),
    // pageable);

    // }

    // public Page<Product> getProductsWithFactory(Pageable pageable, String
    // factory) {
    // return this.productRepository.findAll(ProductSpecs.matchFactory(factory),
    // pageable);

    // }

    // public Page<Product> getProductsWithFactorys(Pageable pageable, List<String>
    // factory) {
    // return this.productRepository.findAll(ProductSpecs.matchFactorys(factory),
    // pageable);

    // }

    // public Page<Product> getProductsWithPrice(Pageable pageable, String price) {
    // // eg: price 10-toi-15-trieu
    // if (price.equals("10-toi-15-trieu")) {
    // double min = 10000000;
    // double max = 15000000;
    // return this.productRepository.findAll(ProductSpecs.matchPrice(min, max),
    // pageable);
    // } else if (price.equals("15-toi-30-trieu")) {
    // // "15-toi-30-trieu"
    // double min = 15000000;
    // double max = 30000000;
    // return this.productRepository.findAll(ProductSpecs.matchPrice(min, max),
    // pageable);
    // } else {
    // return this.productRepository.findAll(pageable);
    // }

    // }

    // public Page<Product> getProductsWithPrices(Pageable pageable, List<String>
    // price) {
    // Specification<Product> combinedSpec = (root, query, criteriaBuilder) ->
    // criteriaBuilder.disjunction();
    // int count = 0;
    // for (String p : price) {
    // double min = 0;
    // double max = 0;

    // // Set the appropriate min and max based on the price range string
    // switch (p) {
    // case "10-toi-15-trieu":
    // min = 10000000;
    // max = 15000000;
    // count++;
    // break;
    // case "15-toi-20-trieu":
    // min = 15000000;
    // max = 20000000;
    // count++;
    // break;
    // case "20-toi-30-trieu":
    // min = 20000000;
    // max = 30000000;
    // count++;
    // break;
    // // Add more cases as needed
    // }

    // if (min != 0 && max != 0) {
    // Specification<Product> rangeSpec = ProductSpecs.matchMultiplePrice(min, max);
    // combinedSpec = combinedSpec.or(rangeSpec);
    // }
    // }

    // // Check if any price ranges were added (combinedSpec is empty)
    // if (count == 0) {
    // return this.productRepository.findAll(pageable);
    // }

    // return this.productRepository.findAll(combinedSpec, pageable);
    // }
    public Specification<Product> buildPriceSpecification(List<String> price) {
        Specification<Product> combinedSpec = Specification.where(null);
        for (String p : price) {
            double min = 0;
            double max = 0;

            // Set the appropriate min and max based on the price range string
            switch (p) {
                case "duoi-10-trieu":
                    min = 1;
                    max = 10000000;
                    break;
                case "10-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    break;
                case "15-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    break;
                case "tren-20-trieu":
                    min = 20000000;
                    max = 200000000;
                    break;
            }

            if (min != 0 && max != 0) {
                Specification<Product> rangeSpec = ProductSpecs.matchMultiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }

        return combinedSpec;
    }

    public void deleteUserById(long id) {
        this.productRepository.deleteById(id);

    }

    public Optional<Product> fetchProductById(long id) {
        return this.productRepository.findById(id);

    }

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {

        User user = this.userService.getUserByEmail(email);

        if (user != null) {
            // check user da co cart hay chua neu chua tao moi
            Cart cart = this.cartRepository.findByUser(user);

            if (cart == null) {
                // tao moi cart
                Cart otherCart = new Cart();
                otherCart.setUser(user);
                otherCart.setSum(0);
                cart = this.cartRepository.save(otherCart);
            }

            // find product by id
            Optional<Product> product = this.productRepository.findById(productId);

            if (product.isPresent()) {

                Product realProduct = product.get();
                // check san pham da tung duoc them vao gio hang truoc day chua ?
                CartDetail oldDetail = this.cartDetailRepository.findByCartAndProduct(cart, realProduct);

                if (oldDetail == null) {
                    CartDetail cartDetail = new CartDetail();

                    cartDetail.setCart(cart);
                    cartDetail.setProduct(realProduct);
                    cartDetail.setPrice(realProduct.getPrice());
                    cartDetail.setQuantity(quantity);
                    // save cart_Detail
                    this.cartDetailRepository.save(cartDetail);
                    // update cart sum
                    int sum = cart.getSum() + 1;
                    cart.setSum(sum);
                    this.cartRepository.save(cart);
                    session.setAttribute("sum", sum);
                } else {
                    oldDetail.setQuantity(oldDetail.getQuantity() + quantity);
                    this.cartDetailRepository.save(oldDetail);
                }
            }
        }
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handleRemoveCartDetail(long cartDetailId, HttpSession session) {
        Optional<CartDetail> cardDetaiOptional = this.cartDetailRepository.findById(cartDetailId);
        if (cardDetaiOptional.isPresent()) {
            CartDetail cartDetail = cardDetaiOptional.get();
            Cart currentCart = cartDetail.getCart();
            // delete cartDetail
            this.cartDetailRepository.deleteById(cartDetailId);
            // update Cart
            if (currentCart.getSum() > 1) {
                int sum = currentCart.getSum() - 1;
                currentCart.setSum(sum);
                session.setAttribute("sum", sum);
                this.cartRepository.save(currentCart);
            } else {
                // delete cart (sum = 1)
                this.cartRepository.deleteById(currentCart.getId());
                session.setAttribute("sum", 0);
            }

        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        if (cartDetails == null && cartDetails.isEmpty()) {
            System.out.println("cartDetails is null or empty.");
            return;
        }

        for (CartDetail cartDetail : cartDetails) {
            Optional<CartDetail> cartDetaiOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if (cartDetaiOptional.isPresent()) {
                CartDetail currenCartDetail = cartDetaiOptional.get();
                currenCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currenCartDetail);
            }

        }

    }

    public void handlePlaceOrder(User user, HttpSession session, String receiverName, String receiverAddress,
            String receiverPhone) {
        // Step 1: get cart by user
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();
            if (cartDetails != null) {
                // crete order
                Order order = new Order();
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("PENDING");

                double sum = 0;
                for (CartDetail cartDetail : cartDetails) {
                    sum += cartDetail.getPrice();
                }
                order.setTotalPrice(sum);
                order = this.orderRepository.save(order);
                // create orderDetail

                for (CartDetail cartDetail : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(cartDetail.getProduct());
                    orderDetail.setPrice(cartDetail.getPrice());
                    orderDetail.setQuantity(cartDetail.getQuantity());

                    this.orderDetailRepository.save(orderDetail);
                }

            }
            // step 2:delete cart_detail and cart
            for (CartDetail cartDetail : cartDetails) {
                this.cartDetailRepository.deleteById(cartDetail.getId());
            }
            this.cartRepository.deleteById(cart.getId());
            // step 3:update session
            session.setAttribute("sum", 0);

        }

    }

}
