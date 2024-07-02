package com.example.springcloud1;


import com.fasterxml.jackson.annotation.JsonProperty;
public class User {

    private Long id;
    private Integer age;
    private String username;
    private String password;
    private String address ;

    public User(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public User( String username, String password, String address,Integer age)
    {
        this.username = username;
        this.password = password;
        this.address =  address;
        this.age = age;
    }

    public User(Long id)
    {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
