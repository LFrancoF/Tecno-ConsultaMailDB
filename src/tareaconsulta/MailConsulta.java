/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tareaconsulta;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G. Franco
 */
public class MailConsulta {
    
    public class Consulta {

        private String user;
        private String pattern;
        private final String query = "SELECT * FROM persona WHERE per_nom LIKE ";
        private String result;
        
        //constructor para crear consultas con el usuario y un patron que sera el parametro de las consultas
        public Consulta(String user, String pattern) {
            this.user = user;
            this.pattern = pattern;
        }
        
        public String getUser(){
            return this.user;
        }
        
        public String getQuery(){
            return this.query;
        }
        
        public String getPattern(){
            return this.pattern;
        }
        
        public void setResult(String result){
            this.result = result;
        }
        
        public String getResult(){
            return this.result;
        }
    }
    
    public List<Consulta> getQuerysList(ClientePOP3 cpop3){
        ArrayList<Consulta> listQuerys = new ArrayList<>();
        
        int cantMails = cpop3.countMails();
        String currentMail ="";
        String sender = "";
        String pattern="";
        for (int i = 1; i <= cantMails; i++) {
            currentMail = cpop3.readMail(i);
            sender = getSender(currentMail);
            pattern = getPattern(currentMail);
            if (!pattern.contains("No subject then no pattern")){
                Consulta newC = new Consulta(sender,pattern);
                listQuerys.add(newC);
                
                if (sender.contains("grupo05sa@tecnoweb.org.bo")){
                    boolean delet = cpop3.deleteMail(i);
                    if (delet)  System.out.println("Se elimino el correo nro " + i);
                }
                else 
                    System.out.println("Error al eliminar el correo nro " + i);
            }
        }
        return listQuerys;
    }
    
    public String getSender(String mail){
        int senderIndex = mail.indexOf("Return-Path:");
        String senderLine = mail.substring(senderIndex).split("\\R")[0].trim();
        int indexStart = senderLine.indexOf(":")+1;
        String sender = senderLine.substring(indexStart).replace("<", "").replace(">", "").trim();
        return sender;
    }
    
    public String getPattern(String mail){
        int subjectIndex = mail.indexOf("SUBJECT:");
        if (subjectIndex < 0){
            subjectIndex = mail.indexOf("Subject:");
            if (subjectIndex < 0)
                return "No subject then no pattern";
        }
        String subjectLine = mail.substring(subjectIndex).split("\\R")[0].trim();
        int indexStart = subjectLine.indexOf(":")+1;
        String pattern = subjectLine.substring(indexStart).trim();
        pattern = "'%" + pattern + "%'";
        return pattern;
    }
    
    public void processQuerys(List<Consulta> listQuerys, ClientePostgre clientePg){
        for (int i = 0; i < listQuerys.size(); i++) {
            String sql = listQuerys.get(i).getQuery() + listQuerys.get(i).getPattern();
            String result = clientePg.executeQuery(sql);
            listQuerys.get(i).setResult(result);
        }
        clientePg.closeConnection();
    }
    
    public void responseQuerys(List<Consulta> listQuerys, ClienteSMTP csmtp){
        boolean sent;
        String emisor = csmtp.getReceptor();        //en las respuestas, ahora el emisor es el correo del servidor tecnoweb, guardados como receptor en el cliente SMTP
        for (int i = 0; i < listQuerys.size(); i++) {
            String receiver = listQuerys.get(i).getUser();      //guardamos los correos de cada consulta de la lista a los que enviaremos sus resultados
            
            //solo enviare las respuestas a mi (correo = grupo05sa), por ahora, para no molestar a otros xD
            if (receiver.contains("grupo05sa@tecnoweb.org.bo")){
                String result = listQuerys.get(i).getResult();      //guardamos los resultados de cada consulta de la lista
                sent = csmtp.sendMail(emisor, receiver, "Resultado de su Consulta", result);
                if(!sent)
                    break;
                else
                    System.out.println("Se envio el correo con el resultado n.n");
            }
        }
    }
    
    public void run() throws InterruptedException, IOException{
        ClientePOP3 cPOP3 = new ClientePOP3("mail.tecnoweb.org.bo", "grupo07sa", "grup007grup007");
        ClienteSMTP cSMTP = new ClienteSMTP("mail.tecnoweb.org.bo", "grupo05sa@tecnoweb.org.bo", "grupo07sa@tecnoweb.org.bo");
        ClientePostgre cPostgre = new ClientePostgre("mail.tecnoweb.org.bo","5432","db_agenda","agenda","agendaagenda");
        // creamos un ciclo infinito que se ejecutara cada 5 segundos
        while (true) {
            // conectamos los clientes
            cPostgre.connect();
            cPOP3.Connect();
            cPOP3.LogIn();
            if (cPOP3.isLoggedIn()){
                // buscar consultas entre los correos disponibles
                System.out.println("Buscando consultas en correos...");
                List<Consulta> listQuerys = getQuerysList(cPOP3);
                if (listQuerys.size() > 0) {
                    System.out.println("Se encontro "+ listQuerys.size()+ " consultas...");
                    processQuerys(listQuerys, cPostgre);
                    responseQuerys(listQuerys, cSMTP);
                    System.out.println("Finalizado exitosamente.");
                } else {
                    System.out.println("No se encontraron consultas.");
                }
                cPOP3.close();
                cPostgre.closeConnection();
                sleep(5000);
            }else{
                System.out.println("No se ha iniciado sesion");
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MailConsulta c = new MailConsulta();
        try {
            c.run();
        } catch (InterruptedException ex) {
            Logger.getLogger(MailConsulta.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MailConsulta.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
