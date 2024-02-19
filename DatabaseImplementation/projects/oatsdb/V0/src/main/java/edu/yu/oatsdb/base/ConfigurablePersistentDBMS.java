package edu.yu.oatsdb.base;

/** Extends the "Configurable DB Management System" interface with an API that
 * allows for configuration of a persistent DBMSImpl.
 * 
 * Design note: In a production system these APIs would be available only to
 * administrators.

 * @author Avraham Leff
 */

import java.util.*;

public interface ConfigurablePersistentDBMS extends ConfigurableDBMS {

  /** Returns the disk usage in MB of this DBMSImpl instance.
   *
   * @return disk usage in MB for this DBMSImpl
   */
  double getDiskUsageInMB();

  /** Delete all files and directories associated with this DBMSImpl instance from
   * both disk and from main-memory.  Effectively resets the database.
   *
   * IMPORTANT: the effects of this API on existing transactions is
   * undefined.  This method should be invoked only when the system is
   * quiescent (i.e., outside a transaction).
   */
  void clear();
}

