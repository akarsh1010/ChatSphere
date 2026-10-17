package corechat;

import java.util.ArrayList;

public class AdminService {

    // Primary Super Admin username
    private static final String SUPER_ADMIN = "alokrana";

    public static boolean isAdmin(String username) {
        if (username == null) return false;
        String normalized = username.trim().toLowerCase();
        return normalized.equalsIgnoreCase(SUPER_ADMIN) || normalized.equalsIgnoreCase("admin");
    }

    public static boolean deleteUser(String adminUsername, String targetUsername) {
        if (!isAdmin(adminUsername)) {
            return false;
        }

        if (targetUsername.equalsIgnoreCase(SUPER_ADMIN) || targetUsername.equalsIgnoreCase("admin") || targetUsername.equalsIgnoreCase(AIService.BOT_NAME)) {
            return false; // Cannot delete super admin or SphereAI bot
        }

        // 1. Remove user from users file
        ArrayList<User> users = FileManager.loadUsers();
        boolean removed = users.removeIf(u -> u.getUserName().equalsIgnoreCase(targetUsername));
        if (removed) {
            FileManager.overwriteUsers(users);
        }

        // 2. Remove all related messages
        ArrayList<Message> messages = FileManager.loadMessages(users);
        messages.removeIf(m -> m.getSender().getUserName().equalsIgnoreCase(targetUsername)
                || m.getReceiver().getUserName().equalsIgnoreCase(targetUsername));
        FileManager.overwriteMessages(messages);

        return removed;
    }

    public static boolean clearConversation(String adminUsername, String user1, String user2) {
        if (!isAdmin(adminUsername)) {
            return false;
        }

        ArrayList<User> users = FileManager.loadUsers();
        ArrayList<Message> messages = FileManager.loadMessages(users);

        boolean removed = messages.removeIf(m ->
                (m.getSender().getUserName().equalsIgnoreCase(user1) && m.getReceiver().getUserName().equalsIgnoreCase(user2))
                        || (m.getSender().getUserName().equalsIgnoreCase(user2) && m.getReceiver().getUserName().equalsIgnoreCase(user1))
        );

        if (removed) {
            FileManager.overwriteMessages(messages);
        }
        return true;
    }

    public static boolean sendAdminWarning(String adminUsername, String targetUser, String warningText) {
        if (!isAdmin(adminUsername)) {
            return false;
        }

        ArrayList<User> users = FileManager.loadUsers();
        User sender = null;
        User receiver = null;

        for (User u : users) {
            if (u.getUserName().equalsIgnoreCase(adminUsername)) sender = u;
            if (u.getUserName().equalsIgnoreCase(targetUser)) receiver = u;
        }

        if (receiver == null) return false;
        if (sender == null) sender = new User("admin", "");

        String formattedWarning = "⚠️ [Official Admin Notice]: " + warningText;
        Message message = new Message(sender, receiver, formattedWarning);
        FileManager.saveMessage(message);

        return true;
    }

    public static int getTotalUsers() {
        return FileManager.loadUsers().size();
    }

    public static int getTotalMessages() {
        ArrayList<User> users = FileManager.loadUsers();
        return FileManager.loadMessages(users).size();
    }
}
