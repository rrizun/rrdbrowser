import org.mortbay.jetty.*;
import org.mortbay.jetty.webapp.*;

public class RrdBrowser {
  public static void main(String[] args) throws Exception {
    Server server = new Server(8001);
    server.addHandler(new WebAppContext("war", "/rrdbrowser"));
    server.start();
  }
}
