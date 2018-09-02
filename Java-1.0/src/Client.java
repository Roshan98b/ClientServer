import java.net.*;
import java.io.*;

public class Client {
  
  public static void main(String[] args) throws Exception {
    
    try {
      
      Socket socket = new Socket("127.0.0.1", 8888);
      DataInputStream inStream = new DataInputStream(socket.getInputStream());
      DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      
      String clM = "", srM = "";
      StringBuilder txt = new StringBuilder("");
      System.out.println("Reader/writer :");
      
      while (!clM.equals("exit")) {
        
        clM = br.readLine();
        outStream.writeUTF(clM);
        outStream.flush();

        if(clM.equals("reader")) {

          System.out.println("Busy..!");
          srM = inStream.readUTF();
          System.out.println(srM);
          srM = inStream.readUTF();
          System.out.println(srM);
          srM = inStream.readUTF();
          System.out.println(srM);

        }else if(clM.equals("writer")) {

          System.out.println("Busy..!");
          srM = inStream.readUTF();
          System.out.println(srM);
          System.out.println("Enter Text ( enter * in new line to send )");
          clM = br.readLine();

          while(!(clM.equals("*"))) {

              txt.append(clM);
              txt.append("\n");
              clM = br.readLine();

          }

          clM = txt.toString(); 
          outStream.writeUTF(clM);
          outStream.flush();

        }else {

          System.out.println("Bye");

        }

      }
      
      outStream.close();
      outStream.close();
      socket.close();
    
    } catch (Exception e) {

      System.out.println(e);

    }
  
  }

}
