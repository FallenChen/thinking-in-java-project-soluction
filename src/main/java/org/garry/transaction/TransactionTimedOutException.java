package org.garry.transaction;

/**
 * Exception to be thrown when a transaction has timed out
 *
 * <p>Thrown by Spring's local transaction strategies if the deadline
 * for a transaction has been reached when an operation is attempted,
 * according to the timeout specified for the given transaction.
 *
 * <p>Beyond such checks before each transactional operation, Spring's
 * local transaction strategies will also pass appropriate timeout values
 * to resource operations (for example to JDBC Statements, letting the JDBC
 * driver respect the timeout). Such operations will usually throw native
 * resource exceptions (for example, JDBC SQLExceptions) if their operation
 * timeout has been exceeded, to be converted to Spring's DataAccessException
 * in the respective DAO (which might use Spring's JdbcTemplate, for example).
 *
 */
public class TransactionTimedOutException extends TransactionException{

    public TransactionTimedOutException(String msg)
    {
        super(msg);
    }

    public TransactionTimedOutException(String msg, Throwable cause)
    {
        super(msg,cause);
    }
}
