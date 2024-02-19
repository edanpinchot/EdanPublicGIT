package edu.yu.oatsdb.v2;

import edu.yu.oatsdb.base.*;

public class TxImpl implements Tx {
    private TxStatus status;
    private TxCompletionStatus completionStatus;

    public TxImpl() {
        //initialize transaction status to "null" aka "UNKNOWN"
        this.status = TxStatus.UNKNOWN;
    }

    public void setStatuses(TxStatus status) {
        this.status = status;
        this.completionStatus = TxCompletionStatus.updateTxStatus(completionStatus, status);
    }

    public TxStatus getStatus() throws SystemException {
        return status;
    }

    public TxCompletionStatus getCompletionStatus() {           //again added the public
        return completionStatus;
    }
}
