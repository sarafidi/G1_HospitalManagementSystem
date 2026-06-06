package model;

public abstract class Person {
    // private fields
    private String name;
    private int age;
    private String gender;
    private String phone;
    private String email;

    // constructor
    public Person(String name, int age, String gender, String phone, String email) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
    }

    // getters
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    // setters
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }

    // abstract getInfo()
    public abstract String getInfo();
}