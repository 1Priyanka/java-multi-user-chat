import java.net.*;
import java.io.*;
import java.util.*;

public class Client  {

  private ObjectInputStream input;
  private ObjectOutputStream output;
  private Socket socket;

  private String server, username;
  private int port;

  Client(String server, int port, String username) {
    this.server = server;
    this.port = port;
    this.username = username;
  }

  public boolean start() {
    // Опит за свързване до сървъра
    try {
      socket = new Socket(server, port);
    }
    catch (Exception e) {
      System.out.println("Error connectiong to server: " + e);
      return false;
    }
    
    String message = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
    System.out.println(message);

    try {
      input  = new ObjectInputStream(socket.getInputStream());
      output = new ObjectOutputStream(socket.getOutputStream());
    }
    catch (IOException e) {
      System.out.println("Error creating new Input/Output Streams: " + e);
      return false;
    }

    // Създава нишка, която да слуша от сървъра
    ListenFromServer thread = new ListenFromServer();
    thread.start();
    
    // Предаваме си потребителското име към сървъра,
    // всички останали съобщения, които пращаме, ще са от тип Message
    try {
      String encryptedUsername = Encryption.encrypt(username);
      Message usernameMessage = new Message(Message.MESSAGE, encryptedUsername);

      output.writeObject(usernameMessage);
    }
    catch (IOException e) {
      System.out.println("Error doing login : " + e);
      disconnect();
      return false;
    }

    return true;
  }

  void sendMessage(Message message) {
    try {
      String encryptedMessage = Encryption.encrypt(message.getText());
      message.setText(encryptedMessage);

      output.writeObject(message);
    }
    catch (IOException e) {
      System.out.println("Error writing to server: " + e);
    }
  }

  // Ако искаме да disconnect-нем, затваряме потоците и сокета
  private void disconnect() {
    try { 
      if (input != null)
        input.close();
    }
    catch (Exception e) {}

    try {
      if (output != null)
        output.close();
    }
    catch (Exception e) {}

    try {
      if (socket != null)
        socket.close();
    }
    catch (Exception e) {}
  }

  public static void main(String[] args) {
    int portNumber = 1500;
    String serverAddress = "localhost";
    String userName = "Anonymous";

    switch (args.length) {
      // > java Client username portNumber serverAddr
      case 3:
        serverAddress = args[2];

      // > java Client username portNumber
      case 2:
        try {
          portNumber = Integer.parseInt(args[1]);
        }
        catch(Exception e) {
          System.out.println("Invalid port number.");
          System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
          return;
        }

      // > java Client username
      case 1: 
        userName = args[0];

      // > java Client
      case 0:
        break;

      // Невалиден брой аргументи
      default:
        System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
      return;
    }

    Client client = new Client(serverAddress, portNumber, userName);

    // Проверяваме дали можем да стартираме връзката със сървъра
    if (!client.start())
      return;

    Scanner scan = new Scanner(System.in);

    while (true) {
      System.out.print("> ");
      
      // Чакаме съобщение от потребителя
      String message = scan.nextLine();

      if (message.equalsIgnoreCase("LOGOUT")) {
        client.sendMessage(new Message(Message.LOGOUT, ""));
        break;
      }

      else if (message.equalsIgnoreCase("WHO"))
        client.sendMessage(new Message(Message.WHO, ""));       
      else
        client.sendMessage(new Message(Message.MESSAGE, message));
    }
    
    // Ако излезнем от цикъла, disconnect-ваме
    client.disconnect();  
  }

  // Клас, който чака съобщения от сървъра
  class ListenFromServer extends Thread {
    public void run() {
      while (true) {
        try {
          Message message = (Message) input.readObject();
          String decryptedMessage = Encryption.decrypt(message.getText());

          System.out.println(decryptedMessage);
          System.out.print("> ");
        }
        catch (IOException e) {
          System.out.println("Server has closed the connection: " + e);
          break;
        }
        
        // Трябва да хванем ClassNotFound заради readObject
        catch (ClassNotFoundException e2) {
        }
      }
    }
  }
}
