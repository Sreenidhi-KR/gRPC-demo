package performanceTest;



public class JsonPerson{

    public JsonPerson(){}
    public JsonPerson(String lastName , int age, String email, boolean employed, double salary, long bankAccountNumber, int balance , String bio) {
        this.age = age;
        this.lastName = lastName;
        this.email = email;
        this.employed = employed;
        this.salary = salary;
        this.bankAccountNumber = bankAccountNumber;
        this.balance = balance;
        this.bio = bio;
    }

    public String lastName;
    public int age;
    public String email;
    public boolean employed;
    public double salary;
    public long bankAccountNumber;
    public int balance;

    public String bio;
}
