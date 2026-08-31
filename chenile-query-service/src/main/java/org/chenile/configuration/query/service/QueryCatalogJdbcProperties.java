package org.chenile.configuration.query.service;

/** Settings for the optional, startup-loaded JDBC query catalog. */
public class QueryCatalogJdbcProperties {
	/** Disabled by default so existing classpath-only applications are unchanged. */
	private boolean enabled;
	/** A dedicated, read-only {@link javax.sql.DataSource} bean supplied by the application. */
	private String dataSource = "queryCatalogDataSource";
	/** Database value that represents a base (non-tenant-specific) configuration. */
	private String baseScope = "__base__";

	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	public String getDataSource() { return dataSource; }
	public void setDataSource(String dataSource) { this.dataSource = dataSource; }
	public String getBaseScope() { return baseScope; }
	public void setBaseScope(String baseScope) { this.baseScope = baseScope; }
}
