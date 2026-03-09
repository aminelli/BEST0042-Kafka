package com.corso.demo.models;

import com.google.gson.Gson;

public class Customer {
    
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private Integer age = 0;

    public Customer() {
    }

    public Customer(String id, String firstname, String lastname, String email) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public Customer(String id, String firstname, String lastname, String email, Integer age) {
        this(id,  firstname,  lastname,  email);
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }


    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Customer fromJson(String json) {
        return new Gson().fromJson(json, Customer.class);
    }

}
