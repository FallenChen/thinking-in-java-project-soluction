package org.garry.jdbc.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 *
 */
public abstract class AbstractDataSource implements DataSource {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Returns 0, indicating the default system timeout is to be used
     * @return
     * @throws SQLException
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    /**
     * Setting a login timeout is not supported
     * @param seconds
     * @throws SQLException
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }


}
