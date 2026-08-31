package org.chenile.workflow.service.store;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.chenile.utils.entity.service.EntityStore;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Makes every Spring-managed {@link EntityStore} tenant-aware without requiring changes to
 * the workflow module that injects the base store. For a base bean named
 * {@code vehicleEntityStore}, tenant {@code tenant0} resolves a bean named
 * {@code tenant0VehicleEntityStore}. When that bean is absent, the original store is used.
 */
public class TenantEntityStoreBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof EntityStore<?>)) {
            return bean;
        }

        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new TenantEntityStoreInterceptor(
                new TenantEntityStoreResolver(beanFactory), beanName,
                EntityStoreOperationContract.forStore(bean.getClass())));
        return proxyFactory.getProxy(bean.getClass().getClassLoader());
    }

    private static final class TenantEntityStoreInterceptor implements MethodInterceptor {
        private final TenantEntityStoreResolver tenantStoreResolver;
        private final String baseBeanName;
        private final EntityStoreOperationContract operationContract;

        private TenantEntityStoreInterceptor(TenantEntityStoreResolver tenantStoreResolver,
                                             String baseBeanName,
                                             EntityStoreOperationContract operationContract) {
            this.tenantStoreResolver = tenantStoreResolver;
            this.baseBeanName = baseBeanName;
            this.operationContract = operationContract;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            EntityStoreOperationContract.Operation operation = operationContract
                    .find(invocation.getMethod()).orElse(null);
            if (operation == null) {
                return invocation.proceed();
            }

            EntityStore<?> tenantStore = tenantStoreResolver.resolve(baseBeanName);
            if (tenantStore == null || !operation.isSupportedBy(tenantStore)) {
                return invocation.proceed();
            }
            return operation.invoke(tenantStore, invocation.getArguments());
        }
    }
}
