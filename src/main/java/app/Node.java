package app;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Node {
    private final double limit;
    Order headOrder;
    Order tailOrder;

    // later
//    Node parent;
//    Node left;
//    Node right;
//    boolean red; // false = black
//    int size;


    public Node(double limit) {
        this.limit = limit;
    }

    public void add(Order order){
        if (headOrder == null){
            this.headOrder = order;
        } else {
            this.tailOrder.next = order;
            order.prev = this.tailOrder;
        }
        this.tailOrder = order;

        log.debug("added order {}. head= {}", order.getId(), headOrder.getId());
    }


    public double getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return "Node[" +
                "limit=" + limit +
                ", headOrder=" + headOrder +
                ']';
    }
}
