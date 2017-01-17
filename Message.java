import java.io.*;

public class Message implements Serializable {

  protected static final long serialVersionUID = 1112122200L;

  // The different types of message sent by the Client
  // WHOISIN to receive the list of the users connected
  // MESSAGE an ordinary message
  // LOGOUT to disconnect from the Server
  static final int WHO = 0, MESSAGE = 1, LOGOUT = 2;
  private int type;
  private String text;
  
  // constructor
  Message(int type, String text) {
    this.type = type;
    this.text = text;
  }
  
  // getters
  int getType() {
    return type;
  }
  String getText() {
    return text;
  }

  // setter
  void setText(String text) {
    this.text = text;
  }
}
