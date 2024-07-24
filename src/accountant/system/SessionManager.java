package accountant.system;

import java.util.prefs.Preferences;

public class SessionManager {

    private static SessionManager instance;
    private boolean loggedIn = false;
    private int currentUserId; // tambahkan untuk menyimpan ID pengguna
    private String currentUserEmail;
    private final Preferences prefs;

    private SessionManager() {
        prefs = Preferences.userNodeForPackage(SessionManager.class);
        // Load session state from preferences
        loggedIn = prefs.getBoolean("loggedIn", false);
        currentUserId = prefs.getInt("currentUserId", 0); // default value 0
        currentUserEmail = prefs.get("currentUserEmail", null);
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        prefs.putBoolean("loggedIn", loggedIn);
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        prefs.putInt("currentUserId", userId);
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
        prefs.put("currentUserEmail", email);
    }

    public void logout() {
        setLoggedIn(false);
        prefs.remove("currentUserId");
        prefs.remove("currentUserEmail");
    }
}
