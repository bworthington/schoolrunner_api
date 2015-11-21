import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;
import schoolrunner.api_processing.*;

//returns a list of assessments not aligned to objectives
public class AssessmentsWithoutObjectives {
  
  public static void main(String args[]) {
    
    //file path for database
    String dbName = "/home/ubuntu/workspace/databases/ASSESSMENTS_WITHOUT_OBJECTIVES.db";
    
    //file path for output .csv to be saved
    String fileName = "/home/ubuntu/workspace/output/Assessments_Without_Objectives.csv";
    
    String assessmentEndpoint = "https://renew.schoolrunner.org/api/v1/assessments/?limit=30000&active=1&min_date=2015-07-22&has_objectives=0";
    
    DatabaseSetup database = new DatabaseSetup();
    database.createDatabase(dbName);
    database.createAssessmentsTable(assessmentEndpoint);
    database.createSchoolsTable();
    database.createCoursesTable();
    database.createStaffTable();
    
    Connection c = null;
    Statement stmt = null;
    
    try {
      
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:" + dbName);
      c.setAutoCommit(false);
      System.out.println("opened database successfully");
      
      String query = (
        "select " +
            "SCHOOLS.DISPLAY_NAME, " +
            "ifnull(COURSES.COURSE_NAME, '**NO COURSE**'), " +
            "ifnull(STAFF.DISPLAY_NAME, '**NO STAFF**'), " +
            "ifnull(STAFF.EMAIL, '**NO STAFF EMAIL**'), " +
            "ASSESSMENTS.ASSESSMENT_NAME, " +
            "ASSESSMENTS.ASSESSMENT_DATE, " +
            "ASSESSMENTS.PRESENT_STUDENTS, " +
            "ASSESSMENTS.ENROLLED_STUDENTS, " +
            "('https://renew.schoolrunner.org/assessment/edit/' || ASSESSMENTS.ASSESSMENT_ID) " +
        "from ASSESSMENTS " +
            "left outer join COURSES on ASSESSMENTS.COURSE_ID = COURSES.COURSE_ID " +
            "left outer join STAFF on ASSESSMENTS.SR_STAFF_MEMBER_ID = STAFF.SR_STAFF_MEMBER_ID " +
            "left outer join SCHOOLS on ASSESSMENTS.SR_SCHOOL_ID = SCHOOLS.SR_SCHOOL_ID " +
        //"where ASSESSMENTS.SR_SCHOOL_ID != '5' " +
        "order by SCHOOLS.DISPLAY_NAME, COURSES.COURSE_NAME, STAFF.DISPLAY_NAME, ASSESSMENTS.ASSESSMENT_DATE; ");

      stmt = c.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      
      //new filewriter object for saving the data
      FileWriter writer = new FileWriter(fileName);
      
      writer.append("SCHOOL");
	    writer.append(',');
	    writer.append("COURSE_NAME");
	    writer.append(',');
	    writer.append("STAFF");
	    writer.append(',');
	    writer.append("STAFF_EMAIL");
	    writer.append(',');
	    writer.append("ASSESSMENT_NAME");
	    writer.append(',');
	    writer.append("ASSESSMENT_DATE");
	    writer.append(',');
	    writer.append("STUDENTS_ASSESSED");
	    writer.append(',');
	    writer.append("STUDENTS_ENROLLED");
	    writer.append(',');
	    writer.append("EDIT_URL");
	    writer.append('\n');
      
      while (rs.next()) {
          
          //surrounding quotes "\"" used to ensure data that my contain commas don't
          //act as delimiters
          
          writer.append("\"" + rs.getString(1) + "\"");
    	    writer.append(',');
    	    writer.append("\"" + rs.getString(2) + "\"");
    	    writer.append(',');
    	    writer.append("\"" + rs.getString(3) + "\"");
    	    writer.append(',');
    	    writer.append("\"" + rs.getString(4) + "\"");
    	    writer.append(',');
    	    writer.append("\"" + rs.getString(5) + "\"");
    	    writer.append(',');
    	    writer.append("\"" + rs.getString(6) + "\"");
    	    writer.append(',');
    	    writer.append("\"" + rs.getString(7) + "\"");
    	    writer.append(',');
    	    writer.append("\"" + rs.getString(8) + "\"");
    	    writer.append(',');
    	    writer.append("\"" + rs.getString(9) + "\"");
    	    writer.append('\n');
      }
      
      rs.close();
      stmt.close();
      c.close();

	    writer.flush();
	    writer.close();
	    System.out.println(fileName + " saved successfully");
      
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    
  } //end main method
  
} //end class

