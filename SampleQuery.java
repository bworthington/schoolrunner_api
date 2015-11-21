import java.sql.*;

//returns a list showing the number of teacher-created assessments for each
//course at each small school for Q1 and Q2
public class SampleQuery {
  
  public static void main(String args[]) {
    
    Connection c = null;
    Statement stmt = null;
    
    try {
      
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:databases/SRDB1.db");
      c.setAutoCommit(false);
      System.out.println("opened database successfully");
      
      String query = (
        "select " +
            "SCHOOLS.DISPLAY_NAME, " +
            "ifnull(COURSES.COURSE_NAME, '**ASSMTS W/O COURSES**') as COURSE_NAME, " +
            "count(Q1.ASSESSMENT_ID) as Q1_NUMBER_OF_ASSESSMNETS, " +
            "count(Q2.ASSESSMENT_ID) as Q2_NUMBER_OF_ASSESSMNETS " +
        "from ASSESSMENTS " +
            "left outer join SCHOOLS on ASSESSMENTS.SR_SCHOOL_ID = SCHOOLS.SR_SCHOOL_ID " +
            "left outer join COURSES on ASSESSMENTS.COURSE_ID = COURSES.COURSE_ID " +
            "left outer join ( " +
            
            //sub-query to get assessments just from Q1
                "select " +
                    "ASSESSMENTS.ASSESSMENT_ID " +
                "from ASSESSMENTS " +
                "where ASSESSMENTS.ASSESSMENT_DATE <= '2015-09-25' " +
            ") Q1 on ASSESSMENTS.ASSESSMENT_ID = Q1.ASSESSMENT_ID " +
            
            //sub-query to get assessments just from Q2
            "left outer join ( " +
            
                "select " +
                    "ASSESSMENTS.ASSESSMENT_ID " +
                "from ASSESSMENTS " +
                "where ASSESSMENTS.ASSESSMENT_DATE >= '2015-09-26' " +
            ") Q2 on ASSESSMENTS.ASSESSMENT_ID = Q2.ASSESSMENT_ID " +
        
        //assessments from all schools accept RAHS
        "where ASSESSMENTS.SR_SCHOOL_ID != '5' " +
            
            //only include assessments with a staff ID (i.e., teacher-created)
            "and ASSESSMENTS.SR_STAFF_MEMBER_ID is not null " +
            
            "and (COURSES.COURSE_NAME like '%Math%' " +
            "or COURSES.COURSE_NAME like '%ELA%' " +
            "or COURSES.COURSE_NAME like '%Science%' " +
            "or COURSES.COURSE_NAME like '%Nonfiction%' " +
            "or COURSES.COURSE_NAME like '%Social Studies%') " +
            
        "group by COURSES.COURSE_ID " +
        "order by SCHOOLS.DISPLAY_NAME, COURSES.COURSE_NAME;");


      stmt = c.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      
      System.out.printf("%-25s%-35s%-12s%-12s%n",
          "SCHOOL NAME", "COURSE NAME", "#Q1 ASSMTS", "#Q2 ASSMTS");
      
      while (rs.next()) {
        System.out.printf("%-25s%-35s%-12s%-12s%n",
          rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
      }
      
      rs.close();
      stmt.close();
      c.close();
      
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    
  } //end main method
  
} //end class