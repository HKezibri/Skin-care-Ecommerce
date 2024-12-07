package util;

import model.User;

public class SessionContext {
    private static final SessionContext instance = new SessionContext();
    
    private int clientId;
    private User loggedInUser;

    private SessionContext() {}

    public static SessionContext getInstance() {
        return instance;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
