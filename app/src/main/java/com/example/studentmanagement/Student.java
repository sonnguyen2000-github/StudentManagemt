package com.example.studentmanagement;

import java.util.Date;

public class Student{
    private String id;
    private String fullName;
    private Date birthDay;
    private String email;
    private String address;

    public Student(){
    }

    public Student(String id, String fullName, Date birthDay, String email, String address){
        this.id = id;
        this.fullName = fullName;
        this.birthDay = birthDay;
        this.email = email;
        this.address = address;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getFullName(){
        return fullName;
    }

    public void setFullName(String fullName){
        this.fullName = fullName;
    }

    public Date getBirthDay(){
        return birthDay;
    }

    public void setBirthDay(Date birthDay){
        this.birthDay = birthDay;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address){
        this.address = address;
    }
}
