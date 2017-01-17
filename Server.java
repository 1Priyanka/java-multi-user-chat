import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
  // Уникално id за всяка колекция
  private static int uniqueId;
  // Масив от клиентски нишки
  private ArrayList<ClientThread> clients;
  // За часа
  private SimpleDateFormat sdf;
  // Порта, на който ще слушаме
  private int port;

  private boolean shouldWork;

  public Server(int port) {
    this.port = port;
    this.sdf = new SimpleDateFormat("HH:mm:ss");
    this.clients = new ArrayList<ClientThread>();
  }
  
  // Пускаме сървъра
  public void start() {
    shouldWork = true;

    try {
      // Създаване на сървърен сокет
      ServerSocket serverSocket = new ServerSocket(port);

      while (shouldWork) 
      {
        display("Server waiting for Clients on port " + port + ".");
        
        Socket socket = serverSocket.accept();

        if (!shouldWork)
          break;

        // Правим нова нишка за новия потребител
        ClientThread newThread = new ClientThread(socket);
        clients.add(newThread);
        newThread.start();
      }

      // Трябва да спра работа
      try {
        serverSocket.close();

        for (ClientThread thread : clients) {
          try {
            thread.input.close();
            thread.output.close();
            thread.socket.close();
          }
          catch (IOException ioE) {}
        }
      }
      catch (Exception e) {
        display("Exception closing the server and clients: " + e);
      }
    }

    // Нещо друго се е объркало
    catch (IOException e) {
      String message = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
      display(message);
    }
  }   

  private void display(String message) {
    String messageWithTime = sdf.format(new Date()) + " " + message;
    System.out.println(messageWithTime);
  }

  private synchronized void broadcast(String message) {
    String time = sdf.format(new Date());
    String messageWithTime = time + " " + message + "\n";
    System.out.print(messageWithTime);

    for (int i = clients.size() - 1; i >= 0; i--) {
      ClientThread thread = clients.get(i);
      
      // Опитваме се да пишем, ако не успеем, махаме потребителската нишка.
      if (!thread.writeMessage(messageWithTime)) {
        clients.remove(i);
        display("Disconnected Client " + thread.username + " removed from list.");
      }
    }
  }

  // За клиент, който logout-ва с LOGOUT
  synchronized void remove(int id) {
    // Махаме нишката с идентификатор id
    for (int i = 0; i < clients.size(); i++) {
      ClientThread thread = clients.get(i);

      if (thread.id == id) {
        clients.remove(i);
        return;
      }
    }
  }

  public static void main(String[] args) {
    int portNumber = 1500;

    switch(args.length) {
      case 1:
        try {
          portNumber = Integer.parseInt(args[0]);
        }
        catch (Exception e) {
          System.out.println("Invalid port number.");
          System.out.println("Usage is: > java Server [portNumber]");
          return;
        }
      case 0:
        break;
      default:
        System.out.println("Usage is: > java Server [portNumber]");
        return;
    }
    
    // Създаваме нов обект от тип сървър и го пускаме.
    Server server = new Server(portNumber);
    server.start();
  }

  class ClientThread extends Thread {
    Socket socket;
    ObjectInputStream input;
    ObjectOutputStream output;

    int id;
    String username;
    Message message;
    String date;

    ClientThread(Socket socket) {
      id = ++uniqueId;
      this.socket = socket;

      // Опитваме се да създадем двата потока
      System.out.println("Thread trying to create Object Input/Output Streams");
      try {
        output = new ObjectOutputStream(socket.getOutputStream());
        input  = new ObjectInputStream(socket.getInputStream());

        Message message = (Message) input.readObject();
        username = Encryption.decrypt(message.getText());

        display(username + " just connected.");
      }
      catch (IOException e) {
        display("Exception creating new Input/output Streams: " + e);
        return;
      }

      catch (ClassNotFoundException e) {}

      date = new Date().toString() + "\n";
    }

    // "Главната" функция
    public void run() {
      boolean shouldWork = true;

      while (shouldWork) {
        try {
          message = (Message) input.readObject();
          String decryptedMessage = Encryption.decrypt(message.getText());
          message.setText(decryptedMessage);
        }
        catch (IOException e1) {
          display(username + " Exception reading Streams: " + e1);
          break;        
        }
        catch (ClassNotFoundException e2) {
          break;
        }

        String textMessage = message.getText();

        switch (message.getType()) {
          case Message.MESSAGE:
            broadcast(username + ": " + textMessage);
            break;
          case Message.LOGOUT:
            display(username + " disconnected with a LOGOUT message.");
            shouldWork = false;
            break;
          case Message.WHO:
            writeMessage("List of the users connected at " + sdf.format(new Date()) + "\n");

            for (int i = 0; i < clients.size(); i++) {
              ClientThread thread = clients.get(i);
              writeMessage((i+1) + ") " + thread.username + " since " + thread.date);
            }
            break;
        }
      }
      
      // Ако излезнем от цикъла, махаме текущия потребител.
      remove(id);
      close();
    }
    
    // Опитваме се да затворим всичко
    private void close() {
      try {
        if(output != null)
          output.close();
      }
      catch(Exception e) {}

      try {
        if(input != null)
          input.close();
      }
      catch(Exception e) {};

      try {
        if(socket != null)
          socket.close();
      }
      catch (Exception e) {}
    }

    // Пишем до клиента
    private boolean writeMessage(String textMessage) {
      if(!socket.isConnected()) {
        close();
        return false;
      }

      try {
        String encryptedMessage = Encryption.encrypt(textMessage);
        Message message = new Message(Message.MESSAGE, encryptedMessage);

        output.writeObject(message);
      }
      catch(IOException e) {
        display("Error sending message to " + username);
        display(e.toString());
      }
      return true;
    }
  }
}
