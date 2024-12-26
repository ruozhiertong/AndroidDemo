package model;

public class User {
    private String nickname;
    private String account;
    private String password;
    private double weight;
    private double height;
    private int age;
    private String goal; //  lose_weight, muscle_gain, keep_fit

    public User(){}
    public User(String nickname, String account, String password) {
        this.nickname = nickname;
        this.account = account;
        this.password = password;
    }

    // Getters and Setters
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}