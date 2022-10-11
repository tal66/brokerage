package mt.brokerage.models;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Node {
    private final double limit;
    private Order headOrder;
    private Order tailOrder;

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

    public Order getHeadOrder() {
        return headOrder;
    }

    public void setNextHeadOrder() {
        if (this.headOrder == null){
            return;
        }

        this.headOrder = this.headOrder.next;
        if (this.headOrder != null){
            this.headOrder.prev = null;
        }
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
