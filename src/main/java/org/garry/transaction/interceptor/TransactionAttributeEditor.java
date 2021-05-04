package org.garry.transaction.interceptor;

import java.beans.PropertyEditorSupport;

/**
 * @ClassName TransactionAttributeEditor
 * @Description TODO
 * @Author cy
 * @Date 2021/5/4 21:28
 */
public class TransactionAttributeEditor extends PropertyEditorSupport {

    /**
     * Format is PROPAGATION_NAME,ISOLATION_NAME,readOnly,timeout_NNNN,+Exception1,-Exception2.
     * Null or the empty string means that the method is non transactional.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        super.setAsText(text);
    }
}
