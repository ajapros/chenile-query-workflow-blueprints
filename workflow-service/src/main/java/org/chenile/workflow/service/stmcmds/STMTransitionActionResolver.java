package org.chenile.workflow.service.stmcmds;

import org.chenile.core.context.ContextContainer;
import org.chenile.stm.action.STMAutomaticStateComputation;
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
    private final String[] otherPrefixes;
    private final STMTransitionAction<?> defaultAction;

    @Autowired private ApplicationContext applicationContext;
    @Autowired private ContextContainer contextContainer;

    public STMTransitionActionResolver(String prefix) {
        this(prefix, null);
    }

    public STMTransitionActionResolver(String prefix, STMTransitionAction<?> defaultAction) {
        this(prefix, defaultAction, new String[0]);
    }

    public STMTransitionActionResolver(String prefix, STMTransitionAction<?> defaultAction, String... otherPrefixes) {
        this.prefix = prefix != null ? prefix : "";
        this.defaultAction = defaultAction;
        this.otherPrefixes = otherPrefixes != null ? otherPrefixes : new String[0];
    }

    public STMAutomaticStateComputation<?> resolveAutomaticStateDescriptor(String actionStateName){
        return (STMAutomaticStateComputation<?>) internallyResolveBean(actionStateName);
    }

    public STMTransitionAction<?> getBean(String eventId) {
        STMTransitionAction<?> action = (STMTransitionAction<?>) internallyResolveBean(eventId);
        return (action == null)? defaultAction : action;
    }

    private Object internallyResolveBean(String name) {
        try {
            String contextBasedPrefix = resolvePrefixFromContext();
            String beanNameWithContextPrefix = buildBeanName(contextBasedPrefix, name);

            if (applicationContext.containsBean(beanNameWithContextPrefix)) {
                return applicationContext.getBean(beanNameWithContextPrefix);
            }

            String fallbackBeanName = buildBeanName(prefix, name);
            if (applicationContext.containsBean(fallbackBeanName)) {
                return applicationContext.getBean(fallbackBeanName);
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private String resolvePrefixFromContext() {
        if (otherPrefixes.length == 0) return prefix;

        Map<String, String> contextMap = contextContainer.toMap();
        for (String key : otherPrefixes) {
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
