package schoolrunner.api_processing;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.sql.*;

public class AssessmentsAPI {
   
   //defdault API endpoint url, includes extra parameters for active and min_date
   private String endpoint = "https://renew.schoolrunner.org/api/v1/assessments/?limit=30000&active=1&min_date=2015-07-22";
   
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
   public AssessmentsAPI(String username, String password, String dbName, String tableName) {
      this.username = username;
      this.password = password;
      this.dbName = dbName;
      this.tableName = tableName;
   }

   //alternate constructor that also accepts a String endpoint
   //useful incase an endpoint url with parameters should be used instead of the default
   public AssessmentsAPI(String username, String password, String dbName, String tableName, String endpoint) {
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
         JSONArray innerJsonArray = (JSONArray) results.get("assessments");
         
         //loop through the JSON array and save each record to the appropriate table in the SR database
         for (int i = 0; i < innerJsonArray.size(); i++) {
            JSONObject innerJsonObj = (JSONObject) innerJsonArray.get(i);
            
            //save data from innerJsonOnj to temporary String variables that will be use in a SQL insert statement
            String assessmentID = ((String) innerJsonObj.get("assessment_id"));
            String SRSchoolID = ((String) innerJsonObj.get("school_id"));
            String SRStaffMemberID = ((String) innerJsonObj.get("staff_member_id"));
            String courseID = ((String) innerJsonObj.get("course_id"));
            String assessmentDate = ((String) innerJsonObj.get("date"));
            String assessmentName = ((String) innerJsonObj.get("name"));
            String assessmentTypeID = ((String) innerJsonObj.get("assessment_type_id"));
            String avgScore = ((String) innerJsonObj.get("avg_score"));
            String presentStudents = ((String) innerJsonObj.get("present_students"));
            String enrolledStudents = ((String) innerJsonObj.get("enrolled_students"));
            String active = ((String) innerJsonObj.get("active"));
            
            //PreparedStatement provides security from SQL injection attack, allows text to contain single quotes (like some names do)
            stmt = c.prepareStatement("INSERT INTO " + this.tableName +
               " (ASSESSMENT_ID, SR_SCHOOL_ID, SR_STAFF_MEMBER_ID, COURSE_ID, ASSESSMENT_DATE, ASSESSMENT_NAME, ASSESSMENT_TYPE_ID, " +
               "AVG_SCORE, PRESENT_STUDENTS, ENROLLED_STUDENTS, ACTIVE) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
               
            stmt.setString(1, assessmentID);
            stmt.setString(2, SRSchoolID);
            stmt.setString(3, SRStaffMemberID);
            stmt.setString(4, courseID);
            stmt.setString(5, assessmentDate);
            stmt.setString(6, assessmentName);
            stmt.setString(7, assessmentTypeID);
            stmt.setString(8, avgScore);
            stmt.setString(9, presentStudents);
            stmt.setString(10, enrolledStudents);
            stmt.setString(11, active);
            
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