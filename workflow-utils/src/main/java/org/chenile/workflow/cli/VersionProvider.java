package org.chenile.workflow.cli;

import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Model;
import picocli.CommandLine.Spec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class VersionProvider implements IVersionProvider {
    @Spec
    Model.CommandSpec spec;

    public String[] getVersion() {
        return new String[] {  spec.qualifiedName() + ": " + obtainVersion("cli-version.txt") };
    }

    public String obtainVersion(String resourcePath)  {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) return "unknown";
            try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(streamReader)) {
                return reader.readLine();
            }
        } catch (IOException e){
            return "unknown";
        }
    }
}