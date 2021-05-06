package org.garry.transaction.interceptor;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * @ClassName TransactionAttributeEditor
 * @Description Accepts a String of form {@code PROPAGATION_NAME, ISOLATION_NAME, readOnly, timeout_NNNN,+Exception1,-Exception2}
 * where only propagation code is required.
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
        if(StringUtils.hasLength(text))
        {
            // tokenize it with ","
            String[] tokens = StringUtils.commaDelimitedListToStringArray(text);
            RuleBasedTransactionAttribute attr = new RuleBasedTransactionAttribute();
            for(String token: tokens)
            {
                // Trim leading and trailing whitespace
                String trimmedToken = StringUtils.trimWhitespace(token.trim());
                // Checking whether token contains illegal whitespace within text
                if(StringUtils.containsWhitespace(trimmedToken))
                {
                    throw new IllegalArgumentException(
                            "Transaction attribute token contains illegal whitespace: ["
                            + trimmedToken + "]");
                }
                // Check token type
                if(trimmedToken.startsWith(RuleBasedTransactionAttribute.PREFIX_PROPAGATION))
                {
                    attr.setPropagationBehaviorName(trimmedToken);
                }
                else if(trimmedToken.startsWith(RuleBasedTransactionAttribute.PREFIX_ISOLATION))
                {
                    attr.setIsolationLevelName(trimmedToken);
                }
                else if(trimmedToken.startsWith(RuleBasedTransactionAttribute.PREFIX_TIMEOUT))
                {
                    String value = trimmedToken.substring(DefaultTransactionAttribute.PREFIX_TIMEOUT.length());
                    attr.setTimeout(Integer.parseInt(value));
                }
                else if (trimmedToken.equals(RuleBasedTransactionAttribute.READ_ONLY_MARKER)) {
                    attr.setReadOnly(true);
                }
                else if (trimmedToken.startsWith(RuleBasedTransactionAttribute.PREFIX_COMMIT_RULE)) {
                    attr.getRollbackRules().add(new NoRollbackRuleAttribute(trimmedToken.substring(1)));
                }
                else if (trimmedToken.startsWith(RuleBasedTransactionAttribute.PREFIX_ROLLBACK_RULE)) {
                    attr.getRollbackRules().add(new RollbackRuleAttribute(trimmedToken.substring(1)));
                }
                else {
                    throw new IllegalArgumentException("Invalid transaction attribute token: [" + trimmedToken + "]");
                }
            }
            setValue(attr);
        }
        else
        {
            setValue(null);
        }
    }
}
