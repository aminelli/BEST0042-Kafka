package com.ecommerce.streaming;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Classe POJO che rappresenta un evento di Change Data Capture (CDC) da Debezium.
 * Debezium genera eventi in formato JSON con una struttura specifica contenente
 * i dati before/after e metadati sulla sorgente.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebeziumCdcEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Payload contenente i dati effettivi della riga
    @JsonProperty("payload")
    private Payload payload;
    
    public Payload getPayload() {
        return payload;
    }
    
    public void setPayload(Payload payload) {
        this.payload = payload;
    }
    
    /**
     * Classe interna che rappresenta il payload dell'evento CDC.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        // Dati prima della modifica (null per INSERT)
        @JsonProperty("before")
        private OrderData before;
        
        // Dati dopo la modifica (null per DELETE)
        @JsonProperty("after")
        private OrderData after;
        
        // Informazioni sulla sorgente del dato
        @JsonProperty("source")
        private Source source;
        
        // Tipo di operazione: c (create), u (update), d (delete), r (read/snapshot)
        @JsonProperty("op")
        private String operation;
        
        // Timestamp dell'evento in millisecondi
        @JsonProperty("ts_ms")
        private Long timestamp;
        
        public OrderData getBefore() {
            return before;
        }
        
        public void setBefore(OrderData before) {
            this.before = before;
        }
        
        public OrderData getAfter() {
            return after;
        }
        
        public void setAfter(OrderData after) {
            this.after = after;
        }
        
        public Source getSource() {
            return source;
        }
        
        public void setSource(Source source) {
            this.source = source;
        }
        
        public String getOperation() {
            return operation;
        }
        
        public void setOperation(String operation) {
            this.operation = operation;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Informazioni sulla sorgente del dato (database, tabella, ecc.).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @JsonProperty("db")
        private String database;
        
        @JsonProperty("table")
        private String table;
        
        public String getDatabase() {
            return database;
        }
        
        public void setDatabase(String database) {
            this.database = database;
        }
        
        public String getTable() {
            return table;
        }
        
        public void setTable(String table) {
            this.table = table;
        }
    }
    
    /**
     * Dati della riga dell'ordine.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderData implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @JsonProperty("order_id")
        private Integer orderId;
        
        @JsonProperty("customer_id")
        private Integer customerId;
        
        @JsonProperty("order_date")
        private String orderDate; // Timestamp come stringa ISO 8601
        
        @JsonProperty("total_amount")
        private String totalAmount; // Decimal come stringa (da Debezium)
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("shipping_address")
        private String shippingAddress;
        
        @JsonProperty("payment_method")
        private String paymentMethod;
        
        @JsonProperty("updated_at")
        private String updatedAt; // Timestamp come stringa ISO 8601
        
        public Integer getOrderId() {
            return orderId;
        }
        
        public void setOrderId(Integer orderId) {
            this.orderId = orderId;
        }
        
        public Integer getCustomerId() {
            return customerId;
        }
        
        public void setCustomerId(Integer customerId) {
            this.customerId = customerId;
        }
        
        public String getOrderDate() {
            return orderDate;
        }
        
        public void setOrderDate(String orderDate) {
            this.orderDate = orderDate;
        }
        
        public String getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(String totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getShippingAddress() {
            return shippingAddress;
        }
        
        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }
        
        public String getPaymentMethod() {
            return paymentMethod;
        }
        
        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
        
        public String getUpdatedAt() {
            return updatedAt;
        }
        
        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
