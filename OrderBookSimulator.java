import java.util.*;

/**
 * OrderBook Simulator - Mimics a stock exchange matching engine
 * Uses price-time priority matching algorithm
 */
public class OrderBookSimulator {
    
    /**
     * Order class representing a single order in the system
     */
    static class Order {
        private final String orderId;
        private final OrderType type;
        private double price;
        private int quantity;
        private final long timestamp;
        
        public Order(String orderId, OrderType type, double price, int quantity) {
            this.orderId = orderId;
            this.type = type;
            this.price = price;
            this.quantity = quantity;
            this.timestamp = System.nanoTime(); // High-resolution timestamp for ordering
        }
        
        // Copy constructor for safe operations
        public Order(Order other) {
            this.orderId = other.orderId;
            this.type = other.type;
            this.price = other.price;
            this.quantity = other.quantity;
            this.timestamp = other.timestamp;
        }
        
        public String getOrderId() { return orderId; }
        public OrderType getType() { return type; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public long getTimestamp() { return timestamp; }
        
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        @Override
        public String toString() {
            return String.format("Order[%s, %s, $%.2f, Qty:%d]", 
                orderId, type, price, quantity);
        }
    }
    
    /**
     * Enum for order types
     */
    enum OrderType {
        BUY, SELL
    }
    
    /**
     * Comparator for BUY orders (price-time priority)
     * BUY orders: highest price first, then earliest timestamp
     */
    static class BuyOrderComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            // First compare by price (descending - higher price has priority)
            int priceCompare = Double.compare(o2.getPrice(), o1.getPrice());
            if (priceCompare != 0) {
                return priceCompare;
            }
            // If prices equal, compare by timestamp (ascending - earlier has priority)
            return Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
    }
    
    /**
     * Comparator for SELL orders (price-time priority)
     * SELL orders: lowest price first, then earliest timestamp
     */
    static class SellOrderComparator implements Comparator<Order> {
        @Override
        public int compare(Order o1, Order o2) {
            // First compare by price (ascending - lower price has priority)
            int priceCompare = Double.compare(o1.getPrice(), o2.getPrice());
            if (priceCompare != 0) {
                return priceCompare;
            }
            // If prices equal, compare by timestamp (ascending - earlier has priority)
            return Long.compare(o1.getTimestamp(), o2.getTimestamp());
        }
    }
    
    /**
     * Trade class representing a completed trade
     */
    static class Trade {
        private final String buyOrderId;
        private final String sellOrderId;
        private final double price;
        private final int quantity;
        private final long timestamp;
        
        public Trade(String buyOrderId, String sellOrderId, double price, int quantity) {
            this.buyOrderId = buyOrderId;
            this.sellOrderId = sellOrderId;
            this.price = price;
            this.quantity = quantity;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format("Trade[Buy:%s, Sell:%s, Price:$%.2f, Qty:%d, Time:%d]", 
                buyOrderId, sellOrderId, price, quantity, timestamp);
        }
    }
    
    /**
     * Main OrderBook class containing matching engine logic
     */
    static class OrderBook {
        // Priority queues for BUY and SELL orders
        private final PriorityQueue<Order> buyOrders;
        private final PriorityQueue<Order> sellOrders;
        
        // List of completed trades
        private final List<Trade> trades;
        
        public OrderBook() {
            buyOrders = new PriorityQueue<>(new BuyOrderComparator());
            sellOrders = new PriorityQueue<>(new SellOrderComparator());
            trades = new ArrayList<>();
        }
        
        /**
         * Add a new order to the order book and attempt to match it
         * @param order The order to add
         */
        public void addOrder(Order order) {
            System.out.println("\n=== Adding New Order: " + order + " ===");
            
            if (order.getType() == OrderType.BUY) {
                processBuyOrder(order);
            } else {
                processSellOrder(order);
            }
            
            // Print order books after processing
            printOrderBook();
        }
        
        /**
         * Process a BUY order - match against existing SELL orders
         * @param buyOrder The BUY order to process
         */
        private void processBuyOrder(Order buyOrder) {
            Order remainingBuyOrder = new Order(buyOrder);
            
            // Try to match with existing SELL orders
            while (!sellOrders.isEmpty() && remainingBuyOrder.getQuantity() > 0) {
                Order bestSellOrder = sellOrders.peek();
                
                // Check if match is possible (BUY price >= SELL price)
                if (remainingBuyOrder.getPrice() >= bestSellOrder.getPrice()) {
                    // Execute trade
                    executeTrade(remainingBuyOrder, bestSellOrder);
                } else {
                    // No more possible matches (BUY price < best SELL price)
                    break;
                }
            }
            
            // If any quantity remains, add to buy order book
            if (remainingBuyOrder.getQuantity() > 0) {
                buyOrders.add(remainingBuyOrder);
                System.out.println("Added remaining BUY order to book: " + remainingBuyOrder);
            }
        }
        
        /**
         * Process a SELL order - match against existing BUY orders
         * @param sellOrder The SELL order to process
         */
        private void processSellOrder(Order sellOrder) {
            Order remainingSellOrder = new Order(sellOrder);
            
            // Try to match with existing BUY orders
            while (!buyOrders.isEmpty() && remainingSellOrder.getQuantity() > 0) {
                Order bestBuyOrder = buyOrders.peek();
                
                // Check if match is possible (BUY price >= SELL price)
                if (bestBuyOrder.getPrice() >= remainingSellOrder.getPrice()) {
                    // Execute trade
                    executeTrade(bestBuyOrder, remainingSellOrder);
                } else {
                    // No more possible matches (best BUY price < SELL price)
                    break;
                }
            }
            
            // If any quantity remains, add to sell order book
            if (remainingSellOrder.getQuantity() > 0) {
                sellOrders.add(remainingSellOrder);
                System.out.println("Added remaining SELL order to book: " + remainingSellOrder);
            }
        }
        
        /**
         * Execute a trade between a BUY and SELL order
         * @param buyOrder The BUY order (from buyOrders queue)
         * @param sellOrder The SELL order (from sellOrders queue or incoming)
         */
        private void executeTrade(Order buyOrder, Order sellOrder) {
            // Determine trade price (use the earlier order's price - price-time priority)
            // In real exchanges, different rules apply, but we'll use the sell order's price
            // as it's the limit price being matched
            double tradePrice = sellOrder.getPrice();
            
            // Determine trade quantity (minimum of both orders' quantities)
            int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
            
            // Create and record the trade
            Trade trade = new Trade(buyOrder.getOrderId(), sellOrder.getOrderId(), 
                                  tradePrice, tradeQuantity);
            trades.add(trade);
            
            System.out.println("\n--- Trade Executed ---");
            System.out.println("Buy Order: " + buyOrder.getOrderId());
            System.out.println("Sell Order: " + sellOrder.getOrderId());
            System.out.printf("Trade Price: $%.2f%n", tradePrice);
            System.out.println("Trade Quantity: " + tradeQuantity);
            System.out.println("Trade Record: " + trade);
            
            // Update order quantities
            buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
            sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);
            
            // Remove fully filled orders from the book
            if (buyOrder.getQuantity() == 0) {
                buyOrders.poll(); // Remove the order from the queue
            }
            
            if (sellOrder.getQuantity() == 0) {
                sellOrders.poll(); // Remove the order from the queue
            }
        }
        
        /**
         * Print the current state of the order book
         */
        public void printOrderBook() {
            System.out.println("\n=== ORDER BOOK STATUS ===");
            System.out.println("BUY ORDERS (Highest to Lowest Price):");
            
            if (buyOrders.isEmpty()) {
                System.out.println("  [Empty]");
            } else {
                // Create a copy and print sorted
                List<Order> sortedBuys = new ArrayList<>(buyOrders);
                sortedBuys.sort(new BuyOrderComparator());
                for (Order order : sortedBuys) {
                    System.out.printf("  %s%n", order);
                }
            }
            
            System.out.println("\nSELL ORDERS (Lowest to Highest Price):");
            if (sellOrders.isEmpty()) {
                System.out.println("  [Empty]");
            } else {
                // Create a copy and print sorted
                List<Order> sortedSells = new ArrayList<>(sellOrders);
                sortedSells.sort(new SellOrderComparator());
                for (Order order : sortedSells) {
                    System.out.printf("  %s%n", order);
                }
            }
            
            System.out.println("\nCOMPLETED TRADES: " + trades.size());
            for (Trade trade : trades) {
                System.out.println("  " + trade);
            }
            System.out.println("=== END ORDER BOOK ===\n");
        }
        
        /**
         * Get current best bid (highest buy price)
         */
        public Double getBestBid() {
            return buyOrders.isEmpty() ? null : buyOrders.peek().getPrice();
        }
        
        /**
         * Get current best ask (lowest sell price)
         */
        public Double getBestAsk() {
            return sellOrders.isEmpty() ? null : sellOrders.peek().getPrice();
        }
        
        /**
         * Get the spread (difference between best ask and best bid)
         */
        public Double getSpread() {
            Double bestBid = getBestBid();
            Double bestAsk = getBestAsk();
            
            if (bestBid == null || bestAsk == null) {
                return null;
            }
            
            return bestAsk - bestBid;
        }
    }
    
    /**
     * Main method to demonstrate the order book simulator
     */
    public static void main(String[] args) {
        System.out.println("=== ORDER BOOK SIMULATOR START ===\n");
        
        // Create order book
        OrderBook orderBook = new OrderBook();
        
        // Initial order book display
        orderBook.printOrderBook();
        
        // Demonstration 1: Simple matching (full fills)
        System.out.println("\n\n=== DEMO 1: SIMPLE MATCHING ===");
        orderBook.addOrder(new Order("S1", OrderType.SELL, 100.00, 100));
        orderBook.addOrder(new Order("B1", OrderType.BUY, 100.50, 100));
        
        // Demonstration 2: Partial fills
        System.out.println("\n\n=== DEMO 2: PARTIAL FILLS ===");
        orderBook.addOrder(new Order("S2", OrderType.SELL, 101.00, 150));
        orderBook.addOrder(new Order("B2", OrderType.BUY, 101.50, 75));  // Partial fill
        
        // Demonstration 3: Multiple orders at same price (time priority)
        System.out.println("\n\n=== DEMO 3: TIME PRIORITY ===");
        try {
            // Add small delay to ensure different timestamps
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        orderBook.addOrder(new Order("S3", OrderType.SELL, 99.50, 50));
        orderBook.addOrder(new Order("S4", OrderType.SELL, 99.50, 30));  // Same price, later time
        
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        orderBook.addOrder(new Order("B3", OrderType.BUY, 100.00, 100));  // Should match S3 first
        
        // Demonstration 4: Price priority
        System.out.println("\n\n=== DEMO 4: PRICE PRIORITY ===");
        orderBook.addOrder(new Order("S5", OrderType.SELL, 102.00, 50));
        orderBook.addOrder(new Order("S6", OrderType.SELL, 101.50, 40));  // Lower price, should match first
        orderBook.addOrder(new Order("B4", OrderType.BUY, 102.00, 60));   // Should match S6 then S5 partially
        
        // Demonstration 5: Unmatched orders
        System.out.println("\n\n=== DEMO 5: UNMATCHED ORDERS ===");
        orderBook.addOrder(new Order("S7", OrderType.SELL, 105.00, 100));  // Too high, won't match
        orderBook.addOrder(new Order("B5", OrderType.BUY, 95.00, 100));    // Too low, won't match
        
        // Final order book display
        System.out.println("\n\n=== FINAL ORDER BOOK STATE ===");
        orderBook.printOrderBook();
        
        // Display market summary
        System.out.println("\n=== MARKET SUMMARY ===");
        Double bestBid = orderBook.getBestBid();
        Double bestAsk = orderBook.getBestAsk();
        Double spread = orderBook.getSpread();
        
        System.out.printf("Best Bid: %s%n", bestBid == null ? "N/A" : String.format("$%.2f", bestBid));
        System.out.printf("Best Ask: %s%n", bestAsk == null ? "N/A" : String.format("$%.2f", bestAsk));
        System.out.printf("Spread: %s%n", spread == null ? "N/A" : String.format("$%.2f", spread));
        
        System.out.println("\n=== ORDER BOOK SIMULATOR END ===");
    }
}