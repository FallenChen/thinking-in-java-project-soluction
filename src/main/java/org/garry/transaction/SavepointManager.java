package org.garry.transaction;

import java.sql.Connection;
import java.sql.Savepoint;

/**
 * Interface that specifies an API to programmatically manage transaction
 * savepoints in a generic fashion.Extended by TransactionStatus to
 * expose savepoint management functionality for a specific transaction
 *
 * This interface is inspired by JDBC 3.0's Savepoint mechanism
 * but is independent from any specific persistence technology
 */
public interface SavepointManager {

    /**
     * @see Connection#setSavepoint()
     * @return
     * @throws TransactionException
     */
    Object createSavepoint() throws TransactionException;

    /**
     * @see Connection#rollback(Savepoint)
     * @param savepoint
     * @throws TransactionException
     */
    void rollbackToSavepoint(Object savepoint) throws TransactionException;

    /**
     * @see java.sql.Connection#releaseSavepoint(Savepoint)
     * @param savepoint
     * @throws TransactionException
     */
    void releaseSavepoint(Object savepoint)throws TransactionException;

}
