import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.PairFunction;
import scala.Int;
import scala.Tuple2;

import javax.sql.rowset.RowSetFactory;
import javax.swing.*;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RDD {



    public static void main(String[] args) {

        try {
            //create an print writer for writing to a file
            PrintStream out = new PrintStream(new FileOutputStream("RDDDrill_1_Output.txt"));
            System.setOut(out);


            SparkConf conf = new SparkConf().setAppName("appName").setMaster("local");
            JavaSparkContext sc = new JavaSparkContext(conf);


            JavaRDD<String> lines = sc.textFile("tx_dataset.txt");


            PairFunction<String, String, String> date =
                    new PairFunction<String, String, String>() {
                        public Tuple2<String, String> call(String x) {
                            return new Tuple2(x.split("#")[0], x);
                        }
                    };
            JavaPairRDD<String, String> datePairs = lines.mapToPair(date);

            PairFunction<String, String, String> time =
                    new PairFunction<String, String, String>() {
                        public Tuple2<String, String> call(String x) {
                            return new Tuple2(x.split("#")[1], x);
                        }
                    };
            JavaPairRDD<String, String> timePairs = lines.mapToPair(time);

            PairFunction<String, String, String> customerId =
                    new PairFunction<String, String, String>() {
                        public Tuple2<String, String> call(String x) {
                            return new Tuple2(x.split("#")[2], x);
                        }
                    };
            JavaPairRDD<String, String> customerIdPairs = lines.mapToPair(customerId);

            PairFunction<String, String, String> productId =
                    new PairFunction<String, String, String>() {
                        public Tuple2<String, String> call(String x) {
                            return new Tuple2(x.split("#")[3], x);
                        }
                    };
            JavaPairRDD<String, String> productIdPairs = lines.mapToPair(productId);

            PairFunction<String, String, String> itemNumber =
                    new PairFunction<String, String, String>() {
                        public Tuple2<String, String> call(String x) {
                            return new Tuple2(x.split("#")[4], x);
                        }
                    };
            JavaPairRDD<String, String> itemNumberPairs = lines.mapToPair(itemNumber);

            PairFunction<String, String, String> itemPrice =
                    new PairFunction<String, String, String>() {
                        public Tuple2<String, String> call(String x) {
                            return new Tuple2(x.split("#")[5], x);
                        }
                    };
            JavaPairRDD<String, String> itemPricePairs = lines.mapToPair(itemPrice);

            //prints the lines
//            for (Tuple2 line : customerIdPairs.collect()) {
//                System.out.println(line);
//            }

            //step1
            JavaRDD<Integer> lineLengths = lines.map(s -> s.length());
            int totalLength = lineLengths.reduce((a, b) -> a + b);

            //step2
            System.out.println("STEP 2: How many lines are in the file:");
            System.out.println(lines.count());

            //step3
            System.out.println("STEP 3: How many unique customers:");
            JavaPairRDD uniqueCustomers = customerIdPairs.groupByKey();
            System.out.println(uniqueCustomers.count());

            //step4
            System.out.println("STEP 4: Which customer made the most purchases:");
            Map map = customerIdPairs.countByKey();
//            for (Object i : map.keySet()) {
//                System.out.println(i + ": " + map.get(i));
//            }
            RDD rdd = new RDD();
            Object maxCustomer = rdd.maxKey(map);
            System.out.println(maxCustomer);

            //step5
            System.out.println("STEP 5: How many purchases did that customer make:");
            Object maxPurchases = rdd.maxValue(map);
            System.out.println(maxPurchases);

            //step6
            System.out.println("STEP 6: Print that set of purchases:");
            List purchases = customerIdPairs.lookup(maxCustomer.toString());
            for (Object purchase : purchases) {
                String purchaseString = purchase.toString();
                String[] split = purchaseString.split("#");
                for (int i = 0; i < split.length; i++) {
                    System.out.print(split[i]);
                    if ((i+1) < split.length) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }

            //step7
            List twentyFive = productIdPairs.lookup("25");

        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public <K, V extends Comparable<V>> K maxKey(Map<K, V> map) {
        Map.Entry<K, V> maxEntry = Collections.max(map.entrySet(), (Map.Entry<K, V> e1, Map.Entry<K, V> e2) -> e1.getValue()
                .compareTo(e2.getValue()));
        return maxEntry.getKey();
    }

    public <K, V extends Comparable<V>> V maxValue(Map<K, V> map) {
        Map.Entry<K, V> maxEntry = Collections.max(map.entrySet(), (Map.Entry<K, V> e1, Map.Entry<K, V> e2) -> e1.getValue()
                .compareTo(e2.getValue()));
        return maxEntry.getValue();
    }
}
