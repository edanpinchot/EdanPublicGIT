package edu.yu.mdm;

/** A skeleton class for implementating the "gpa_JDBC" requirements doc.
 *
 * @author Avraham Leff
 */

import java.sql.*;
import java.util.*;

public class gpaJDBC {
  private Connection c;
  private List<GPARecord> gpaList = new LinkedList<>();
  private Map<String, Double> idToGPA = new HashMap<>();

  // A value class used by the studentGPAs() method
  public static class GPARecord implements Comparable<GPARecord> {
    /** Constructor: you may not change the signature or semantics of this
     * method.
     *
     * @param String studentId
     * @param double gpa
     */
    public GPARecord(final String studentId, final double gpa) {
      assert studentId != null : "studentId can't be null";
      assert studentId.length() > 0 : "studentId can't be empty: "+studentId;
      assert gpa >= 0.0 : "gpa must be non-negative: "+gpa;

      this.studentId = studentId;
      this.gpa = gpa;
    }

    public double getGpa() {
      return gpa;
    }

    public String getStudentId() {
      return studentId;
    }

    @Override
    public int compareTo(final GPARecord b) {
      return -1;
    }

    // safe to expose because immutable
    public final String studentId;
    public final double gpa;
  }


  /** Constructor specifying the JDBC "connection parameters" to use when
   * subsequentally invoking operations that require connecting to the
   * database.
   *
   * @param databaseURL the base JDBC url, does NOT contain either the database
   * name, the user name, or the password.  Example:
   * "jdbc:postgresql://localhost/"
   * @param dbName specifies the database to connect to
   * @param userName the user name credentials to use when connecting
   * @param password the password to use when connecting
   *
   * @see https://jdbc.postgresql.org/documentation/head/connect.html
   */
  public gpaJDBC(final String databaseURL, final String dbName, final String userName, final String password) {
    this.c = null;

    try {
      Class.forName("org.postgresql.Driver");
      this.c = DriverManager.getConnection((databaseURL + dbName), userName, password);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Opened database successfully");
  }


  /** Return a List of GPARecord, ordered in lexicographical order, by
   * ascending student id for all students who have taken a course.  GPAs
   * should be reported with a precision of 2 decimal points.  If a student
   * doesn't have a gpa because she hasn't taken courses or because the grade
   * earned on a course doesn't meet the criteria, the student should not be
   * included in the returned result.
   *
   * @return List of GPARecord per the above semantics.
   * @see GPARecord
   */
  public List<GPARecord> studentGPAs() {
    Map<String, Integer> studentCreditTotal = new HashMap<>();
    Map<String, Double> studentGradesWeighted = new HashMap<>();

    try {
      //part 1
      String SQL_QUERY1 = "SELECT id, SUM(credits) " +
              "FROM takes_s, course_s " +
              "WHERE takes_s.course_id = course_s.course_id AND takes_s.grade != 'F' " +
              "GROUP BY id;";
      PreparedStatement preparedStatement1 = c.prepareStatement(SQL_QUERY1);
      ResultSet resultSet1 = preparedStatement1.executeQuery();

      //for each row, initialize students weighted grades to 0, and put credits into map of each student's credit total
      //query itself weeds out credits for classes that were failed
      while (resultSet1.next()) {
        studentCreditTotal.put(resultSet1.getString("id"), resultSet1.getInt("sum"));
        studentGradesWeighted.put(resultSet1.getString("id"), 0.0);
      }

      //part2
      String SQL_QUERY2 = "SELECT takes_s.id, takes_s.grade, course_s.credits " +
              "FROM takes_s, course_s " +
              "WHERE takes_s.course_id = course_s.course_id " +
              "ORDER BY takes_s.id ASC;";
      PreparedStatement preparedStatement = c.prepareStatement(SQL_QUERY2);
      ResultSet resultSet2 = preparedStatement.executeQuery();

      //for each row, turn the grade into a number, "weight" it by multiplying by the corresponding course's credit total,
      //and add that to the already existing weighted grade for that student
      while (resultSet2.next()) {
        String id = resultSet2.getString("id");
        String grade = resultSet2.getString("grade");
        int credits = resultSet2.getInt("credits");
        double numGrade = 0.0;
        if (grade == null) {
          continue;
        }
        switch(grade) {
          case "A":
            numGrade = 4.0;
            break;
          case "A-":
            numGrade = 3.7;
            break;
          case "B+":
            numGrade = 3.3;
            break;
          case "B":
            numGrade = 3.0;
            break;
          case "B-":
            numGrade = 2.7;
            break;
          case "C+":
            numGrade = 2.3;
            break;
          case "C":
            numGrade = 2.0;
            break;
          case "C-":
            numGrade = 1.7;
            break;
          case "D+":
            numGrade = 1.3;
            break;
          case "D":
            numGrade = 1.0;
            break;
        }

        double updatedGrade = studentGradesWeighted.get(id) + (numGrade * credits);
        studentGradesWeighted.put(id, updatedGrade);
      }

      //putting it together
      //for each student, divide total weighted grade by total of credits, rounded to 2 decimal points
      for (String id : studentCreditTotal.keySet()) {
        double gpa = (studentGradesWeighted.get(id) / studentCreditTotal.get(id));
        double roundedGpa = Math.round(gpa * 100.0) / 100.0;
        GPARecord gpaRecord = new GPARecord(id, roundedGpa);
        gpaList.add(gpaRecord);
        //this is so that we can easily extract just the gpa given an id, for purposes of gpa() method
        idToGPA.put(id, roundedGpa);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return gpaList;
  } // allStudentsGPAReport


  /** Returns the total number of credits points earned by that student across
   * all courses taken by the student
   *
   * @param ID the student's id
   * @return total grade points (not GPA) earned by that student across all
   * courses taken by that student.  If the student didn't take any courses,
   * return 0.  A grade that is less than a "D" doesn't contribute to the total
   * grade points.
   * @throws IllegalArgumentException if no student with that ID exists.
   */
  public int totalCredits(final String studentId) {
    int total = 0;

    try {
      String SQL_QUERY = "SELECT takes_s.ID, takes_s.grade, course_s.course_id, course_s.credits " +
              "FROM takes_s, course_s " +
              "WHERE takes_s.ID = '" + studentId + "' AND takes_s.course_id = course_s.course_id;";
      PreparedStatement preparedStatement = c.prepareStatement(SQL_QUERY);
      ResultSet resultSet = preparedStatement.executeQuery();

      //query returns each course the given student took and the credits for each course, so add up the credits in each row.
      //but transform the grade into a numeric so that we can ignore the credits for classes below a D
      while (resultSet.next()) {
        String grade = resultSet.getString("grade");
        double numGrade = 0.0;
        if (grade == null) {
          continue;
        }
        switch(grade) {
          case "A":
            numGrade = 4.0;
            break;
          case "A-":
            numGrade = 3.7;
            break;
          case "B+":
            numGrade = 3.3;
            break;
          case "B":
            numGrade = 3.0;
            break;
          case "B-":
            numGrade = 2.7;
            break;
          case "C+":
            numGrade = 2.3;
            break;
          case "C":
            numGrade = 2.0;
            break;
          case "C-":
            numGrade = 1.7;
            break;
          case "D+":
            numGrade = 1.3;
            break;
          case "D":
            numGrade = 1.0;
            break;
        }

        int credits = resultSet.getInt("credits");
        if(numGrade >= 1.0) {
          total += credits;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return total;
  }


  /** Returns the student's GPA.
   *
   * @param ID the student's id
   * @return The grade point average earned by that student across all courses
   * taken by that student.  GPAs must be calculated with a precision of 2
   * decimal points.  If the student didn't take any courses, return 0.0
   * @throws IllegalArgumentException if no student with that ID exists.
   */
  public double gpa(final String studentId) {
    if (totalCredits(studentId) == 0) {
      return 0;
    }

    return idToGPA.get(studentId);
  }


  public static void main(String[] args) {
    gpaJDBC g = new gpaJDBC("jdbc:postgresql://localhost:5432/", "edanpinchot", "edanpinchot", "");

    //test studentGPAs()
    List<GPARecord> list = g.studentGPAs();
    for (GPARecord gpaRecord : list) {
      System.out.println(gpaRecord.getStudentId() + " " + gpaRecord.getGpa());
    }
    System.out.println();

    //test totalCredits()
    int res = g.totalCredits("12345");
    System.out.println(res);
    System.out.println();

    //test gpa()
    double gpa = g.gpa("12345");
    System.out.println(gpa);

  }

}