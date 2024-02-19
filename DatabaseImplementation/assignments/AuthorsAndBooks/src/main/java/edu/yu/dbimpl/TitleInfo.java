package edu.yu.dbimpl;

/* Defines an interface for title information for a single book.
 *
 * @author Avraham Leff
 */

public interface TitleInfo {
  String getTitle();
  String getISBN();
  int getEditionNumber();
  String getCopyright();
}
