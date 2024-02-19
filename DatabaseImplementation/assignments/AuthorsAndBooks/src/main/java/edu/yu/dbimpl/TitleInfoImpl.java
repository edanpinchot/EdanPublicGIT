package edu.yu.dbimpl;

public class TitleInfoImpl implements TitleInfo {
    private String title;
    private String ISBN;
    private int editionNumber;
    private String copyright;

    public TitleInfoImpl(String title, String ISBN, int editionNumber, String copyright) {
        this.title = title;
        this.ISBN = ISBN;
        this.editionNumber = editionNumber;
        this.copyright = copyright;
    }

    public String getTitle() {
        return title;
    }

    public String getISBN() {
        return ISBN;
    }

    public int getEditionNumber() {
        return editionNumber;
    }

    public String getCopyright() {
        return copyright;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if ((object == null) || (object.getClass() != this.getClass())) {
            return false;
        }

        TitleInfoImpl ti = (TitleInfoImpl) object;
        if ((this.title.equals(ti.title)) && (this.ISBN.equals(ti.ISBN)) && (this.editionNumber == (ti.editionNumber)) && (this.copyright.equals(ti.copyright))) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (7 * (title.hashCode() + ISBN.hashCode() + copyright.hashCode()));
    }
}
