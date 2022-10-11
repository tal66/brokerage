package mt.brokerage.services;

import lombok.extern.slf4j.Slf4j;
import mt.brokerage.models.Node;
import mt.brokerage.models.Order;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

@Service
@Slf4j
public class OrderBook {
    private final TreeSet<Node> bids;
    private final TreeSet<Node> asks;

    private final HashMap<Long, Order> ordersById;
    private final HashMap<Double, Node> bidNodeByLimit;
    private final HashMap<Double, Node> askNodeByLimit;

    private Node minSellNode;
    private Node maxBuyNode;
    private static final Node defaultMinSellNode = new Node(Double.MAX_VALUE);
    private static final Node defaultMaxBuyNode = new Node(Double.MIN_VALUE);

    public OrderBook() {
        log.debug("new OrderBook");
        bids = new TreeSet<>(Comparator.comparingDouble(x-> -x.getLimit()));
        asks = new TreeSet<>(Comparator.comparingDouble(x-> x.getLimit()));
        ordersById = new HashMap<>();
        bidNodeByLimit = new HashMap<>();
        askNodeByLimit = new HashMap<>();
        minSellNode = defaultMinSellNode;
        maxBuyNode = defaultMaxBuyNode;
    }

    public double getMinSell(){
        return minSellNode.getLimit();
    }

    public double getMaxBuy(){
        return maxBuyNode.getLimit();
    }

    public int getTotalOrders(){
        return ordersById.size();
    }

    public Order getOrder(long id){
        return ordersById.get(id);
    }

    public void add(Order order){
        if (order.getType() == Order.Type.BUY){
            buy(order);
        } else {
            sell(order);
        }
    }

    private void sell(Order order) {
        double maxBuy = this.maxBuyNode.getLimit();
        double orderPrice = order.getPrice();

        if (orderPrice > maxBuy){
            insertAskToBook(order);
            order.setStatus(Order.Status.OPEN);
        } else {
            log.info("execute {}", order);
            int qSell = order.getQuantity();

            while (qSell > 0){
                Order headOrder = maxBuyNode.getHeadOrder();
                if (headOrder == null || headOrder.getPrice() < orderPrice){
                    log.debug("maxBuyNode.headOrder break quantity loop");
                    break;
                }

                int qBuy = headOrder.getQuantity();
                int minQ = Math.min(order.getQuantity(), qBuy);
                log.info("execute q={} p={}", minQ, orderPrice);

                qSell -= minQ;
                headOrder.decreaseQuantityBy(minQ);

                if (headOrder.getQuantity() == 0){
                    log.debug("remove bid order from book {}", headOrder);
                    log.info("order {} completed", headOrder.getId());
                    ordersById.remove(headOrder.getId());
                    headOrder.setStatus(Order.Status.COMPLETED);
                    setNextMaxBuyNode();
                }
            }

            if (qSell > 0){
                log.debug("order {} partially fulfilled, q={} left", order.getId(), qSell);
                order.setQuantity(qSell);
                order.setStatus(Order.Status.OPEN);
                insertAskToBook(order);
            } else {
                log.info("order {} completed", order.getId());
                order.setStatus(Order.Status.COMPLETED);
            }
        }
    }

    private void buy(Order order) {
        double minAsk = this.minSellNode.getLimit();
        double orderPrice = order.getPrice();

        if (orderPrice < minAsk){
            insertBidToBook(order);
            order.setStatus(Order.Status.OPEN);
        } else {
            log.info("execute {}", order);
            int qBuy = order.getQuantity();

            while (qBuy > 0){
                Order headOrder = minSellNode.getHeadOrder();
                if (headOrder == null || headOrder.getPrice() > orderPrice){
                    log.debug("minSellNode.headOrder break quantity loop");
                    break;
                }

                int qSell = headOrder.getQuantity();
                int minQ = Math.min(qSell, qBuy);
                log.info("execute q={} p={}", minQ, headOrder.getPrice());

                headOrder.decreaseQuantityBy(minQ);
                qBuy -= minQ;

                if (headOrder.getQuantity() == 0){
                    log.debug("remove ask order from book {}", headOrder);
                    log.info("order {} completed", headOrder.getId());
                    headOrder.setStatus(Order.Status.COMPLETED);
                    ordersById.remove(headOrder.getId());
                    setNextMinSellNode();
                }
            }

            if (qBuy > 0){
                log.debug("order {} partially fulfilled, q={} left", order.getId(), qBuy);
                order.setQuantity(qBuy);
                order.setStatus(Order.Status.OPEN);
                insertBidToBook(order);
            } else {
                log.info("order {} completed", order.getId());
                order.setStatus(Order.Status.COMPLETED);
            }
        }
    }

    private void insertAskToBook(Order order) {
        log.info("insert ask to book {}", order);
        double orderPrice = order.getPrice();
        Node node = askNodeByLimit.get(orderPrice);
        if (node == null){
            node = new Node(orderPrice);
            askNodeByLimit.put(orderPrice, node);
            asks.add(node);
        }

        node.add(order);
        ordersById.put(order.getId(), order);

        if (orderPrice < minSellNode.getLimit()){
            log.debug("minSellNode is {}", node);
            minSellNode = node;
        }
    }

    private void insertBidToBook(Order order) {
        log.info("insert bid to book {}", order);
        double orderPrice = order.getPrice();
        Node node = bidNodeByLimit.get(orderPrice);
        if (node == null){
            node = new Node(orderPrice);
            bids.add(node);
            bidNodeByLimit.put(orderPrice, node);
        }

        node.add(order);
        ordersById.put(order.getId(), order);

        if (orderPrice > maxBuyNode.getLimit()){
            log.debug("set maxBuyNode {}", node);
            maxBuyNode = node;
        }
    }

    private void setNextMaxBuyNode() {
        log.debug("setNextMaxBuyNode. start: {}", maxBuyNode);
        maxBuyNode.setNextHeadOrder();

        if (maxBuyNode.getHeadOrder() == null){
            bids.remove(maxBuyNode);
            askNodeByLimit.remove(maxBuyNode.getLimit());
            if (bids.size() > 0){
                maxBuyNode = bids.first();
            } else {
                maxBuyNode = defaultMaxBuyNode;
            }
        }
        log.debug("setNextMaxBuyNode. end: {}", maxBuyNode);
    }

    private void setNextMinSellNode() {
        log.debug("setNextMinSellNode. start: {}", minSellNode);
        minSellNode.setNextHeadOrder();

        if (minSellNode.getHeadOrder() == null){
            asks.remove(minSellNode);
            bidNodeByLimit.remove(minSellNode.getLimit());
            if (asks.size() > 0){
                minSellNode = asks.first();
            } else {
                minSellNode = defaultMinSellNode;
            }
        }
        log.debug("setNextMinSellNode. end: {}", minSellNode);
    }

    public Order cancel(long id){
         Order order = ordersById.get(id);
         if (order == null){
             log.error("cancel order: id not found {}", id);
             return order;
         }

         Node node;
         if (order.getType() == Order.Type.BUY){
             node = bidNodeByLimit.get(order.getPrice());
         } else {
             node = askNodeByLimit.get(order.getPrice());
         }

        if (node == null){
            log.error("cancel order: node not found {}", order);
            return order;
        }

        log.info("cancel {}", order);
        node.setNextHeadOrder();
        ordersById.remove(id);
        order.setStatus(Order.Status.CANCELED);

        if (node.getHeadOrder() == null){
            removeNode(node, order.getType());
        }
        return order;
    }

    private void removeNode(Node node, Order.Type type) {
        if (type == Order.Type.BUY){
            if (node == maxBuyNode){
                setNextMaxBuyNode();
            } else {
                bids.remove(node);
            }
            bidNodeByLimit.remove(node.getLimit());
        } else {
            if (node == minSellNode){
                setNextMinSellNode();
            } else {
                asks.remove(node);
            }
            askNodeByLimit.remove(node.getLimit());
        }
    }
}
