/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tareaconsulta;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author G. Franco
 */
public class ClientePOP3 {
    private String server;
    private String user;
    private String password;
    private final int port=110;
    private BufferedReader bInput;
    private DataOutputStream dOutput;
    private boolean Connected;
    private boolean LoggedIn;
    private Socket socket;
    
    public ClientePOP3(String s, String u, String p){
        this.server = s;
        this.user = u;
        this.password = p;
        this.Connected = false;
        this.LoggedIn = false;
    }
    
    //conecta al servidor
    public void Connect(){
        try {
            this.socket=new Socket(server,port);
            this.bInput =new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.dOutput = new DataOutputStream (this.socket.getOutputStream());
            String line = this.bInput.readLine();
            if(line.contains("OK Dovecot ready")){
                this.Connected = true;
               System.out.println("S : "+line);
            }
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }
    
    //verifica si esta conectado al servidor
    public boolean isConnected(){
        return this.Connected;
    }
    
    //verifica si esta logueado
    public boolean isLoggedIn(){
        return this.LoggedIn;
    }
    
    //Se loguea con autenticacion de user y pass
    public void LogIn(){
        if(isConnected()){
            String command="";
            boolean userok=false;
            try {
                command = "USER " + this.user + "\r\n"; 
                System.out.print("Client: " + command);
                this.dOutput.writeBytes(command);
                if(this.bInput.readLine().contains("OK"))
                    userok=true;
                
                command = "PASS " + this.password + "\r\n"; 
                System.out.print("Client: " + command);
                this.dOutput.writeBytes(command);
                if(this.bInput.readLine().contains("OK Logged in") && userok)
                    this.LoggedIn = true;
            
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
    }
    
    //cierra el ClientePOP3
    public void close(){
        if(isLoggedIn()){
            String command = "";
            try {
                command = "QUIT\r\n";
                System.out.print("Client: " + command);
                this.dOutput.writeBytes(command);
                System.out.println("Server: " + this.bInput.readLine() + "\r\n");

                //Cerrar el socket y los flujos de salida y entrada
                this.dOutput.close();
                this.bInput.close();
                this.socket.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
    }  
    
    //cuenta los correos de este usuario
    public int countMails(){
        int result=-1;
        if(isLoggedIn()){
            String command = "";
            String line="";
            try {
                command = "STAT\r\n";
                System.out.print("Client: " + command);
                this.dOutput.writeBytes(command);
                line = this.bInput.readLine();
                System.out.println("Server: " + line + "\r\n");
                int start = line.indexOf(" ")+1;
                int end = line.lastIndexOf(" ");
                result = Integer.parseInt(line.substring(start,end));
                
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return result;
    }
    
    //lee el correo nro (index)
    public String readMail(int index){
        String result = "No se encontro nada";
        String command = "";
        if (isLoggedIn()) {
            try {
                command = "RETR " + index + "\r\n";
                //System.out.print("Client: " + command);
                this.dOutput.writeBytes(command);
                result = getMultiline(this.bInput);
                //System.err.println("Server: " + result + "\r\n");
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return result;
    }
    
    //lee el primer correo de la lista
    public String readFirstMail(){
        return readMail(1);
    }
    
    //lee el ultimo correo de la lista
    public String readLastMail(){
        return readMail(countMails());
    }
    
    //elimina el correo nro (index)
    public boolean deleteMail(int index){
        String command = "";
        String line = "";
        boolean result = false;
        if (isConnected() && isLoggedIn()) {
            try {
                command = "DELE " + index + "\r\n";
                System.out.print("Client: " + command);
                this.dOutput.writeBytes(command);
                line = this.bInput.readLine();
                System.out.println("Server: " + line + "\r\n");
                if (line.contains("OK"))
                    result = true;
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        }
        return result;
    }
    
    //Permite leer multiples lineas de respuesta del Protocolo POP
    static protected String getMultiline(BufferedReader in) throws IOException{
        String lines = "";
        while (true){
            String line = in.readLine();
            if (line == null){
               // Server closed connection
               throw new IOException("Server : Server unawares closed the connection.");
            }
            if (line.equals(".")){
                // No more lines in the server response
                break;
            }
            if ((line.length() > 0) && (line.charAt(0) == '.')){
                // The line starts with a "." - strip it off.
                line = line.substring(1);
            }
            // Add read line to the list of lines
            lines = lines + "\n" + line;
        }       
        return lines;
    }
    
}
