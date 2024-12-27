package vn.hoidanit.laptopshop.controller.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ProductCriteriaDTO;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.VNPayService;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ItemController {
    private final ProductService productService;
    private final VNPayService vnPayService;

    public ItemController(ProductService productService, VNPayService vnPayService) {
        this.productService = productService;
        this.vnPayService = vnPayService;
    }

    @GetMapping("/product/{id}")
    public String getProductPage(Model model, @PathVariable Long id) {
        Product product = this.productService.fetchProductById(id).get();
        model.addAttribute("product", product);
        model.addAttribute("id", id);

        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long productId = id;
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, productId, session, 1);

        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartDetailPage(Model model, HttpServletRequest request) {
        User currentUser = new User(); // null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);
        Cart cart = this.productService.fetchByUser(currentUser);

        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        double totalPrice = 0;
        for (CartDetail cartDetail : cartDetails) {
            totalPrice += cartDetail.getPrice() * cartDetail.getQuantity();
        }
        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cart", cart);

        return "client/cart/show";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String deleteCartDetail(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long cartDetailId = id;
        this.productService.handleRemoveCartDetail(cartDetailId, session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String getCheckOutPage(Model model, HttpServletRequest request) {
        User currentUser = new User();// null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);

        List<CartDetail> cartDetails = cart.getCartDetails();
        if (cartDetails == null) {
            cartDetails = new ArrayList<>();
        }

        double totalPrice = 0;
        for (CartDetail cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        return "client/cart/checkout";
    }

    @PostMapping("/confirm-checkout")
    public String getCheckOutPage(@ModelAttribute("cart") Cart cart) {
        // List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() :
        // cart.getCartDetails();
        List<CartDetail> cartDetails = cart.getCartDetails();
        if (cartDetails == null) {
            cartDetails = new ArrayList<>();
        }
        System.out.println(cartDetails);
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);
        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(
            HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("totalPrice") String totalPrice) throws UnsupportedEncodingException {
        // get user
        User currentUser = new User(); // null
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        this.productService.handlePlaceOrder(currentUser, session, receiverName, receiverAddress, receiverPhone,
                paymentMethod, uuid);
        if (!paymentMethod.equals("CODE")) {
            String ip = this.vnPayService.getIpAddress(request);
            String vnUrl = this.vnPayService.generateVNPayURL(Double.parseDouble(totalPrice), uuid, ip);
            return "redirect:" + vnUrl;
        }

        return "redirect:/thanks";
    }

    @GetMapping("/thanks")
    public String getThankYouPage(Model model, @RequestParam("vpn_ResponseCode") Optional<String> vnpayResponseCode,
            @RequestParam("vnp_Txnref") Optional<String> paymentRef) {
        if (vnpayResponseCode.isPresent() && paymentRef.isPresent()) {
            String paymentStatus = vnpayResponseCode.equals("00") ? "PAYMENT_SUCCEED" : "PAYMENT_FAILED";
            this.productService.updatePaymentStatus(paymentRef.get(), paymentStatus);
        }
        return "client/cart/thanks";
    }

    @PostMapping("/add-product-from-view-detail")
    public String handleAddProductFromViewDetail(
            @RequestParam("id") long id,
            @RequestParam("quantity") long quantity,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, id, session, quantity);
        return "redirect:/product/" + id;
    }

    @GetMapping("/products")
    public String getProductPage(Model model, ProductCriteriaDTO productCriteriaDTO, HttpServletRequest request) {
        int page = 1;
        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            } else {
                ///
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        Pageable pageable = PageRequest.of(page - 1, 10);

        // check sort
        if (productCriteriaDTO.getSort() != null && productCriteriaDTO.getSort().isPresent()) {
            String sort = productCriteriaDTO.getSort().get();
            if (sort.equals("gia-tang-dan")) {
                pageable = PageRequest.of(page - 1, 10, Sort.by(Product_.PRICE).ascending());

            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of(page - 1, 10, Sort.by(Product_.PRICE).descending());

            }
        }

        Page<Product> pageProduct = this.productService.getAllProductsWithSpec(pageable, productCriteriaDTO);

        // case 1:
        // double minPrice = minOptional.isPresent() ?
        // Double.parseDouble(minOptional.get()) : 0;
        // Page<Product> pageProduct =
        // this.productService.getAllProductsWithMinPrice(pageable, minPrice);

        // case 2:
        // double maxPrice = maxOptional.isPresent() ?
        // Double.parseDouble(maxOptional.get()) : 0;
        // Page<Product> pageProduct =
        // this.productService.getAllProductsWithMaxPrice(pageable, maxPrice);

        // case3:
        // String factory = factoryOptional.isPresent() ? factoryOptional.get() : "";
        // Page<Product> pageProduct =
        // this.productService.getProductsWithFactory(pageable, factory);

        // case 4:
        // List<String> factory = Arrays.asList(factoryOptional.get().split(","));
        // Page<Product> pageProduct =
        // this.productService.getProductsWithFactorys(pageable, factory);

        // case:5
        // String price = priceOptional.isPresent() ? priceOptional.get() : "";
        // Page<Product> pageProduct =
        // this.productService.getProductsWithPrice(pageable, price);
        // case 6:
        // List<String> price = Arrays.asList(priceOptional.get().split(","));
        // Page<Product> pageProduct =
        // this.productService.getProductsWithPrices(pageable, price);

        List<Product> listProduct = pageProduct.getContent().size() > 0 ? pageProduct.getContent()
                : new ArrayList<Product>();
        String query = request.getQueryString();
        System.out.println(query);
        if (query != null && !query.isBlank()) {
            query = query.replace("page" + page, "");

        }

        model.addAttribute("products", listProduct);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageProduct.getTotalPages());
        model.addAttribute("queryString", query);

        return "client/product/show";
    }

}
