package org.chenile.configuration.query.service;

import org.chenile.base.exception.BadRequestException;
import org.chenile.core.context.ContextContainer;
import org.chenile.query.service.error.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryTenantResolver {
	private static final Logger logger = LoggerFactory.getLogger(QueryTenantResolver.class);

	private final QueryDatasourcesProperties properties;
	private final ContextContainer contextContainer;

	public QueryTenantResolver(QueryDatasourcesProperties properties, ContextContainer contextContainer) {
		this.properties = properties;
		this.contextContainer = contextContainer;
	}

	public String resolveTenant() {
		String tenantId = normalize(contextContainer.getTenant());
		if (tenantId != null) {
			return tenantId;
		}
		String defaultTenantId = getDefaultTenantId();
		if (defaultTenantId != null) {
			logger.warn("No query tenant was provided. Falling back to query.defaultTenantId '" + defaultTenantId + "'.");
			return defaultTenantId;
		}
		throw new BadRequestException(ErrorCodes.MISSING_TENANT.getSubError(),
				new Object[] {"x-chenile-tenant-id", "query.defaultTenantId"});
	}

	public String getDefaultTenantId() {
		return normalize(properties.getDefaultTenantId());
	}

	public static String normalize(String tenantId) {
		if (tenantId == null) {
			return null;
		}
		String normalized = tenantId.trim();
		return normalized.isEmpty() ? null : normalized;
	}
}
