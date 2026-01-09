package passwordmanager.model;

import java.io.Serializable;

public class Entry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String service;
    private final String username;
    private final String password;

    public Entry(String name, String username, String password) {
        this.service = name;
        this.username = username;
        this.password = password;
    }

    public String getService() {
        return service;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Service: " + service + " | User: " + username + " | Password: " + password;
    }
}
