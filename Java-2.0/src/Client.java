import java.net.*;
import java.io.*;

public class Client {

	public static void main(String[] args) throws Exception {

		String ip = "127.0.0.1";
		if(args.length > 0) {
			ip = args[0];
		}
		
		try {
			
			Socket socket = new Socket(ip, 8888);
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Enter Username");
			String uname = br.readLine();
			outStream.writeUTF(uname);
			outStream.flush();
			System.out.println("Enter Password");
			String passw = br.readLine();
			outStream.writeUTF(passw);
			outStream.flush();
		
			int st = inStream.readInt();
			if(st == 0) {
				System.out.println("Invalid Username or Password");
				System.exit(0);
			}
			
			String clM = "", srM = "";
			StringBuilder txt = new StringBuilder("");
			
			System.out.println("Enter Directory number\n1. Dir1\t2. Dir2");
			int d = Integer.parseInt(br.readLine());
			outStream.writeInt(d);
			outStream.flush();
			
			System.out.println("Enter File number\n1. File1\t2. File2");
			int f = Integer.parseInt(br.readLine());
			outStream.writeInt(f);
			outStream.flush();
			
			System.out.println("Reader/writer :");

			while (!clM.equals("exit")) {

				clM = br.readLine();
				outStream.writeUTF(clM);
				outStream.flush();

				if (clM.equals("reader")) {

					System.out.println("Busy..!");
					srM = inStream.readUTF();
					System.out.println(srM);
					srM = inStream.readUTF();
					System.out.println(srM);
					srM = inStream.readUTF();
					System.out.println(srM);

				} else if (clM.equals("writer")) {

					System.out.println("Busy..!");
					srM = inStream.readUTF();
					System.out.println(srM);
					System.out.println("Enter Text ( enter * in new line to send )");
					clM = br.readLine();

					while (!(clM.equals("*"))) {

						txt.append(clM);
						txt.append("\n");
						clM = br.readLine();

					}

					clM = txt.toString();
					outStream.writeUTF(clM);
					outStream.flush();

				} else {

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
