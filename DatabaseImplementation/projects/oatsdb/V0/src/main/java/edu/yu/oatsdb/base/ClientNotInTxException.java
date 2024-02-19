package edu.yu.oatsdb.base;

/** Runtime exception thrown when client accesses a "transactional resource"
 * (e.g., a DBMSImpl Map or an entry in a DBMSImpl Map) when not in a transaction.
 * 
 * @author Avraham Leff
 */

public class ClientNotInTxException extends RuntimeException {
  public ClientNotInTxException(final String msg) {
    super(msg);
  }
  public ClientNotInTxException(final Throwable cause) {
    super(cause);
  }
  public ClientNotInTxException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
