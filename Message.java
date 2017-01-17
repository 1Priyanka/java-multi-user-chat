import java.io.*;

public class Message implements Serializable {

  protected static final long serialVersionUID = 1112122200L;

  static final int WHO = 0, MESSAGE = 1, LOGOUT = 2;
  private int type;
  private String text;

  Message(int type, String text) {
    this.type = type;
    this.text = text;
  }

  int getType() {
    return type;
  }

  String getText() {
    return text;
  }

  void setText(String text) {
    this.text = text;
  }
}
