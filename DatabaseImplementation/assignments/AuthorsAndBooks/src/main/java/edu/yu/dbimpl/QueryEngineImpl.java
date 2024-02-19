package edu.yu.dbimpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryEngineImpl implements QueryEngine {
    private Connection connection;

    public QueryEngineImpl() { }


    public void configureConnection(String dbname, String username, String password) {
        String url = "jdbc:postgresql://localhost:5432/" + dbname;

        try {
            this.connection = DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Set<AuthorInfo> getAllAuthorInfos() {
        Set<AuthorInfo> authorInfoSet = new HashSet<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM authors");
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int numberOfColumns = metaData.getColumnCount();

            while (resultSet.next()) {
                    //break each data point into an AuthorInfoImpl object
                    //and insert it into the authorsInfo set
                    int authorID = resultSet.getInt("authorID");
                    String firstName = resultSet.getString("firstName");
                    String lastName = resultSet.getString("lastName");
                    AuthorInfoImpl author = new AuthorInfoImpl(authorID, firstName, lastName);
                    authorInfoSet.add(author);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return authorInfoSet;
    }


    public Author authorByName(String lastName, String firstName) {
        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new IllegalArgumentException("ERROR: blank first or last name");
        }

        AuthorImpl author = null;
        Set<TitleInfo> titleInfos = new HashSet<>();

        try {
            //first use this query to determine how many authors are returned (for purpose of throwing errors)
            String SQL_QUERY2 = "SELECT * " +
                    "FROM authors " +
                    "WHERE authors.firstName = '" + firstName + "' " +
                    "AND authors.lastName = '" + lastName + "'";
            PreparedStatement preparedStatement2 = connection.prepareStatement(SQL_QUERY2);
            ResultSet resultSet2 = preparedStatement2.executeQuery();

            int i = 0;
            while (resultSet2.next()) {
                i++;
            }
            if (i > 1) {
                throw new IllegalArgumentException("ERROR: combination of the last name and first name are not unique");
            }
            if (i == 0) {
                return null;
            }

            //onto second query to create the Author Object and give it the proper information
            String SQL_QUERY = "SELECT * " +
                    "FROM authors, titles, authorISBN " +
                    "WHERE authors.firstName = '" + firstName + "' " +
                    "AND authors.lastName = '" + lastName + "' " +
                    "AND authors.authorID = authorISBN.authorID " +
                    "AND authorISBN.isbn = titles.ISBN";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int authorID = resultSet.getInt("authorID");
                String title = resultSet.getString("title");
                String ISBN = resultSet.getString("isbn");
                int editionNumber = resultSet.getInt("editionNumber");
                String copyright = resultSet.getString("copyright");

                TitleInfo ti = createTitleInfo(ISBN, title, copyright, editionNumber);
                titleInfos.add(ti);
                author = new AuthorImpl(authorID, firstName, lastName, titleInfos);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return author;
    }


    public List<Book> booksByTitle(final String title) {
        List<Book> bookList = new ArrayList<>();

        try {
            //first query database to get each book that matches this title in table "titles"
            String SQL_QUERY = "SELECT * " +
                    "FROM titles " +
                    "WHERE titles.title = '" + title + "' " +
                    "ORDER BY isbn ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_QUERY);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                List<AuthorInfo> authorInfos = new ArrayList<>();
                String isbn = resultSet.getString("isbn");

                //now for each book that matched the title, using that book's isbn, query the database for its authors
                //this way, if more than one book has the same titles, we'll obtain their differing authors separately
                String SQL_QUERY2 = "SELECT * " +
                        "FROM authors, authorISBN " +
                        "WHERE authorISBN.isbn = '" + isbn + "' " +
                        "AND authorISBN.authorID = authors.authorID " +
                        "ORDER BY lastName, firstName ASC";
                PreparedStatement preparedStatement2 = connection.prepareStatement(SQL_QUERY2);
                ResultSet resultSet2 = preparedStatement2.executeQuery();

                while (resultSet2.next()) {
                    int authorID = resultSet2.getInt("authorID");
                    String firstName = resultSet2.getString("firstName");
                    String lastName = resultSet2.getString("lastName");
                    AuthorInfo ai = createAuthorInfo(authorID, firstName, lastName);
                    authorInfos.add(ai);
                }

                int editionNumber = resultSet.getInt("editionNumber");
                String copyright = resultSet.getString("copyright");
                BookImpl book = new BookImpl(title, isbn, editionNumber, copyright, authorInfos);
                bookList.add(book);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return bookList;
    }


    public AuthorInfo createAuthorInfo(int authorID, String firstName, String lastName) {
        if (authorID < 0) {
            throw new IllegalArgumentException("ERROR: authorID cannot be negative");
        }
        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new IllegalArgumentException("ERROR: blank first or last name");
        }
        return new AuthorInfoImpl(authorID, firstName, lastName);
    }


    public TitleInfo createTitleInfo(final String isbn, final String title, final String copyright, final int editionNumber) {
        if (title.isEmpty() || copyright.isEmpty() || editionNumber == 0 || isbn.isEmpty()) {
            throw new IllegalArgumentException("ERROR: Something is wrong with the given parameters");
        }
        return new TitleInfoImpl(title, isbn, editionNumber, copyright);
    }

}