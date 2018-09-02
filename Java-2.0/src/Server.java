import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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

	public static Semaphore wrt[] = {
			new Semaphore(1),
			new Semaphore(1),
			new Semaphore(1),
			new Semaphore(1),
			new Semaphore(1),
	};
	public static Semaphore mutex[] =  {
			new Semaphore(1),
			new Semaphore(1),
			new Semaphore(1),
			new Semaphore(1),
			new Semaphore(1),
	};
	private String clM = "", srM = "", tc, line;
	Socket client;
	int clientNo;
	public static int readcount[] = {0,0,0,0,0};

	ReaderWriter(Socket inSocket, int counter) {

		client = inSocket;
		clientNo = counter;
	}

	public void run() {

		try {

			DataInputStream inStream = new DataInputStream(client.getInputStream());
			DataOutputStream outStream = new DataOutputStream(client.getOutputStream());

			String uname = inStream.readUTF();
			String passw = inStream.readUTF();
			
			Class.forName("com.mysql.jdbc.Driver");
			Connection c = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "root");
			Statement s = c.createStatement();
			String qry = "select Fullname from jee.login where Username = '" + uname + "' and Password = '" + passw + "'";
			ResultSet r = s.executeQuery(qry);
			if (r.next()) {
				String name = r.getString("Fullname");
				System.out.println("Welcome "+name);
				outStream.writeInt(1);
				outStream.flush();
			} else {
				System.out.println("Invalid Username or Password");
				outStream.writeInt(0);
				outStream.flush();
			}
			c.close();
			s.close();
			r.close();
			
			int d = inStream.readInt();
			int f = inStream.readInt();
			String filename="";
			int ch = 0;
			
			switch (d) {
			case 1:
				switch (f) {
				case 1: filename = "Dir1/File1.txt";
						ch=1;
					break;
				case 2: filename = "Dir1/File2.txt";
						ch=2;
					break;
				}
				break;

			case 2:
				switch (f) {
				case 1: filename = "Dir2/File1.txt";
						ch=3;
					break;
				case 2: filename = "Dir2/File2.txt";
						ch=4;
					break;
				}
				break;
			}
			
			while (!clM.equals("exit")) {

				clM = inStream.readUTF();
				System.out.println("Client " + clientNo);

				if (clM.equals("reader")) {

					tc = "reader";
					mutex[ch].acquire();
					readcount[ch]++;
					System.out.println(readcount);
					if (readcount[ch] == 1)
						wrt[ch].acquire();
					mutex[ch].release();

					outStream.writeUTF("Reader Ready");
					outStream.flush();
					outStream.writeUTF("Contents of file :");
					outStream.flush();
					srM = "";
					System.out.println("Reader..!!");

					FileReader fr = new FileReader(filename);
					BufferedReader bfr = new BufferedReader(fr);

					while ((line = bfr.readLine()) != null) {

						srM += line;
						srM += "\n";

					}

					bfr.close();
					outStream.writeUTF(srM);
					outStream.flush();

				} else if (clM.equals("writer")) {

					tc = "writer";
					wrt[ch].acquire();
					srM = "Writer Ready";
					outStream.writeUTF(srM);
					outStream.flush();

					srM = inStream.readUTF();
					System.out.println(srM);

					FileWriter fw = new FileWriter(filename);
					BufferedWriter bfw = new BufferedWriter(fw);
					bfw.write(srM);
					bfw.close();

				} else {

					System.out.println(clM);

				}

			}

			if (tc.equals("reader")) {

				System.out.println(tc);
				mutex[ch].acquire();
				readcount[ch]--;
				if (readcount[ch] == 0)
					wrt[ch].release();
				mutex[ch].release();

			} else if (tc.equals("writer")) {

				System.out.println(tc);
				wrt[ch].release();

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
