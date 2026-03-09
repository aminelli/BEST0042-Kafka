package com.corso.demo.deserializers;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.corso.demo.models.Customer2;


public class DeserializerBinCustomer implements Deserializer<Customer2> {

    @Override
    public Customer2 deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);

            int id = buffer.getInt();

            int firstNameLength = buffer.getInt();
            int lastNameLength = buffer.getInt();
            
            byte[] firstNameBytes = new byte[firstNameLength];
            byte[] lastNameBytes = new byte[lastNameLength];

            buffer.get(firstNameBytes);
            buffer.get(lastNameBytes);

            String firstName = new String(firstNameBytes, "UTF-8");
            String lastName = new String(lastNameBytes, "UTF-8");

            Customer2 customer = new Customer2();
            customer.setId(id);
            customer.setFirstname(firstName);
            customer.setLastname(lastName);

            return customer;

        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        Deserializer.super.close();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // TODO Auto-generated method stub
        Deserializer.super.configure(configs, isKey);
    }
    
}
