package app;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Order {
    public enum Type {
        BUY, SELL;
    }

    private static long idCounter = 0;

    private final long id;
    private final double price;
    private final Type type;
    private int quantity;
    Order next;
    Order prev;
    // ticker

    public Order(double price, int quantity, Type type) {
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.id = ++idCounter;
    }

    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0){
            String err = "set negative quantity";
            log.error(err);
            throw new RuntimeException(err);
        }
        this.quantity = quantity;
    }

    public void decreaseQuantityBy(int quantity) {
        setQuantity(this.quantity - quantity);
    }

    @Override
    public String toString() {
        return "Order[" +
                "id=" + id +
                ", price=" + price +
                ", quantity=" + quantity +
                ", type=" + type +
                ", next=" + ((next == null)? null: next.id) +
                ']';
    }
}
