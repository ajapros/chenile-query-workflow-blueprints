package org.chenile.workflow.service.store;

import org.chenile.utils.entity.service.EntityStore;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Discovers the {@link EntityStore} contracts implemented by a store bean.
 *
 * <p>Every method declared by {@code EntityStore}, or by an application interface that extends
 * it, is eligible for tenant-aware dispatch. Methods declared only on an implementation class are
 * intentionally excluded.</p>
 */
final class EntityStoreOperationContract {

    private final List<Operation> operations;

    private EntityStoreOperationContract(List<Operation> operations) {
        this.operations = operations;
    }

    static EntityStoreOperationContract forStore(Class<?> storeType) {
        Set<Method> contractMethods = new LinkedHashSet<>();
        for (Class<?> storeInterface : ClassUtils.getAllInterfacesForClassAsSet(storeType)) {
            if (EntityStore.class.isAssignableFrom(storeInterface)) {
                contractMethods.addAll(Arrays.asList(storeInterface.getMethods()));
            }
        }

        List<Operation> operations = new ArrayList<>();
        for (Method contractMethod : contractMethods) {
            if (contractMethod.getDeclaringClass() != Object.class) {
                operations.add(new Operation(contractMethod, resolvedParameterTypes(contractMethod, storeType)));
            }
        }
        return new EntityStoreOperationContract(operations);
    }

    Optional<Operation> find(Method invokedMethod) {
        Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(invokedMethod);
        return operations.stream().filter(operation -> operation.matches(bridgedMethod)).findFirst();
    }

    private static Class<?>[] resolvedParameterTypes(Method method, Class<?> implementationType) {
        Class<?>[] parameterTypes = new Class<?>[method.getParameterCount()];
        for (int index = 0; index < parameterTypes.length; index++) {
            parameterTypes[index] = ResolvableType.forMethodParameter(method, index, implementationType)
                    .resolve(method.getParameterTypes()[index]);
        }
        return parameterTypes;
    }

    static final class Operation {
        private final Method method;
        private final Class<?>[] parameterTypes;

        private Operation(Method method, Class<?>[] parameterTypes) {
            this.method = method;
            this.parameterTypes = parameterTypes;
        }

        boolean matches(Method invokedMethod) {
            return method.getName().equals(invokedMethod.getName())
                    && Arrays.equals(parameterTypes, invokedMethod.getParameterTypes());
        }

        boolean isSupportedBy(EntityStore<?> store) {
            return method.getDeclaringClass().isInstance(store);
        }

        Object invoke(EntityStore<?> store, Object[] arguments) throws Throwable {
            try {
                return method.invoke(store, arguments);
            } catch (InvocationTargetException exception) {
                throw exception.getTargetException();
            }
        }
    }
}
