package com.corso.demo.serializers;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.corso.demo.models.Customer;

import tools.jackson.databind.ObjectMapper;

public class SerializerJsonCustomer implements Serializer<Customer> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, Customer data) {
        
        if (data == null) {
            return new byte[0];
        }

        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception ex) {
            System.out.println("Error serializing Customer: " + data.getId());
        }
        return new byte[0];
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        Serializer.super.close();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // TODO Auto-generated method stub
        Serializer.super.configure(configs, isKey);
    }


    

}
