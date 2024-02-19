package edu.yu.dbimpl;

import java.util.List;

public class BookImpl implements Book {
    private TitleInfo titleInfo;
    private List<AuthorInfo> authorInfos;

    public BookImpl(TitleInfo titleInfo, List<AuthorInfo> authorInfos) {
        this.titleInfo = titleInfo;
        this.authorInfos = authorInfos;
    }

    public BookImpl(String title, String ISBN, int editionNumber, String copyright, List<AuthorInfo> authorInfos) {
        this.titleInfo = new TitleInfoImpl(title, ISBN, editionNumber, copyright);
        this.authorInfos = authorInfos;
    }

    public TitleInfo getTitleInfo() {
        return titleInfo;
    }

    /** Get information (not including books written by the author) about all the
     * authors who wrote the book.  Must be least one author.  If there are
     * multiple authors, they must be ordered per query semantics.
     */
    public List<AuthorInfo> getAuthorInfos() {
        return authorInfos;
    }
}