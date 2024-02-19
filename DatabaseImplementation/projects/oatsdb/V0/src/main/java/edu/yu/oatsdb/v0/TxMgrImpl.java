package edu.yu.oatsdb.v0;

import edu.yu.oatsdb.base.*;

import java.util.HashMap;
import java.util.Map;

public enum TxMgrImpl implements TxMgr {
    Instance;

    private TxImpl transaction = new TxImpl();
    private Thread thread = Thread.currentThread();
    private Map<Thread, TxImpl> ThreadToTx = new HashMap<>();


    public void begin() throws NotSupportedException, SystemException {
            //if thread is already associated with a transaction
            if (ThreadToTx.get(thread) != null) {
                throw new NotSupportedException("ERROR: Thread is already associated with a transaction.");
            }

            transaction.setStatuses(TxStatus.ACTIVE);

            //and associate transaction with the current thread
            ThreadToTx.put(thread, transaction);
    }


    public void commit() throws RollbackException, IllegalStateException, SystemException {
        //indicate that transaction has been rolled back instead of committed
        if (transaction.getStatus().equals(TxStatus.ROLLEDBACK)) {
            throw new RollbackException("ERROR: Transaction has been rolled back rather than committed.");
        }

        //if the current thread is not associated with a transaction
        transactionAssociation();

        transaction.setStatuses(TxStatus.COMMITTING);

        //thread is no longer associated with the transaction
        ThreadToTx.remove(thread);

        transaction.setStatuses(TxStatus.COMMITTED);
    }


    public void rollback() throws IllegalStateException, SystemException {
        //if the current thread is not associated with a transaction
        transactionAssociation();

        transaction.setStatuses(TxStatus.ROLLING_BACK);

        //thread is no longer associated with the transaction
        ThreadToTx.remove(thread);

        transaction.setStatuses(TxStatus.ROLLEDBACK);
    }


    public Tx getTx() throws SystemException {
        //if no transaction is associated with current thread, return a transaction with TxStatus "NO_TRANSACTION"
        if ((!ThreadToTx.containsKey(thread)) || (ThreadToTx.get(thread) == null)) {
            transaction.setStatuses(TxStatus.NO_TRANSACTION);
        }

        return transaction;
    }


    public TxStatus getStatus() throws SystemException {
        return transaction.getStatus();
    }


    private void transactionAssociation() {
        TxStatus status = null;
        try {
            //status will be NO_TRANSACTION as long as this thread is not associated with a transaction, which includes its status being UNKOWN, COMMITTED, or ROLLEDBACK
            status = getTx().getStatus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (status == (TxStatus.NO_TRANSACTION)) {
            throw new IllegalStateException("ERROR: Current thread is not associated with a transaction.");
        }
    }
}



//        if ((!ThreadToTx.containsKey(thread)) || (ThreadToTx.get(thread) == null)) {
//            throw new IllegalStateException("ERROR: Current thread is not associated with a transaction");
//        }