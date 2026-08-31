package org.chenile.query.service.catalog;

import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/** Reads only the root MyBatis mapper namespace before MyBatis compiles the document. */
final class MapperNamespaceReader {
	private MapperNamespaceReader() { }

	static String namespace(Resource resource) {
		try (InputStream input = resource.getInputStream()) {
			XPathParser parser = new XPathParser(input, false, null, new XMLMapperEntityResolver());
			XNode mapper = parser.evalNode("/mapper");
			if (mapper == null || mapper.getStringAttribute("namespace") == null
					|| mapper.getStringAttribute("namespace").isBlank()) {
				throw new IllegalStateException("MyBatis mapper has no namespace: " + resource.getDescription());
			}
			return mapper.getStringAttribute("namespace").trim();
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read MyBatis mapper: " + resource.getDescription(), e);
		}
	}
}
