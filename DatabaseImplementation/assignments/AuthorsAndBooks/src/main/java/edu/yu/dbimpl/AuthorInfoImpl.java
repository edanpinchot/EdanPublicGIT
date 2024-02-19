package edu.yu.dbimpl;

public class AuthorInfoImpl implements AuthorInfo {
    private int authorID;
    private String firstName;
    private String lastName;

    public AuthorInfoImpl(int authorID, String firstName, String lastName) {
        this.authorID = authorID;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getAuthorID() {
        return authorID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if ((object == null) || (object.getClass() != this.getClass())) {
            return false;
        }

        AuthorInfoImpl ti = (AuthorInfoImpl) object;
        if ((this.authorID == (ti.authorID)) && (this.firstName.equals(ti.firstName)) && (this.lastName.equals(ti.lastName))) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (7 * (authorID + firstName.hashCode() + lastName.hashCode()));
    }
}
