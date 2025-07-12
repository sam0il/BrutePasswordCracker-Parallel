package org.example;

public class ResultHolder {
    private volatile boolean found = false;
    private String password;

    public synchronized void setPassword(String password) {
        if (!found) {
            this.password = password;
            this.found = true;
        }
    }

    public boolean isFound() {
        return found;
    }

    public String getPassword() {
        return password;
    }
}

