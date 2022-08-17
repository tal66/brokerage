package app;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

@Slf4j
public class OrderBook {
    private final TreeSet<Node> bids; // switch this later
    private final TreeSet<Node> asks;

    private final HashMap<Long, Order> ordersById;
    private final HashMap<Double, Node> bidLimitNodeByLimit;
    private final HashMap<Double, Node> askLimitNodeByLimit;

    private Node minSellNode;
    private Node maxBuyNode;
    private static final Node defaultMinSellNode = new Node(Double.MAX_VALUE);
    private static final Node defaultMaxBuyNode = new Node(Double.MIN_VALUE);

    public OrderBook() {
        log.debug("new OrderBook");
        bids = new TreeSet<>(Comparator.comparingDouble(x-> -x.getLimit()));
        asks = new TreeSet<>(Comparator.comparingDouble(x-> x.getLimit()));
        ordersById = new HashMap<>();
        bidLimitNodeByLimit = new HashMap<>();
        askLimitNodeByLimit = new HashMap<>();
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
            log.info("insert ask to book {}", order);

            Node node = askLimitNodeByLimit.get(orderPrice);
            if (node == null){
                node = new Node(orderPrice);
                asks.add(node);
            }

            node.add(order);
            ordersById.put(order.getId(), order);

            if (orderPrice < minSellNode.getLimit()){
                log.debug("minSellNode is {}", node);
                minSellNode = asks.first();
            }
        } else {
            log.info("execute {}", order);
            int qSell = order.getQuantity();

            while (qSell > 0){
                if (maxBuyNode.headOrder == null || maxBuyNode.headOrder.getPrice() < orderPrice){
                    log.debug("maxBuyNode.headOrder break quantity loop");
                    break;
                }

                int qBuy = maxBuyNode.headOrder.getQuantity();
                int minQ = Math.min(order.getQuantity(), qBuy);
                log.info("execute q={} p={}", minQ, orderPrice);

                qSell -= minQ;
                maxBuyNode.headOrder.decreaseQuantityBy(minQ);

                if (maxBuyNode.headOrder.getQuantity() == 0){
                    log.debug("remove bid order from book {}", maxBuyNode.headOrder);
                    log.info("order {} completed", maxBuyNode.headOrder.getId());
                    ordersById.remove(maxBuyNode.headOrder.getId());
                    setNextMaxBuyNode();
                }
            }

            if (qSell > 0){
                log.debug("order {} partially fulfilled, q={} left", order.getId(), qSell);
                order.setQuantity(qSell);

                Node node = askLimitNodeByLimit.get(orderPrice);
                if (node == null){
                    node = new Node(orderPrice);
                    askLimitNodeByLimit.put(orderPrice, node);
                }

                node.add(order);
                asks.add(node);
                ordersById.put(order.getId(), order);
                log.info("insert ask to book {}", order);

                if (orderPrice < minSellNode.getLimit()){
                    log.debug("set minSellNode {}", node);
                    minSellNode = node;
                }

            } else {
                log.info("order {} completed", order.getId());
            }
        }
    }


    private void buy(Order order) {
        double minAsk = this.minSellNode.getLimit();
        double orderPrice = order.getPrice();

        if (orderPrice < minAsk){
            log.info("insert bid to book {}", order);

            Node node = bidLimitNodeByLimit.get(orderPrice);
            if (node == null){
                node = new Node(orderPrice);
                bids.add(node);
                bidLimitNodeByLimit.put(orderPrice, node);
            }

            node.add(order);
            ordersById.put(order.getId(), order);

            if (orderPrice > maxBuyNode.getLimit()){
                log.debug("maxBuyNode is {}", node);
                maxBuyNode = node;
            }
        } else {
            log.info("execute {}", order);
            int qBuy = order.getQuantity();

            while (qBuy > 0){
                if (minSellNode.headOrder == null || minSellNode.headOrder.getPrice() > orderPrice){
                    log.debug("minSellNode.headOrder break quantity loop");
                    break;
                }

                int qSell = minSellNode.headOrder.getQuantity();
                int minQ = Math.min(qSell, qBuy);
                log.info("execute q={} p={}", minQ, minSellNode.headOrder.getPrice());

                minSellNode.headOrder.decreaseQuantityBy(minQ);
                qBuy -= minQ;

                if (minSellNode.headOrder.getQuantity() == 0){
                    log.debug("remove ask order from book {}", minSellNode.headOrder);
                    log.info("order {} completed", minSellNode.headOrder.getId());
                    ordersById.remove(minSellNode.headOrder.getId());
                    setNextMinSellNode();
                }
            }

            if (qBuy > 0){
                log.debug("order {} partially fulfilled, q={} left", order.getId(), qBuy);
                order.setQuantity(qBuy);
                Node node = bidLimitNodeByLimit.get(orderPrice);
                if (node == null){
                    node = new Node(orderPrice);
                    bidLimitNodeByLimit.put(orderPrice, node);
                }

                node.add(order);
                bids.add(node);
                ordersById.put(order.getId(), order);
                log.info("insert bid order to book {}", order);

                if (orderPrice > maxBuyNode.getLimit()){
                    log.debug("set maxBuyNode {}", node);
                    maxBuyNode = node;
                }

            } else {
                log.info("order {} completed", order.getId());
            }
        }
    }

    private void setNextMaxBuyNode() {
        log.debug("setNextMaxBuyNode. start: {}", maxBuyNode);
        maxBuyNode.headOrder = maxBuyNode.headOrder.next;

        if (maxBuyNode.headOrder != null){
            maxBuyNode.headOrder.prev = null;
        } else {
            bids.remove(maxBuyNode);
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
        minSellNode.headOrder = minSellNode.headOrder.next;

        if (minSellNode.headOrder != null){
            minSellNode.headOrder.prev = null;
        } else {
            asks.remove(minSellNode);
            if (asks.size() > 0){
                minSellNode = asks.first();
            } else {
                minSellNode = defaultMinSellNode;
            }
        }
        log.debug("setNextMinSellNode. end: {}", minSellNode);
    }

    public void cancel(long id){
        // TODO
        // Order order = ordersById.get(id);
    }
}
