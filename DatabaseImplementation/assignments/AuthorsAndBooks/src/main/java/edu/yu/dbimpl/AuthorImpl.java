package edu.yu.dbimpl;

import java.util.Set;

public class AuthorImpl implements Author {
    private AuthorInfo author;
    private Set<TitleInfo> titleInfos;

    public AuthorImpl(AuthorInfo author, Set<TitleInfo> titleInfos) {
        this.author = author;
        this.titleInfos = titleInfos;
    }

    public AuthorImpl(int authorID, String firstName, String lastName, Set<TitleInfo> titleInfos) {
        this.author = new AuthorInfoImpl(authorID, firstName, lastName);
        this.titleInfos = titleInfos;
    }

    /** Get the title information (must be at least one) for books written by
     * this author
     */
    public Set<TitleInfo> getTitleInfos() {
        return titleInfos;
    }

    public int getAuthorID() {
        return author.getAuthorID();
    }

    public String getFirstName() {
        return author.getFirstName();
    }

    public String getLastName() {
        return author.getLastName();
    }
}
