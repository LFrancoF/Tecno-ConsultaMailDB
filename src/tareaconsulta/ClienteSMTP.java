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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G. Franco
 */
public class ClienteSMTP {
    private String server;
    private final int port = 25;
    private Socket socket;
    private String userReceptor;
    private String userEmisor;

    
    public ClienteSMTP(String s, String e, String r){
        this.server = s;
        this.userReceptor = r;
        this.userEmisor = e;
    }
    
    public String getReceptor(){
        return this.userReceptor;
    }
    
    public String getEmisor(){
        return this.userEmisor;
    }
    
    
    public boolean sendMail(String sender, String receiver, String subject, String body){
        String command="";
        String res="";
        try{
            this.socket = new Socket(this.server,this.port);
            BufferedReader bInput = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            DataOutputStream dOutput = new DataOutputStream (this.socket.getOutputStream());
            
            if(this.socket != null && bInput != null && dOutput != null){
                res = bInput.readLine();
                System.out.println("Server: " + res);
                
                command = "HELO " + this.server + "\r\n";
                System.out.print("Client: " + command);
                dOutput.writeBytes(command);
                res = bInput.readLine();
                System.out.println("Server: " + res);
                if (!res.contains("250"))
                    return false;

                //emisor
                command = "MAIL FROM: " + sender + "\r\n";
                System.out.print("Client: " + command);
                dOutput.writeBytes(command);
                res = bInput.readLine();
                System.out.println("Server: " + res);
                if (!res.contains("250"))
                    return false;

                //receptor
                command = "RCPT TO: " + receiver + "\r\n";
                System.out.print("Client: " + command);
                dOutput.writeBytes(command);
                res = bInput.readLine();
                System.out.println("Server: " + res);
                if (!res.contains("250"))
                    return false;

                //Escribir mensaje
                command = "DATA\r\n"; // server should expect more commands after this one
                System.out.print("Client: " + command);
                dOutput.writeBytes(command);
                res = bInput.readLine();
                System.out.println("Server: " + res);
                if (!res.contains("354"))
                    return false;
                
                //armar el mensaje y enviar
                command="SUBJECT: " + subject + "\r\n";
                command+= body + "\r\n";
                command+= ".\r\n";
                System.out.print("Client: " + command);
                dOutput.writeBytes(command);
                res = bInput.readLine();
                System.out.println("Server: " + res);
                if (!res.contains("250"))
                    return false;

                //Salir 
                command = "QUIT\r\n";
                System.out.print("Client: " + command);
                dOutput.writeBytes(command);
                System.out.println("Server: " + bInput.readLine());
            }
            this.socket.close();
            bInput.close();
            dOutput.close();
            return true;
    }catch(IOException ex){
            System.out.println(ex.toString());
            return false;
        }
    }
}
