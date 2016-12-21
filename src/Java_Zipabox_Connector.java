
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.security.*;
import java.io.*;

import java.sql.*;


  public class Java_Zipabox_Connector {

  static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://raspberry:3306/zipato";
   static final String USER = "";  // mysql username to be filled
   static final String PASS = "";  // mysql passwod
   static String Username=""; // myzipato username to be filled
   static String Password=""; //myzipato password
   static Boolean Debug=false;
   
  public static void main(String [] args )
			  throws Exception
			  {
			    new Java_Zipabox_Connector();
			  }
		   
  public Java_Zipabox_Connector() throws Exception
  {
	  System.out.println("Java Connector Zipabox");
	  
	  if(!Init_Connection_Zipabox(Username,Password))
		  System.out.println("Problem connection with Zipabox API");
	  
	  	  
  }
  
  
  
  public void JavaDBConnection(String sql,String Date, String Value) 
  {
	  
  Connection conn = null;
  Statement stmt = null;
  
  try{
     //STEP 2: Register JDBC driver
     Class.forName("com.mysql.jdbc.Driver");

     //STEP 3: Open a connection
     
     System.out.println("Now Connecting to MySQL Zipato DB...");
     conn = DriverManager.getConnection(DB_URL,USER,PASS);
          
     //STEP 4: Execute a query
     stmt = conn.createStatement();
     // String sql;
     
     // sql="INSERT INTO temperature_jardin (temperature) VALUES ("+Value+")";
     System.out.println(sql);
     
     stmt.executeUpdate(sql);
     
     //STEP 6: Clean-up environment
     stmt.close();
     conn.close();
  }catch(SQLException se){
     //Handle errors for JDBC
     se.printStackTrace();
  }catch(Exception e){
     //Handle errors for Class.forName
     e.printStackTrace();
  }finally{
     //finally block used to close resources
     try{
        if(stmt!=null)
           stmt.close();
     }catch(SQLException se2){
     }// nothing we can do
     try{
        if(conn!=null)
           conn.close();
     }catch(SQLException se){
        se.printStackTrace();
     }//end finally try
  }//end try
  
  System.out.println("Goodbye!");
  
}//end main  
  private Boolean Init_Connection_Zipabox(String Username, String Password) throws NoSuchAlgorithmException
  {
	  String jSessionId="";
	  String Nonce="";
	  String token="";
	  String sha1="";
      
      try {
    	
    	String results = doHttpUrlConnectionAction("https://my.zipato.com:443/zipato-web/v2/user/init",jSessionId);
	       
		jSessionId=Parse_Json_Object(results,"jsessionid");
		
		Nonce=Parse_Json_Object(results,"nonce");
		
		sha1= Build_Sha1(Password);
		
		sha1=sha1.toLowerCase();
		
		token= Build_Sha1(Nonce+sha1);
		
		token=token.toLowerCase();
		
		results = doHttpUrlConnectionAction("https://my.zipato.com/zipato-web/v2/user/login?username="+Username+"&token="+token,jSessionId);
		
		/*System.out.println(results);*/
		
		if (results.contains("true"))
		{		
			System.out.println("Welcome "+Username+", you are connected to Zipato API V2 !");
			System.out.println("");
			
			results = doHttpUrlConnectionAction("https://my.zipato.com:443/zipato-web/v2/attributes/full?network=false&device=false&endpoint=true&clusterEndpoint=false&definition=false&config=false&room=false&icons=true&value=true&parent=false&children=false&full=false&type=false",jSessionId);
			
			// System.out.println(results);
						
			Parse_Json_Array_Connect_DB(results,"value");
			
			return(true);
		}
		else
			return(false);
		
      } catch (Exception e) {
		e.printStackTrace();
      	}
	  
	  return(false);	  
  }
  
    
  private void Parse_Json_Array_Connect_DB(String Json_String,String GetKey) throws Exception 
  {
	
	  JSONParser parser = new JSONParser();
	  
	  String sql;

	try {
		
		JSONArray jsonArray= (JSONArray) parser.parse(Json_String);
		
		 Iterator itr= jsonArray.iterator();
		 
		 while(itr.hasNext()){
		 
             JSONObject featureJsonObj = (JSONObject)itr.next();
             
             // 
             JSONObject endpoint = (JSONObject)featureJsonObj.get("endpoint");
             
             if ((endpoint.get("uuid").toString().equals("fcc6a868-8e30-476a-9e8e-6f6e1193d1f1")) && (featureJsonObj.get("attributeId").toString().equals("96")))  
             {
             	// System.out.println("Uuid="+(String)endpoint.get("uuid")+", Name :"+(String)endpoint.get("name"));
             	JSONObject value = (JSONObject)featureJsonObj.get("value");
             	
             	// System.out.println("Get endpoint Value Object : "+value.toString());
             	String timestamp=Parse_Json_Object(value.toString(),"timestamp");
    			System.out.println("Date :"+timestamp);
    			String Value=Parse_Json_Object(value.toString(),"value");
    			System.out.println("Humidite Jardin : "+Value);
    			sql="INSERT INTO humidite_jardin (humidite) VALUES ("+Value+")";
    			JavaDBConnection(sql,timestamp,Value);  
             }
             
             if ((endpoint.get("uuid").toString().equals("fcc6a868-8e30-476a-9e8e-6f6e1193d1f1")) && (featureJsonObj.get("attributeId").toString().equals("95")))  
             {
             	// System.out.println("Uuid="+(String)endpoint.get("uuid")+", Name :"+(String)endpoint.get("name"));
             	JSONObject value = (JSONObject)featureJsonObj.get("value");           	
            
             	String timestamp=Parse_Json_Object(value.toString(),"timestamp");
    			System.out.println("Date :"+timestamp);
    			String Value=Parse_Json_Object(value.toString(),"value");
    			System.out.println("Temperature Jardin : "+Value);    			
    			sql="INSERT INTO temperature_jardin (temperature) VALUES ("+Value+")";    			
    			JavaDBConnection(sql,timestamp,Value);    			
             }
             
             if ((endpoint.get("uuid").toString().equals("120a71ce-21d0-4ca6-8bb5-ba749b5b4428")))  
             {
             	// System.out.println("Uuid="+(String)endpoint.get("uuid")+", Name :"+(String)endpoint.get("name"));
             	JSONObject value = (JSONObject)featureJsonObj.get("value");           	
            
             	String timestamp=Parse_Json_Object(value.toString(),"timestamp");
    			System.out.println("Date :"+timestamp);
    			String Value=Parse_Json_Object(value.toString(),"value");
    			System.out.println("Temperature Salon : "+Value);    			
    			sql="INSERT INTO temperature_salon (temperature) VALUES ("+Value+")";    			
    			JavaDBConnection(sql,timestamp,Value);    			
             }
             if ((endpoint.get("uuid").toString().equals("2ba34d82-b9b8-4ffc-80dc-8e2e5b334c49")))  
             {
             	// System.out.println("Uuid="+(String)endpoint.get("uuid")+", Name :"+(String)endpoint.get("name"));
             	JSONObject value = (JSONObject)featureJsonObj.get("value");           	
            
             	String timestamp=Parse_Json_Object(value.toString(),"timestamp");
    			System.out.println("Date :"+timestamp);
    			String Value=Parse_Json_Object(value.toString(),"value");
    			System.out.println("Index instantané : "+Value);    			
    			sql="INSERT INTO compteur_edf (index_compteur) VALUES ("+Value+")";
    			JavaDBConnection(sql,timestamp,Value);    			
             }
                
		 }
		
		
		
	
	} catch (ParseException e) {
		e.printStackTrace();
	}
  }
   
  /**
   * Returns the output from the given URL.
   * 
   * @param desiredUrl
   * @return
   * @throws Exception
   */
  private String doHttpUrlConnectionAction(String desiredUrl, String J_SESSION_ID)
  throws Exception
  {
    URL url = null;
    BufferedReader reader = null;
    StringBuilder stringBuilder;
 
    try
    {
      // create the HttpURLConnection
      url = new URL(desiredUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
      // connection.setRequestProperty("Cookie","jsessionid =" + J_SESSION_ID);
      // just want to do an HTTP GET here
      connection.setRequestMethod("GET");
       
      // uncomment this if you want to write output to this url
      //connection.setDoOutput(true);
       
      // give it 15 seconds to respond
      connection.setReadTimeout(15*1000);
      
      connection.setRequestProperty("Cookie","JSESSIONID=" + J_SESSION_ID);
                  
      connection.connect();
      
      if(connection.getResponseCode()!=200)
      {
    	  System.out.println("Problem Connection Http !!!");
      }
 
      // read the output from the server
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      stringBuilder = new StringBuilder();
 
      String line = null;
      while ((line = reader.readLine()) != null)
      {
        stringBuilder.append(line + "\n");
      }
      return stringBuilder.toString();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw e;
    }
    finally
    {
      // close the reader; this can throw an exception too, so
      // wrap it in another try/catch block.
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (IOException ioe)
        {
          ioe.printStackTrace();
        }
      }
    }
  }
  
  private String Parse_Json_Object(String Json_Object,String GetKey) throws Exception 
  {
	  
	  String Key="";
	  String Key_Value="";
	  
	  JSONParser parser = new JSONParser();
	  
	  ContainerFactory containerFactory = new ContainerFactory(){
	    public List creatArrayContainer() {
	      return new LinkedList();
	    }

	    public Map createObjectContainer() {
	      return new LinkedHashMap();
	    }
	                        
	  };	  
	               
	  try{
		  
		 	    
		Map json = (Map)parser.parse(Json_Object, containerFactory);
		
		
		
	    
	    Iterator iter = json.entrySet().iterator();	    
	    
	    while(iter.hasNext()){
	      Map.Entry entry = (Map.Entry)iter.next();
	     // System.out.println(entry.getKey() + "=" + entry.getValue());
	      
	      Key=(String) entry.getKey();
	      
	      if (Key.equals(GetKey))
	      {
	    	    Key_Value=(String) entry.getValue();
	    		
	    	  //     System.out.println(Key_Value);
	      }
	      	      	 	      
	    }
	
	    
	  }
	  catch(ParseException pe){
	    System.out.println(pe);
	  }	  
	  
	return (Key_Value);  
  }
 
  private String Build_Sha1(String input) throws NoSuchAlgorithmException
  {
	  String output_string;
	  
	  MessageDigest md = MessageDigest.getInstance("SHA1");
	  	  
	  md.update(input.getBytes()); 

	  byte[] output = md.digest();

	  // System.out.print("SHA1(\""+input+"\") =");
	  
	  output_string=bytesToHex(output);
	  
	  // System.out.println("   "+bytesToHex(output));
	  
	  return (output_string);

  }

    
  
  public static String bytesToHex(byte[] b) {

      char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',

                         '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

      StringBuffer buf = new StringBuffer();

      for (int j=0; j<b.length; j++) {

         buf.append(hexDigit[(b[j] >> 4) & 0x0f]);

         buf.append(hexDigit[b[j] & 0x0f]);

      }

      return buf.toString();

   }
  
  
}
