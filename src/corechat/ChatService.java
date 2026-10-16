package corechat;

import java.util.ArrayList;
import java.util.InputMismatchException;

import static corechat.ChatApplication.scanner;

public class ChatService {

    private User currentUser;
    private ArrayList<User> users;
    private MessageManager messageManager;


    public ChatService(User currentUser, ArrayList<User> users, MessageManager messageManager) {
        this.currentUser = currentUser;
        this.users = users;
        this.messageManager = messageManager;
    }

    public void chats() {


        if (users.size() <= 1) {
            System.out.println("No other users available :( ");
            return;
        }

        int count = 1;
        for (int i = 0; i < users.size(); i++) {

            if (users.get(i).equals(currentUser)) {
                continue;
            }
            System.out.println(count + "." + users.get(i).getUserName());
            count++;
        }

        boolean valid = false;
        int choice = 0;
        while (!valid) {
            try {
                System.out.print("\nEnter your choice : ");
                choice = scanner.nextInt();
                scanner.nextLine();
                System.out.println();

                valid = true;

            } catch (InputMismatchException e) {

                System.out.println("-> Invalid choice :( \n");
                scanner.nextLine();
            }
        }
        count = 1;

        User selectedUser = null;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).equals(currentUser)) {
                continue;
            }
            if (count == choice) {
                selectedUser = users.get(i);
                break;
            }
            count++;
        }

        if (selectedUser != null) {
            System.out.println("========================================");
            System.out.println(" Chatting with @" + selectedUser.getUserName());
            System.out.println(" (Type 'exit' or 'back' to leave chat)  ");
            System.out.println("========================================");

            // Display previous chat history
            for (int i = 0; i < messageManager.getMessages().size(); i++) {
                Message message = messageManager.getMessages().get(i);

                if (
                        (message.getSender().getUserName().equalsIgnoreCase(currentUser.getUserName())
                                && message.getReceiver().getUserName().equalsIgnoreCase(selectedUser.getUserName()))
                                ||
                                (message.getSender().getUserName().equalsIgnoreCase(selectedUser.getUserName())
                                        && message.getReceiver().getUserName().equalsIgnoreCase(currentUser.getUserName()))
                ) {
                    System.out.println(
                            message.getSender().getUserName() + " : " + message.getMessage());
                }
            }
            System.out.println("----------------------------------------");

            // Continuous chat loop
            while (true) {
                System.out.print(currentUser.getUserName() + " > ");
                String messageText = scanner.nextLine().trim();

                if (messageText.equalsIgnoreCase("exit") || messageText.equalsIgnoreCase("back")) {
                    System.out.println("\nLeft chat with @" + selectedUser.getUserName() + ".\n");
                    break;
                }

                if (messageText.isEmpty()) {
                    continue;
                }

                Message message = new Message(currentUser, selectedUser, messageText);
                messageManager.storeMessages(message);
                FileManager.saveMessage(message);
            }

        } else {
            System.out.println("Invalid user choice !!");
        }


    }
}



