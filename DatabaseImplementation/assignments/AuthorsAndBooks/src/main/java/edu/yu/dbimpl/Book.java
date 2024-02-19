package edu.yu.dbimpl;

import java.util.List;

/* Defines an interface for a single Book.
 *
 * @author Avraham Leff
 */
public interface Book {

  /** Get the title information of this Book.
   */
  TitleInfo getTitleInfo();

  /** Get information (not including books written by the author) about all the
   * authors who wrote the book.  Must be least one author.  If there are
   * multiple authors, they must be ordered per query semantics.
   */
  List<AuthorInfo> getAuthorInfos();
}
