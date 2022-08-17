package app;

public class Main {

    public static void main(String[] args) {
        OrderBook orderBook = new OrderBook();
        orderBook.add(new Order(100.1, 1, Order.Type.BUY));
        orderBook.add(new Order(100.1, 1, Order.Type.BUY));
        orderBook.add(new Order(100.3, 1, Order.Type.BUY));
        orderBook.add(new Order(100, 1, Order.Type.SELL));
        orderBook.add(new Order(100.2, 2, Order.Type.SELL));
        orderBook.add(new Order(100.1, 2, Order.Type.SELL));
    }
}
