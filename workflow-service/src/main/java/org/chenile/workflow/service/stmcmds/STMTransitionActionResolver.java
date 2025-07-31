package org.chenile.workflow.service.stmcmds;

import org.chenile.core.context.ContextContainer;
import org.chenile.stm.action.STMAutomaticStateComputation;
import org.chenile.stm.action.STMTransitionAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.springframework.util.StringUtils.capitalize;

/**
 * Resolves bean name for the various STM components (such as TransitionActions, Automatic
 * State Computations and Post Save Hooks) in Spring using the event ID and a prefix
 * specific to the workflow. If no matching bean is found, returns a default action if set.
 * Suffix is also used depending on the component type which is getting resolved.
 * Suffix usage is optional to keep this backward compatible
 * <p>Shared between {@link BaseTransitionAction} and {@link StmBodyTypeSelector}.</p>
 */
public class STMTransitionActionResolver {
    public static enum ComponentType {
        TRANSITION_ACTION(TRANSITION_ACTION_SUFFIX),
        POST_SAVE_HOOK(POST_SAVE_HOOK_SUFFIX),
        AUTO_STATE(AUTO_STATE_SUFFIX);
        private String suffix = "";
        ComponentType(String suffix){
            this.suffix = suffix;
        }
        public String suffix(){ return suffix;}

    }
    private final String prefix;
    private final String[] otherPrefixes;
    private final STMTransitionAction<?> defaultAction;
    private boolean useSuffix = false ; // Use a suffix to distinguish between
    // different types of STM components
    private final static String TRANSITION_ACTION_SUFFIX = "Action";
    private final static String POST_SAVE_HOOK_SUFFIX = "PostSaveHook";
    private final static String AUTO_STATE_SUFFIX = "AutoState";

    @Autowired private ApplicationContext applicationContext;
    @Autowired private ContextContainer contextContainer;

    public STMTransitionActionResolver(String prefix) {
        this(prefix, null);
    }

    public STMTransitionActionResolver(String prefix, STMTransitionAction<?> defaultAction) {
        this(prefix, defaultAction, false,new String[0]);
    }

    public STMTransitionActionResolver(String prefix, boolean useSuffix , STMTransitionAction<?> defaultAction) {
        this(prefix, defaultAction, useSuffix, new String[0]);
    }

    public STMTransitionActionResolver(String prefix, STMTransitionAction<?> defaultAction, String... otherPrefixes) {
        this(prefix, defaultAction, false, otherPrefixes);
    }

    public STMTransitionActionResolver(String prefix, STMTransitionAction<?> defaultAction,
                                       boolean useSuffix, String... otherPrefixes) {
        this.prefix = prefix != null ? prefix : "";
        this.defaultAction = defaultAction;
        this.useSuffix = useSuffix;
        this.otherPrefixes = otherPrefixes != null ? otherPrefixes : new String[0];
    }

    public STMAutomaticStateComputation<?> resolveAutomaticStateDescriptor(String actionStateName){
        return (STMAutomaticStateComputation<?>) internallyResolveBean(actionStateName,ComponentType.AUTO_STATE);
    }

    public PostSaveHook<?> resolvePostSaveHook(String actionStateName){
        return (PostSaveHook<?>) internallyResolveBean(actionStateName,ComponentType.POST_SAVE_HOOK);
    }

    public STMTransitionAction<?> getBean(String eventId) {
        STMTransitionAction<?> action = (STMTransitionAction<?>) internallyResolveBean(eventId,ComponentType.TRANSITION_ACTION);
        return (action == null)? defaultAction : action;
    }

    private Object internallyResolveBean(String name,ComponentType componentType) {
        try {
            String contextBasedPrefix = resolvePrefixFromContext();
            String beanNameWithContextPrefix = buildBeanName(contextBasedPrefix, name,componentType);

            if (applicationContext.containsBean(beanNameWithContextPrefix)) {
                return applicationContext.getBean(beanNameWithContextPrefix);
            }

            String fallbackBeanName = buildBeanName(prefix, name,componentType);
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

    private String buildBeanName(String resolvedPrefix, String eventId,ComponentType componentType) {
        String suffix = "";
        if (useSuffix){
            suffix = componentType.suffix();
        }
        return (resolvedPrefix == null || resolvedPrefix.isEmpty())
                ? (eventId + suffix)
                : (resolvedPrefix + capitalize(eventId) + suffix);
    }


}
