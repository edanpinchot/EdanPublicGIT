package edu.yu.oatsdb.base;

/** Extends the "DB Management System" interface with an API that allows for
 * configuration of the DBMSImpl.
 * 
 * @author Avraham Leff
 */

import java.util.*;

public interface ConfigurableDBMS extends DBMS {

  /** Sets the duration of the "transaction timeout".  A client whose
   * transaction's duration exceeds the DBMSImpl's timeout will be automatically
   * rolled back by the DBMSImpl.
   *
   * @param ms the timeout duration in ms, must be greater than 0
   */
  void setTxTimeoutInMillis(int ms);

  /** Returns the current DBMSImpl transaction timeout duration.
   *
   * @return duration in milliseconds
   */
  int getTxTimeoutInMillis();
}

