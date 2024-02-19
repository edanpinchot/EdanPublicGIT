import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.IntegerType;
import scala.Int;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLInput;
import java.util.Properties;

public class SparkDF {

    public static void main(String[] args) {
        try {
            //create an print writer for writing to a file
            PrintStream out = new PrintStream(new FileOutputStream("SparkDF_Output.txt"));
            System.setOut(out);

            SparkConf conf = new SparkConf().setAppName("appName").setMaster("local");
            JavaSparkContext sc = new JavaSparkContext(conf);


            System.out.println("Step 0: Initializing Spark Session...\n");
            SparkSession spark = SparkSession
                    .builder()
                    .appName("appName")
                    .config("spark.master", "local")
                    .getOrCreate();


            System.out.println("Step 1: Current Spark Version...\n" + sc.version() + "\n");
            Dataset<Row> df = spark.read().format("csv")
                    .option("header", "true")
                    .load("/Users/edanpinchot/MYGIT/PinchotEdan/ModernDataManagement/assignments/SparkDF1/MDM_SparkAssigments/flight-data/csv/2015-summary.csv") ;


            System.out.println("Step 2: Displaying schema...");
            df.printSchema();


            System.out.println("Step 3: Displaying three rows of data...");
            df.show(3);


            System.out.println("Step 4: Save data to a postgreSQL database...\n");
            // The connection URL, assuming your PostgreSQL instance runs locally on the
            // default port, and the database we use is "sparkdf"
            String dbConnectionUrl = "jdbc:postgresql://localhost/sparkdf";

            String username = "edanpinchot";
            String password = "";
            Properties connectionProperties = new Properties();
            connectionProperties.put("user", username) ;
            connectionProperties.put("password", password) ;
            connectionProperties.put("driver", "org.postgresql.Driver");

            String tableName = "public.flight2015";
            df.write()
                    .mode (SaveMode.Overwrite)
                    .jdbc(dbConnectionUrl, tableName, connectionProperties);


            System.out.println("Step 5: Number of records in the dataset...");
            System.out.println(df.count());


            System.out.println("\nStep 6: Create a temporary view...\n");
            df.createOrReplaceTempView("sparkView");


            System.out.println("Step 7: Number of unique origin countries...");
            df.sqlContext().registerDataFrameAsTable(df, "flight2015");
            Dataset<Row> q = spark.sql("SELECT COUNT(DISTINCT `ORIGIN_COUNTRY_NAME`) FROM flight2015;");
            q.show();
//            System.out.print(df.select("ORIGIN_COUNTRY_NAME").distinct().count());


            System.out.println("\nStep 8: Number of rows associated with most common destination...");
            df.groupBy("DEST_COUNTRY_NAME")
                    .count().as("count")
                    .orderBy(org.apache.spark.sql.functions.desc("count"))
                    .limit(1)
                    .show();


            System.out.println("\nStep 9: Which country has the most flights to itself...");
            Dataset<Row> q3 = spark.sql("SELECT `ORIGIN_COUNTRY_NAME`, MAX(count)\n" +
                    "FROM flight2015\n" +
                    "WHERE `ORIGIN_COUNTRY_NAME` = `DEST_COUNTRY_NAME`\n" +
                    "GROUP BY `ORIGIN_COUNTRY_NAME`;");
            q3.show();


            System.out.println("\nStep 10: Top 5 destination countries...");
            df.groupBy("DEST_COUNTRY_NAME")
                    .agg(org.apache.spark.sql.functions.sum("count").as("sum"))
                    .select("DEST_COUNTRY_NAME", "sum")
                    .orderBy(org.apache.spark.sql.functions.desc("sum"))
                    .limit(5)
                    .show();
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
