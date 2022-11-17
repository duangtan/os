import java.io.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.jar.Attributes.Name;
import java.net.*;

/**
 * server
 */
public class Server {
    public static int numclient;
    private ServerSocket serverSocket;
    private ServerSocketChannel serverChannel;
    static File folder = new File("C:/os/server/");   //<<<<<<<<<<<<<<<<<<<<<<<<
    private final File[] listOfFiles = folder.listFiles();
    String[] filename = new String[10000];
    
    
    public final void connection(){
        try {
            serverSocket = new ServerSocket(5000);  //<<<<<<<<<<<<<<<<<<<<<<<<
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(5001)); //<<<<<<<<<<<<<<<<<<<<<<<<
            
            System.out.println("[ Wait for Client join the Server ]");
            while (true) {
                Socket socketClient = serverSocket.accept();
                DataInputStream dis = new DataInputStream(socketClient.getInputStream());
                DataOutputStream dos = new DataOutputStream(socketClient.getOutputStream());
                SocketChannel socketChannel = serverChannel.accept();
                System.out.println("============================================");
                System.out.println("|           New Client Connected           |");
                System.out.println("|               Client No: " + (numclient+1)+ "               |");
                System.out.println("============================================");
                new Thread(new ClientHandle(socketClient, dis,  dos, socketChannel, ++numclient)).start();
            }
        } catch (IOException e) {}
    }

    class ClientHandle implements Runnable {

        private final int clientNo;
        private final Socket socket;
        private final DataInputStream dis;
        private final DataOutputStream dos;
        private final SocketChannel socketChannel;

        public ClientHandle(Socket socket, DataInputStream dis, DataOutputStream dos, SocketChannel socketChannel, int clientNo) {
            this.socket = socket;
            this.dis = dis;
            this.dos = dos;
            this.socketChannel = socketChannel;
            this.clientNo = clientNo;
            
        }
        void sendList(){
            try{
                dos.writeInt(listOfFiles.length);
                for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    filename[i] = listOfFiles[i].getName();
                    dos.writeUTF(filename[i] );
                    dos.flush();;
                }
            } 
            }catch (IOException e) { }
            
        }

        @Override
        public void run() {
            sendList();  
            try {
                boolean check = false;
                while (true) {
                    String namefile = dis.readUTF();
                    String filePath = "C:/os/server/"+namefile; //<<<<<<<<<<<<<<<<<<<<<<<<
                    File file = new File(filePath);
                    for (int i = 0; i < listOfFiles.length; ++i) {
                        if (namefile.equalsIgnoreCase(filename[i])) {
                            System.out.println("[ SERVER ] Send File List ");
                            check = true;      
                        }     
                    }dos.writeBoolean(check);
                    dos.flush();

                    String type = dis.readUTF();
                    long size = file.length();
                    dos.writeLong(size);
                    System.out.print("[ Client " + clientNo + " ] Select File name: "+ namefile);
                    if(type.equals("1")){
                        System.out.println(" : copy file");
                    }else if(type.equals("2")){
                        System.out.println(" : zero copy file");
                    }
                    
                    if(type.equals("1")){
                        copy(filePath, size,namefile);
                    }
                    else if(type.equals("2")){
                        zeroCopy(filePath, size,namefile);
                    }
                    long time=dis.readLong();
                    System.out.println("Time : "+time+" ms\n");
                }
            } catch (IOException ex) {
                disconnect();
            }
        }
 
        public void copy(String filePath, long size,String namefile) {
            FileInputStream readfile = null;
            try {
                readfile = new FileInputStream(filePath);
                byte[] buffer = new byte[8*1024];
                int read;
                long currentRead = 0;
                while (currentRead < size && (read = readfile.read(buffer)) != -1) {
                    dos.write(buffer, 0, read);
                    currentRead += read;
                }
                System.out.println("[ SERVER ] Send File:  " + namefile +" : copy file "+ " Successful!!");
            } 
            catch (IOException e) {} 
            finally {
                try {
                    if (readfile != null)
                        readfile.close();
                        
                } catch (IOException e) {
                    disconnect();
                }
            }
        }
        
        public void zeroCopy(String filePath, long size,String namefile){
            FileChannel source = null;
            try{
                source = new FileInputStream(filePath).getChannel();
                long currentRead = 0;
                long read;
                while(currentRead < size && (read = source.transferTo(currentRead, size - currentRead, socketChannel)) != -1){
                    currentRead += read;
                }               
                System.out.println("[ SERVER ] Send File:  " + namefile +" : zero copy "+ " Successful!!");
            }
            catch (IOException e){}
            finally{
                try{
                    if(source != null)
                        source.close();
                        
                } catch (IOException e){
                    disconnect();
                }
            }
        }
        
        public void disconnect(){
            System.out.println("[ SERVER ] Client: " + clientNo + " Disconnected!\n");
            try{
                if(dis != null)
                    dis.close();
                if(dos != null)
                    dos.close();
                if(socket != null)
                    socket.close();
                if(socketChannel != null)
                    socketChannel.close();
            } catch (IOException e){ }
        }
    }

    public static void main(String[] args) throws IOException {
       Server server = new Server();
       server.connection();
    }  
}

