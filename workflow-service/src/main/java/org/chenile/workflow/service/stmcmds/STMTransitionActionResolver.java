package org.chenile.workflow.service.stmcmds;

import org.chenile.core.context.ContextContainer;
import org.chenile.stm.action.STMTransitionAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.springframework.util.StringUtils.capitalize;

/**
 * Resolves bean name for the STM transition action in Spring using the event ID and a prefix
 * specific to the workflow. If no matching bean is found, returns a default action if set.
 * <p>Shared between {@link BaseTransitionAction} and {@link StmBodyTypeSelector}.</p>
 */
public class STMTransitionActionResolver {

    private final String prefix;
    private final String[] subPrefixes;
    private final STMTransitionAction<?> defaultAction;

    @Autowired private ApplicationContext applicationContext;
    @Autowired private ContextContainer contextContainer;

    public STMTransitionActionResolver(String prefix) {
        this(prefix, null);
    }

    public STMTransitionActionResolver(String prefix, STMTransitionAction<?> defaultAction) {
        this(prefix, defaultAction, new String[0]);
    }

    public STMTransitionActionResolver(String prefix, STMTransitionAction<?> defaultAction, String... subPrefixes) {
        this.prefix = prefix != null ? prefix : "";
        this.defaultAction = defaultAction;
        this.subPrefixes = subPrefixes != null ? subPrefixes : new String[0];
    }

    public STMTransitionAction<?> getBean(String eventId) {
        try {
            String contextBasedPrefix = resolvePrefixFromContext();
            String beanNameWithContextPrefix = buildBeanName(contextBasedPrefix, eventId);

            if (applicationContext.containsBean(beanNameWithContextPrefix)) {
                return (STMTransitionAction<?>) applicationContext.getBean(beanNameWithContextPrefix);
            }

            String fallbackBeanName = buildBeanName(prefix, eventId);
            if (applicationContext.containsBean(fallbackBeanName)) {
                return (STMTransitionAction<?>) applicationContext.getBean(fallbackBeanName);
            }
        } catch (Exception ignored) {
            return defaultAction;
        }
        return defaultAction;
    }

    private String resolvePrefixFromContext() {
        if (subPrefixes.length == 0) return prefix;

        Map<String, String> contextMap = contextContainer.toMap();
        for (String key : subPrefixes) {
            String value = contextMap.get("x-" + key);
            if (value != null) {
                return value + capitalize(prefix);
            }
        }
        return null;
    }

    private String buildBeanName(String resolvedPrefix, String eventId) {
        return (resolvedPrefix == null || resolvedPrefix.isEmpty())
                ? eventId
                : resolvedPrefix + capitalize(eventId);
    }

}
