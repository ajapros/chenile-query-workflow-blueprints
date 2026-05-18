package org.chenile.configuration.workflow.info.service;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

final class PlantUmlToImageConverter {
    private PlantUmlToImageConverter() {
    }

    static byte[] toPng(String plantUml) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(plantUml.replace(';', '\n'));
            reader.generateImage(outputStream, new FileFormatOption(FileFormat.PNG, false));
            return outputStream.toByteArray();
        }
    }

    static Map<String, byte[]> toPngMap(Map<String, String> plantUmlMap) throws Exception {
        Map<String, byte[]> output = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : plantUmlMap.entrySet()) {
            output.put(entry.getKey(), toPng(entry.getValue()));
        }
        return output;
    }
}
