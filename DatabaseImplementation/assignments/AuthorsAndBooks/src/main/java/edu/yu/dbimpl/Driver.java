package edu.yu.dbimpl;

import java.util.List;
import java.util.Set;

public class Driver {

    public static void main(String[] args) {

        TitleInfoImpl ti1 = new TitleInfoImpl("Red Book", "1234", 3, "Publishers");
        TitleInfoImpl ti2 = new TitleInfoImpl("Red Book", "1234", 3, "Publishers");
        System.out.println(ti1.equals(ti2));
        System.out.println(ti1.hashCode());
        System.out.println(ti2.hashCode());

        QueryEngineImpl queryEngine = new QueryEngineImpl();
        queryEngine.configureConnection("authorsandbooks", "edanpinchot", "arianne");

        Set<AuthorInfo> author = queryEngine.getAllAuthorInfos();
        for (AuthorInfo a : author) {
            System.out.println(a.getFirstName() + " " + a.getLastName());
        }

        Author a = queryEngine.authorByName("Deitel", "Paul");
        Set<TitleInfo> titleInfos = a.getTitleInfos();
        for (TitleInfo ti : titleInfos) {
            System.out.println(ti.getTitle());
        }

        List<Book> books = queryEngine.booksByTitle("iPhone for Programmers: An App-Driven Approach");
        for (Book b : books) {
            System.out.println(b.getTitleInfo().getTitle());
            List<AuthorInfo> authors = b.getAuthorInfos();
            for (AuthorInfo ai : authors) {
                System.out.println(ai.getFirstName() + " " + ai.getLastName());
            }
        }

    }

}
