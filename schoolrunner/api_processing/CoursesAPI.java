package schoolrunner.api_processing;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.sql.*;

public class CoursesAPI {
   
   //defdault API endpoint url
   private String endpoint = "https://renew.schoolrunner.org/api/v1/courses/?limit=30000";
   
   private String username;
   private String password;
   
   //variable for name of database 
   private String dbName;
   private String tableName;
   
   //variable for counting how many records from the API have been processed
   private int recordsProcessed;
   
   //constructor that takes the username and password
   //which will be passed to the ConnectToSRAPI constructor
   //constructor also requires the names of database and tables being used
   public CoursesAPI(String username, String password, String dbName, String tableName) {
      this.username = username;
      this.password = password;
      this.dbName = dbName;
      this.tableName = tableName;
   }

   //alternate constructor that also accepts a String endpoint
   //useful incase an endpoint url with parameters should be used instead of the default
   public CoursesAPI(String username, String password, String dbName, String tableName, String endpoint) {
      this.username = username;
      this.password = password;
      this.dbName = dbName;
      this.tableName = tableName;
      this.endpoint = endpoint;
   }

   public void run() {
      
      Connection c = null;
      PreparedStatement stmt = null;
	
	   try {
	      
	      //create a new connection to SR API
         ConnectToSRAPI connection = new ConnectToSRAPI(endpoint, username, password);
      
         String rawData = connection.run();
         
         //connect to local SR database
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:" + this.dbName);
         c.setAutoCommit(false);
         System.out.println("opened database successfully");
         
         JSONParser jsonParser = new JSONParser();
         JSONObject jsonObject = (JSONObject) jsonParser.parse(rawData);
         
         //drill down from raw data to the "result" stuff
         JSONObject results = (JSONObject) jsonObject.get("results");
         
         //drill down from "result" to the actual stuff we want (which is a JSON array)
         JSONArray innerJsonArray = (JSONArray) results.get("courses");
         
         //loop through the JSON array and save each record to the appropriate table in the SR database
         for (int i = 0; i < innerJsonArray.size(); i++) {
            JSONObject innerJsonObj = (JSONObject) innerJsonArray.get(i);
            
            //save data from innerJsonOnj to temporary String variables that will be use in a SQL insert statement
            String courseID = ((String) innerJsonObj.get("course_id"));
            String courseName = ((String) innerJsonObj.get("name"));
            String SRSchoolID = ((String) innerJsonObj.get("school_id"));
            String displayName = ((String) innerJsonObj.get("display_name"));
            String active = ((String) innerJsonObj.get("active"));
            
            //PreparedStatement provides security from SQL injection attack, allows text to contain single quotes (like some names do)
            stmt = c.prepareStatement("INSERT INTO " + this.tableName +
               " (COURSE_ID, COURSE_NAME, SR_SCHOOL_ID, DISPLAY_NAME, ACTIVE) " +
               "VALUES (?, ?, ?, ?, ?)");
               
            stmt.setString(1, courseID);
            stmt.setString(2, courseName);
            stmt.setString(3, SRSchoolID);
            stmt.setString(4, displayName);
            stmt.setString(5, active);
            
            //execute the insert statement
            stmt.executeUpdate();
            
            recordsProcessed++;
         }
      
         stmt.close();
         c.commit();
         c.close();
      
	   } catch (ParseException ex) {
            ex.printStackTrace();
	         
	   } catch (Exception e) {
	         System.err.println(e.getClass().getName() + ": " + e.getMessage());
	   }
	   
	   //print confirmation message
	   System.out.printf("%s%s%d.%n%n", this.tableName, " data processing complete, total number of records: ", recordsProcessed);
		
   } //end run method
   
} //end class