package mt.brokerage.controllers;

import mt.brokerage.models.Order;
import mt.brokerage.services.OrderBook;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    private final OrderBook orderBook;

    public OrderController(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    @GetMapping("/order/{id}")
    public Order getOrder(@PathVariable Long id){
        Order order = orderBook.getOrder(id);
        return order;
    }

    @PostMapping("/order")
    public Order postNewOrder(@RequestBody Order order){
        orderBook.add(order);
        return order;
    }

    @DeleteMapping("/order/{id}")
    public Order postNewOrder(@PathVariable Long id){
        Order order = orderBook.cancel(id);
        return order;
    }

    @GetMapping("/price")
    public Object getPrice(){
        var askPrice = orderBook.getMinSell();
        var bidPrice = orderBook.getMaxBuy();
        var result = new Object() {
            public double ask = askPrice;
            public double bid = bidPrice;
        };
        return result;
    }
}
