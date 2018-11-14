import java.net.*; 
import java.io.*; 
import java.util.*; 
public class GroupChat 
{ 
	private static final String TERMINATE = "Exit"; 
	static String name; 
	static volatile boolean finished = false; 
	public static void main(String[] args) 
	{ 
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter your ip address: ");
		String ip = sc.nextLine();
		String p = "1234";
		if (false) 
			System.out.println("Two arguments required: <multicast-host> <port-number>"); 
		else
		{ 
			try
			{ 
				InetAddress group = InetAddress.getByName(ip); 
				int port = Integer.parseInt(p);  
				System.out.print("Enter your name: "); 
				name = sc.nextLine(); 
				MulticastSocket socket = new MulticastSocket(port); 
			
				// Since we are deploying this on localhost only (For a subnet set it as 1)
				socket.setTimeToLive(0);  
				socket.joinGroup(group); 
				Thread t = new Thread(new
				ReadThread(socket,group,port)); 
			
				// Spawn a thread for reading messages 
				t.start(); 
				
				// sent to the current group 
				System.out.println("Start typing messages...\n");
				String joinMessage = "\n\t" + name + " joined the chat\n";
				byte[] joinBuffer = joinMessage.getBytes(); 
				DatagramPacket joinDatagram = new
				DatagramPacket(joinBuffer,joinBuffer.length,group,port); 
				socket.send(joinDatagram);
				while(true) 
				{ 
					String message; 
					message = sc.nextLine(); 
					if(message.equalsIgnoreCase(GroupChat.TERMINATE)) 
					{ 
						finished = true; 
						socket.leaveGroup(group); 
						socket.close(); 
						break; 
					} 
					message = name + ": " + message; 
					System.out.println(message);
					byte[] buffer = message.getBytes(); 
					DatagramPacket datagram = new
					DatagramPacket(buffer,buffer.length,group,port); 
					socket.send(datagram); 
				} 
			} 
			catch(SocketException se) 
			{ 
				System.out.println("Error creating socket"); 
				se.printStackTrace(); 
			} 
			catch(IOException ie) 
			{ 
				System.out.println("Error reading/writing from/to socket"); 
				ie.printStackTrace(); 
			} 
		} 
	} 
} 
class ReadThread implements Runnable 
{ 
	private MulticastSocket socket; 
	private InetAddress group; 
	private int port; 
	private static final int MAX_LEN = 1000; 
	ReadThread(MulticastSocket socket,InetAddress group,int port) 
	{ 
		this.socket = socket; 
		this.group = group; 
		this.port = port; 
	} 
	
	@Override
	public void run() 
	{ 
		while(!GroupChat.finished) 
		{ 
				byte[] buffer = new byte[ReadThread.MAX_LEN]; 
				DatagramPacket datagram = new
				DatagramPacket(buffer,buffer.length,group,port); 
				String message; 
			try
			{ 
				socket.receive(datagram); 
				message = new
				String(buffer,0,datagram.getLength(),"UTF-8"); 
				if(!message.startsWith(GroupChat.name)) 
					System.out.println(message); 
			} 
			catch(IOException e) 
			{ 
				System.out.println("Socket closed!"); 
			} 
		} 
	} 
}
