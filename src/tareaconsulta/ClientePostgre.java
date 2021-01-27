package tareaconsulta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientePostgre {
   private static final String DRIVER = "jdbc:postgresql://";
   
   private Connection connection;
   private String user;
   private String pass;
   private String driver;
   
   public ClientePostgre(String host, String port, String name, String user, String pass){
       this.driver = DRIVER + host + ":" + port + "/" + name;
       this.user = user;
       this.pass = pass;
   }
   
   public Connection connect(){
       Connection con = null;
       try{
           con = DriverManager.getConnection(driver, user, pass);
           this.connection = con;
       }catch(SQLException e){
           System.out.println("Error al conectar a la base de datos: " + e.toString());
       }
       return con;
   }
   
   public String executeQuery(String sql){
        String result = "";
        if(this.connection == null)
            result = "No hay conexion a la base de datos.";
        else{
            try{
                Statement st = this.connection.createStatement();
                ResultSet rs = st.executeQuery(sql);
                int colcount = rs.getMetaData().getColumnCount();
                while(rs.next()){
                    for(int i = 1; i <= colcount; i++){
                        result += "|" + rs.getString(i).trim() + "||";
                    }
                    result += "\r\n";
                }
            }catch(SQLException e){
                result = "Error: " + e.toString();
            }
        }
        return result;
    }
   
    public void closeConnection(){
        this.connection = null;
   }
}
