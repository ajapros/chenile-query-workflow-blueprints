package org.chenile.query.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.chenile.base.exception.BadRequestException;
import org.chenile.base.exception.ServerException;
import org.chenile.query.model.QueryMetadata;
import org.chenile.query.model.SortCriterion;
import org.chenile.query.service.error.ErrorCodes;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MybatisQueryExecutionProvider implements QueryExecutionProvider {
	public static final String PROVIDER_NAME = "mybatis";
	private final Logger logger = LoggerFactory.getLogger(MybatisQueryExecutionProvider.class);
	private final SqlSessionTemplate sessionTemplate;

	public MybatisQueryExecutionProvider(SqlSessionTemplate sessionTemplate) {
		this.sessionTemplate = sessionTemplate;
	}

	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	@Override
	public void applySort(Map<String, Object> filters, List<SortCriterion> sortCriteria, QueryMetadata queryMetadata) {
		if (!queryMetadata.isSortable()) {
			return;
		}
		String orderBy = "order by 1 ASC";
		if (sortCriteria == null || sortCriteria.isEmpty()) {
			filters.put(ORDER_BY_PART, orderBy);
			return;
		}

		int counter = 0;
		int sortCriteriaSize = sortCriteria.size();
		StringBuilder orderByStringBuilder = new StringBuilder("order by ");
		for (SortCriterion sortCriterion : sortCriteria) {
			counter++;
			if (StringUtils.isNotEmpty(sortCriterion.getName())) {
				orderByStringBuilder.append(sortCriterion.getName())
						.append(sortCriterion.isAscendingOrder() ? " ASC " : " DESC ");
			} else {
				orderByStringBuilder.append(sortCriterion.getIndex())
						.append(sortCriterion.isAscendingOrder() ? " ASC " : " DESC ");
			}

			if (counter != sortCriteriaSize) {
				orderByStringBuilder.append(", ");
			}
		}
		filters.put(ORDER_BY_PART, orderByStringBuilder.toString());
	}

	@Override
	public void applyPagination(Map<String, Object> filters, int startRow, int numRowsInPage) {
		filters.put(PAGINATION_PART, "limit " + numRowsInPage + " offset " + (startRow - 1));
	}

	@Override
	public Object executeCount(String queryName, Map<String, Object> filters) {
		try {
			return sessionTemplate.selectOne(queryName, filters);
		} catch(BadRequestException e) {
			throw e;
		} catch(Exception e) {
			BadRequestException badRequestException = findBadRequestException(e);
			if (badRequestException != null) {
				throw badRequestException;
			}
			throw new ServerException(ErrorCodes.CANNOT_EXECUTE_COUNT_QUERY.getSubError(),
					new Object[]{queryName, filters, e.getMessage()}, e);
		}
	}

	@Override
	public List<Object> executeQuery(String queryName, Map<String, Object> filters) {
		try {
			return sessionTemplate.selectList(queryName, filters);
		} catch(BadRequestException e) {
			throw e;
		} catch(Exception e) {
			BadRequestException badRequestException = findBadRequestException(e);
			if (badRequestException != null) {
				throw badRequestException;
			}
			logger.error("Cannot execute query", e);
			throw new ServerException(ErrorCodes.CANNOT_EXECUTE_QUERY.getSubError(),
					new Object[]{queryName, filters, e.getMessage()}, e);
		}
	}

	private BadRequestException findBadRequestException(Throwable throwable) {
		while (throwable != null) {
			if (throwable instanceof BadRequestException badRequestException) {
				return badRequestException;
			}
			throwable = throwable.getCause();
		}
		return null;
	}
}
