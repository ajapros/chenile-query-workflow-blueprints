package org.chenile.workflow.service.stmcmds;

import org.chenile.core.model.ChenileServiceDefinition;
import org.chenile.core.model.OperationDefinition;
import org.chenile.mcp.model.ChenilePolymorphProvider;
import org.chenile.mcp.model.ChenilePolymorphVariant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MCP polymorph provider for the StateEntityService processById operation.
 * It exposes one variant per workflow event using payload metadata from StmBodyTypeSelector.
 */
public class ProcessIdPolymorph implements ChenilePolymorphProvider {
	private final String prefix;
	private final StmBodyTypeSelector stmBodyTypeSelector;

	public ProcessIdPolymorph(String prefix, StmBodyTypeSelector stmBodyTypeSelector) {
		this.prefix = prefix;
		this.stmBodyTypeSelector = stmBodyTypeSelector;
	}

	@Override
	public List<ChenilePolymorphVariant> getVariants(ChenileServiceDefinition serviceDefinition,
			OperationDefinition operationDefinition) {
		assert operationDefinition.getParams().size() == 3;
		String secondParamName = operationDefinition.getParams().get(1).getName();
		String thirdParamName = operationDefinition.getParams().get(2).getName();
		List<ChenilePolymorphVariant> variants = new ArrayList<>();
		stmBodyTypeSelector.getConfigs().forEach((event, eventData) -> variants.add(new ChenilePolymorphVariant(
				prefix + "_" + event,
				eventData.description(),
				Map.of(thirdParamName, eventData.typeReference()),
				Map.of(),
				Map.of(thirdParamName, eventData.description()),
				Map.of(secondParamName, event))));
		return variants;
	}
}
