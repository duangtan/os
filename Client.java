import java.net.*;
import java.nio.channels.*;
import java.util.Scanner;
import java.io.*;

public class Client {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private SocketChannel socketChannel;
    private final String foldercopy = "C:/os/client/copy/";  //<<<<<<<<<<<<<<<<<<<<<<<<
    private final String folderzero = "C:/os/client/zero/";  //<<<<<<<<<<<<<<<<<<<<<<<<
    String[] filename = new String[10000];
    


        
        
       

    public final void connection() {
        try {
            socket = new Socket("localhost", 5000);  //<<<<<<<<<<<<<<<<<<<<<<<<
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 5001)); //<<<<<<<<<<<<<<<<<<<<<<<<
        } catch (IOException e) {}
    }

  
    
    public final void showFile() {
        
        System.out.println(" __________________________________________");
        System.out.println("|                                          |");
        System.out.println("|             File in Server               |");
        System.out.println("|__________________________________________|");
        System.out.println("|                                          |");
        try{
             int length = dis.readInt();
             System.out.println(length);
            for (int i = 0; i < length; i++) {
                filename[i]=dis.readUTF();
                System.out.println("     File: " + filename[i]);
            } 
        }catch (IOException e) { }
        
        
        System.out.println("|                                          |");
        System.out.println("|__________________________________________|");
    }

    public final void askUser() {
        String namefile = "";
        boolean check;
        Scanner sc = new Scanner(System.in);
        
        while (true) {
            showFile();
            System.out.print("[ CLEINT ] Select File: ");
            namefile = sc.next();
            if(namefile.equalsIgnoreCase("EXIT")){
                break;
            }
            try {
                dos.writeUTF(namefile);
                check =dis.readBoolean();
                if (check==false) {
                    System.out.println("Not Found \n"); 
                    break;
                }
                System.out.print("1.Copy\n2.Zero Copy\nSelect type to copy : ");
                String type = sc.next();
                if (!type.equals("1") && !type.equals("2")) {
                    showFile();
                    namefile="";
                    check=false;
                    continue;
                }
                
                
                dos.writeUTF(type);
                long size = dis.readLong();
                String filePathcopy = foldercopy + namefile;
                String filePathzero = folderzero + namefile;
                long start = System.currentTimeMillis();
                if(type.equals("1"))
                    copy(filePathcopy, size);
                else
                    zeroCopy(filePathzero, size);
                long end = System.currentTimeMillis();
                long time = end-start;
                dos.writeLong(time);
                System.out.println("Time : "+time+" ms\n");
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input\n");
            } catch (IOException ex) {}
        }
    }
    
    public void copy(String filePath, long size) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            byte[] buffer = new byte[8*1024];
            int read;
            long currentRead = 0;
            while (currentRead < size && (read = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
                currentRead += read;
            }
            System.out.println("Copy Success");
        } catch (IOException e) { }
        finally {
            try {
                if (fos != null) 
                    fos.close();
            } catch (IOException e) {
                disconnect();
            }
        }
    }
    
    public final void zeroCopy(String filePath, long size){
        FileChannel destination = null;
        try{
            destination = new FileOutputStream(filePath).getChannel();
            long currentRead = 0;
            long read;
            while(currentRead < size && (read = destination.transferFrom(socketChannel, currentRead, size - currentRead)) != -1)
                currentRead += read;
            System.out.println("Zero Copy Success");
        } catch (IOException e){}
        finally{
            try{
                if(destination != null)
                    destination.close();
            } catch (IOException e){
                disconnect();
            }
        }
    }
    
    public void disconnect(){
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
    
    public static void main(String[] args) {
        Client client = new Client();
        client.connection();
        client.askUser();
    }


}