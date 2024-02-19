package edu.yu.dbimpl;

import java.util.Set;

/* Extends the AuthorInfo interface to include information about books written
 * by this author.
 *
 * @author Avraham Leff
 */

public interface Author extends AuthorInfo {
  /** Get the title information (must be at least one) for books written by
   * this author 
   */
  Set<TitleInfo> getTitleInfos();
}
