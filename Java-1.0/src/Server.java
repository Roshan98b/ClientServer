import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;

public class Server {

  public static void main(String[] args) throws Exception {

    try {

      ServerSocket server = new ServerSocket(8888);
      int counter = 0;
      System.out.println("Server has started..");

      while (true) {

        counter++;
        Socket client = server.accept();
        System.out.println(" >> " + "Client No:" + counter + " started!");
        ReaderWriter rw = new ReaderWriter(client, counter);
        rw.start();

      }

    } catch (Exception e) {

      System.out.println(e);

    }

  }

}

class ReaderWriter extends Thread {
  
  public static Semaphore wrt = new Semaphore(1);
  public static Semaphore mutex = new Semaphore(1);
  private String clM = "", srM = "",tc,line;
  Socket client;
  int clientNo;
  public static int readcount=0;

  ReaderWriter(Socket inSocket, int counter) {

    client = inSocket;
    clientNo = counter;

  }

  public void run() {
    
    try {

      DataInputStream inStream = new DataInputStream(client.getInputStream());
      DataOutputStream outStream = new DataOutputStream(client.getOutputStream());
      
      while (!clM.equals("exit")) {

        clM = inStream.readUTF();
        System.out.println("Client " + clientNo);

        if(clM.equals("reader")) {

          tc = "reader";
          mutex.acquire();
          readcount++;
          System.out.println(readcount);
          if(readcount==1) wrt.acquire();
          mutex.release();
          
          outStream.writeUTF("Reader Ready");
          outStream.flush();
          outStream.writeUTF("Contents of file :");
          outStream.flush();          
          srM="";
          System.out.println("Reader..!!");

          FileReader fr = new FileReader("file.txt");
          BufferedReader bfr = new BufferedReader(fr);

          while((line = bfr.readLine()) != null) {
        
            srM += line;
            srM += "\n";

          }

          bfr.close();
          outStream.writeUTF(srM);
          outStream.flush();
          
        }else if(clM.equals("writer")) {

          tc = "writer";
          wrt.acquire();
          srM = "Writer Ready";
          outStream.writeUTF(srM);
          outStream.flush();

          srM = inStream.readUTF();
          System.out.println(srM);
          
          FileWriter fw = new FileWriter("file.txt");
          BufferedWriter bfw = new BufferedWriter(fw);
          bfw.write(srM);
          bfw.close();

        }else {

          System.out.println(clM);

        }

      }

      if(tc.equals("reader")) {

          System.out.println(tc);
          mutex.acquire();
          readcount--;
          if(readcount==0) wrt.release();
          mutex.release();

      }else if(tc.equals("writer")) {

          System.out.println(tc);
          wrt.release();

      } else {

          System.out.println(tc);

      }

      inStream.close();
      outStream.close();
      client.close();

    } catch (Exception ex) {

      System.out.println(ex);

    } finally {

      System.out.println("Client -" + clientNo + " exit!! ");

    }

  }

}
