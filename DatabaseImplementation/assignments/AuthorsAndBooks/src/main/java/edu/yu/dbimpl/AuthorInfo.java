package edu.yu.dbimpl;

import java.util.Set;

/* Defines an interface for author information.  In contrast to theh Author
 * interface, this interface does not include books written by this author
 *
 * @author Avraham Leff
 */

public interface AuthorInfo {
  int getAuthorID();
  String getFirstName();
  String getLastName();
}
