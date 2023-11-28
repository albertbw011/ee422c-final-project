import java.io.Serializable;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1234567;
    private String username;
    private String password;

    public Customer(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
