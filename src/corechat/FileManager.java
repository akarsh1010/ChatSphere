package corechat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileManager {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";
    private static final String MESSAGES_FILE = DATA_DIR + File.separator + "messages.txt";

    static {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", 2);
                String username = parts[0];
                String password = parts.length > 1 ? parts[1] : "";
                users.add(new User(username, password));
            }
        } catch (IOException e) {
            System.out.println("-> Error reading users file: " + e.getMessage());
        }
        return users;
    }

    public static void saveUser(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(user.getUserName() + "," + user.getPassword());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("-> Error saving user to file: " + e.getMessage());
        }
    }

    public static ArrayList<Message> loadMessages(ArrayList<User> users) {
        ArrayList<Message> messages = new ArrayList<>();
        File file = new File(MESSAGES_FILE);
        if (!file.exists()) {
            return messages;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", 4);
                if (parts.length >= 3) {
                    String senderName = parts[0];
                    String receiverName = parts[1];
                    String content = parts[2];
                    String timestamp = parts.length >= 4 ? parts[3] : "";

                    User sender = findUser(users, senderName);
                    User receiver = findUser(users, receiverName);

                    if (sender == null) sender = new User(senderName, "");
                    if (receiver == null) receiver = new User(receiverName, "");
                    messages.add(new Message(sender, receiver, content, timestamp));
                }
            }
        } catch (IOException e) {
            System.out.println("-> Error reading messages file: " + e.getMessage());
        }
        return messages;
    }

    public static void saveMessage(Message message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MESSAGES_FILE, true))) {
            writer.write(message.getSender().getUserName() + ","
                    + message.getReceiver().getUserName() + ","
                    + message.getMessage() + ","
                    + message.getTimestamp());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("-> Error saving message to file: " + e.getMessage());
        }
    }

    public static void overwriteUsers(ArrayList<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, false))) {
            for (User u : users) {
                writer.write(u.getUserName() + "," + u.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("-> Error rewriting users file: " + e.getMessage());
        }
    }

    public static void overwriteMessages(ArrayList<Message> messages) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MESSAGES_FILE, false))) {
            for (Message m : messages) {
                writer.write(m.getSender().getUserName() + ","
                        + m.getReceiver().getUserName() + ","
                        + m.getMessage() + ","
                        + m.getTimestamp());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("-> Error rewriting messages file: " + e.getMessage());
        }
    }

    private static User findUser(ArrayList<User> users, String username) {
        for (User u : users) {
            if (u.getUserName().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }
}
