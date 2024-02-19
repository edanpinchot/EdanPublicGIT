import redis.clients.jedis.Jedis;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class IntroRedis2 {

    private static final int QUARTER_MINUTE_IN_SECONDS = 15;
    private final int VOTE_SCORE = 500;
    private final int ARTICLES_PER_PAGE = 3;
    private static Jedis jedis = new Jedis("localhost");
    private List<Map<String, String>> articlesToPrint = new LinkedList();


    public void postAnArticle(String user, String title, String link) {

        //Incremented articleId
        String articleId = String.valueOf(jedis.incr("article:"));

        //Primary key
        String article = "article:" + articleId;

        long now = System.currentTimeMillis() / 1000;

        //create "articles" as HashMaps
        Map<String, String> articleMap = new HashMap<>();
        articleMap.put("title", title);
        articleMap.put("link", link);
        articleMap.put("user", user);
        articleMap.put("now", String.valueOf(now));
        articleMap.put("id", articleId);

        //posting an article implicitly casts one vote
        articleMap.put("votes", "1");

        // Record the voters, prevent repeated voting, and  the expiration time
        String voted = "voted:" + articleId;
        jedis.sadd(voted, user);
        jedis.expire(voted, QUARTER_MINUTE_IN_SECONDS);

        // Insert through article key
        jedis.hmset(article, articleMap);

        //score is  to current time + VOTE_SCORE
        jedis.zadd("score:", now + VOTE_SCORE, article);
        jedis.zadd("time:", now, article);
//        TimeUnit.SECONDS.sleep(1);
    }

    public void voteForAnArticle(String user, String articleId) {
        String article = "article:" + articleId;
        String voted = "voted:" + articleId;

        // Get publishing time
        Double zscore = jedis.zscore("time:", article);
        long now = System.currentTimeMillis() / 1000;

        //Can't vote if QUARTER_MINUTE_IN_SECONDS has elapsed
        if (zscore < (now - QUARTER_MINUTE_IN_SECONDS)) {
            return;
        }

        //Can't vote if user has already voted for this article
        if (jedis.sadd(voted, user) == 0) {
            return;
        }

        //Successful vote, score is incremented by VOTE_SCORE, number of votes is incremented by 1
        jedis.zincrby("score:", VOTE_SCORE, article);
        jedis.hincrBy(article, "votes", 1);
    }

    public void getArticlesByScore(int page) {
        long start = (page - 1) * ARTICLES_PER_PAGE;
        long end = start + ARTICLES_PER_PAGE - 1;
        Set<String> ids = jedis.zrevrange("score:", start, end);

        for (String id : ids) {
            Map<String, String> article_data = jedis.hgetAll(id);
//            System.out.println(id);
            articlesToPrint.add(article_data);
        }
    }

    public void getArticlesByTime(int page) {
        long start = (page - 1) * ARTICLES_PER_PAGE;
        long end = start + ARTICLES_PER_PAGE - 1;
        Set<String> ids = jedis.zrevrange("time:", start, end);

        for (String id : ids) {
            Map<String, String> article_data = jedis.hgetAll(id);
//            System.out.println(id);
            articlesToPrint.add(article_data);
        }
    }

    public void printArticles() {
        for (Map<String, String> articleData : articlesToPrint) {
            String id = articleData.get("id");
            String title = articleData.get("title");
            String link = articleData.get("link");
            String user = articleData.get("user");
            String now = articleData.get("now");
            String votes = articleData.get("votes");
            System.out.println("ID: " + id + "\n" + "Title: " + title + "\n" + "Link: " + link + "\n" + "User: " + user + "\n" + "Time: " + now + "\n" + "Votes: " + votes + "\n");
        }
    }

    public static void main(String[] args) {
//        //Connecting to Redis server on localhost
////        Jedis jedis = new Jedis("localhost");
//        System.out.println("Connection to server sucessfully");
//        //check whether server is running or not
//        System.out.println("Server is running: "+jedis.ping());

        System.out.println("***STEP 1:***");
        jedis.flushDB();

        System.out.println("\n" + "***STEPS 2 and 3:***");
        IntroRedis2 ir = new IntroRedis2();
        ir.postAnArticle("Bob", "title21", "http://www.qq.com");
        ir.postAnArticle("Bob", "title180", "http://www.youtube.com");
        ir.postAnArticle("Bob", "title47", "http://www.vk.com");
        ir.postAnArticle("Jane", "title94", "http://www.vk.com");
        ir.postAnArticle("Xandra", "title151", "http://www.google.co.in");
        //print augmented article state

        System.out.println("\n" + "***STEPS 4 and 5:***");
        ir.voteForAnArticle("Joel", "1");
        //print votes for the article
        System.out.println("Number of votes for article 1: " + jedis.hget("article:1", "votes"));

        System.out.println("\n" + "***STEPS 6 and 7:***");
        ir.voteForAnArticle("Joel", "1");
        //print votes for that article
        System.out.println("Number of votes for article 1: " + jedis.hget("article:1", "votes"));

        System.out.println();
        System.out.println("***STEP 8:***");
        ir.getArticlesByScore(1);
        ir.printArticles();


        System.out.println("***STEP 9:***");
        ir.getArticlesByTime(1);
        ir.printArticles();

        System.out.println("***STEPS 10 and 11:***");
        //put program to sleep and wake it up
        try {
            System.out.println("Going to sleep. . . z z z . . .");
            Thread.sleep((QUARTER_MINUTE_IN_SECONDS * 1000) + 1000);
            System.out.println("I hath awoken!!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\n" + "***STEPS 12 and 13:***");
        ir.voteForAnArticle("Xandra", "2");
        System.out.println("Number of votes for article 2: " + jedis.hget("article:2", "votes"));
    }

} 