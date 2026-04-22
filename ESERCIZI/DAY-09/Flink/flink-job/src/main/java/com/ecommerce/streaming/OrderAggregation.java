package com.ecommerce.streaming;

import java.io.Serializable;
import java.time.Instant;

/**
 * Classe che rappresenta le statistiche aggregate degli ordini.
 * Utilizzata per calcolare aggregazioni in finestre temporali.
 */
public class OrderAggregation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Inizio e fine della finestra temporale
    private long windowStart;
    private long windowEnd;
    
    // Metriche aggregate
    private int totalOrders;           // Totale ordini nella finestra
    private double totalRevenue;        // Ricavo totale
    private double averageOrderValue;   // Valore medio ordine
    private double maxOrderValue;       // Ordine di valore massimo
    private double minOrderValue;       // Ordine di valore minimo
    
    // Distribuzione per stato
    private int pendingOrders;
    private int processingOrders;
    private int shippedOrders;
    private int deliveredOrders;
    private int cancelledOrders;
    
    // Timestamp di elaborazione
    private long processingTime;
    
    public OrderAggregation() {
        this.minOrderValue = Double.MAX_VALUE;
        this.maxOrderValue = Double.MIN_VALUE;
    }
    
    public OrderAggregation(long windowStart, long windowEnd) {
        this();
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.processingTime = System.currentTimeMillis();
    }
    
    /**
     * Aggiunge un ordine all'aggregazione aggiornando tutte le metriche.
     */
    public void addOrder(DebeziumCdcEvent.OrderData order) {
        if (order == null || order.getTotalAmount() == null) {
            return;
        }
        
        totalOrders++;
        
        // Parse del totalAmount da base64 (Debezium invia decimals codificati)
        double amount = 0.0;
        try {
            // Debezium invia decimals come base64 encoded bytes
            // Il formato è: bytes codificati in base64 che rappresentano un BigInteger
            // con scala 2 (due decimali)
            byte[] bytes = java.util.Base64.getDecoder().decode(order.getTotalAmount());
            java.math.BigInteger unscaled = new java.math.BigInteger(bytes);
            java.math.BigDecimal decimal = new java.math.BigDecimal(unscaled, 2); // scala 2
            amount = decimal.doubleValue();
        } catch (Exception e) {
            // Prova anche parsing diretto se non è base64
            try {
                amount = Double.parseDouble(order.getTotalAmount());
            } catch (NumberFormatException nfe) {
                System.err.println("Cannot parse totalAmount: " + order.getTotalAmount());
                totalOrders--; // Rollback del conteggio
                return;
            }
        }
        
        totalRevenue += amount;
        
        // Aggiornamento min/max
        if (amount > maxOrderValue) {
            maxOrderValue = amount;
        }
        if (amount < minOrderValue) {
            minOrderValue = amount;
        }
        
        // Calcolo media
        averageOrderValue = totalRevenue / totalOrders;
        
        // Conteggio per stato
        String status = order.getStatus();
        if (status != null) {
            switch (status) {
                case "PENDING" -> pendingOrders++;
                case "PROCESSING" -> processingOrders++;
                case "SHIPPED" -> shippedOrders++;
                case "DELIVERED" -> deliveredOrders++;
                case "CANCELLED" -> cancelledOrders++;
            }
        }
    }
    
    /**
     * Merge di due aggregazioni (usato per combinare aggregazioni parziali).
     */
    public OrderAggregation merge(OrderAggregation other) {
        if (other == null) {
            return this;
        }
        
        this.totalOrders += other.totalOrders;
        this.totalRevenue += other.totalRevenue;
        this.maxOrderValue = Math.max(this.maxOrderValue, other.maxOrderValue);
        this.minOrderValue = Math.min(this.minOrderValue, other.minOrderValue);
        
        if (this.totalOrders > 0) {
            this.averageOrderValue = this.totalRevenue / this.totalOrders;
        }
        
        this.pendingOrders += other.pendingOrders;
        this.processingOrders += other.processingOrders;
        this.shippedOrders += other.shippedOrders;
        this.deliveredOrders += other.deliveredOrders;
        this.cancelledOrders += other.cancelledOrders;
        
        return this;
    }
    
    // Getters e Setters
    
    public long getWindowStart() {
        return windowStart;
    }
    
    public void setWindowStart(long windowStart) {
        this.windowStart = windowStart;
    }
    
    public long getWindowEnd() {
        return windowEnd;
    }
    
    public void setWindowEnd(long windowEnd) {
        this.windowEnd = windowEnd;
    }
    
    public int getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public double getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public double getMaxOrderValue() {
        return maxOrderValue;
    }
    
    public void setMaxOrderValue(double maxOrderValue) {
        this.maxOrderValue = maxOrderValue;
    }
    
    public double getMinOrderValue() {
        return minOrderValue;
    }
    
    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }
    
    public int getPendingOrders() {
        return pendingOrders;
    }
    
    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }
    
    public int getProcessingOrders() {
        return processingOrders;
    }
    
    public void setProcessingOrders(int processingOrders) {
        this.processingOrders = processingOrders;
    }
    
    public int getShippedOrders() {
        return shippedOrders;
    }
    
    public void setShippedOrders(int shippedOrders) {
        this.shippedOrders = shippedOrders;
    }
    
    public int getDeliveredOrders() {
        return deliveredOrders;
    }
    
    public void setDeliveredOrders(int deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }
    
    public int getCancelledOrders() {
        return cancelledOrders;
    }
    
    public void setCancelledOrders(int cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }
    
    public long getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }
    
    @Override
    public String toString() {
        return String.format(
            "OrderAggregation{window=[%s to %s], orders=%d, revenue=%.2f, avg=%.2f, max=%.2f, min=%.2f, " +
            "status=[pending=%d, processing=%d, shipped=%d, delivered=%d, cancelled=%d]}",
            Instant.ofEpochMilli(windowStart),
            Instant.ofEpochMilli(windowEnd),
            totalOrders,
            totalRevenue,
            averageOrderValue,
            maxOrderValue,
            minOrderValue == Double.MAX_VALUE ? 0.0 : minOrderValue,
            pendingOrders,
            processingOrders,
            shippedOrders,
            deliveredOrders,
            cancelledOrders
        );
    }
}
