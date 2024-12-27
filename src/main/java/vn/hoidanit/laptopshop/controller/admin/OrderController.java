package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.Order_;
import vn.hoidanit.laptopshop.service.OrderService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/order")
    public String getDashboard(Model model, @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Order_.ID).descending());
        // Pageable pageable = PageRequest.of(page - 1, 5);
        Page<Order> pageOrder = this.orderService.getAllOrder(pageable);
        List<Order> listOrder = pageOrder.getContent();
        model.addAttribute("orders", listOrder);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageOrder.getTotalPages());
        return "admin/order/show";
    }

    @GetMapping("/admin/order/{id}")
    public String getOrderDetail(Model model, @PathVariable long id) {
        Order order = this.orderService.fechOrderById(id).get();
        model.addAttribute("carDetail", order);
        model.addAttribute("id", id);
        model.addAttribute("orderDetails", order.getOrderDetails());
        return "admin/order/detail";

    }

    @GetMapping("/admin/order/update/{id}")
    public String getUpdateOrderPage(Model model, @PathVariable long id) {
        Optional<Order> currentOrder = this.orderService.fechOrderById(id);
        model.addAttribute("newOrder", currentOrder.get());

        return "admin/order/update";
    }

    @PostMapping("admin/order/update")
    public String UpdateOrder(@ModelAttribute("newOrder") Order order) {
        this.orderService.handleUpdateOrder(order);
        return "redirect:/admin/order";

    }

    @GetMapping("/admin/order/delete/{id}")
    public String getDeletePage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newOrder", new Order());
        return "admin/order/delete";
    }

    @PostMapping("/admin/order/delete")
    public String postDeletePage(@ModelAttribute("newOrder") Order order) {
        this.orderService.deleteOrderById(order.getId());
        return "redirect:/admin/order";
    }

}
