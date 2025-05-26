package org.chenile.workflow.cli;

import picocli.CommandLine;
import java.io.File;
import static picocli.CommandLine.*;

@Command(name = "stm-cli", mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        description = "Reads a State Definition file and allows a few operations on it.\n " +
            "STM is not created. Hence components don't have to be in the class path.")
public class CLI implements Runnable {
    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;
    static class Exclusive {
        @Option(names = {"-s", "--uml-state-diagram"}, description = "Generate a UML state diagram")
        private boolean umlStateDiagram;
        @Option(names = {"-a", "--allowed-actions"},paramLabel = "state", description = "Return allowed actions for a state")
        private String stateForAllowedActions;
        @Option(names = {"-j", "--render-json"},paramLabel = "render-json", description = "Return a JSON representation of the XML file")
        private boolean toJson;
        @Option(names = {"-t", "--generate-test-cases"},paramLabel = "gen-test-cases", description = "Generates test cases")
        private boolean genTestCase;
        @Option(names = {"-T", "--visualize-test-cases"},paramLabel = "visualize-test-cases", description = "Visualizes test cases")
        private boolean visualizeTestCase;
        @Option(names = {"-r", "--render-tests-as-state"},paramLabel = "render-tests-as-state", description = "Renders state diagrams for all generated test cases")
        private boolean renderTestsAsStateDiagram = false;
    }
    @Parameters(index = "0..*", paramLabel = "<XML File names>", description = "The XML filename to read. Must be a valid states XML. Component names in file will be ignored.")
    private File[] xmlFileNames;
    @Option(names = {"-o", "--output"},paramLabel = "output-file-or-directory", description = "Writes output to the specified file or directory for multiple files")
    private String outputFile;
    @Option(names = {"-S", "--styling"},paramLabel = "Styling-rules-file", description = "Use the JSON file for setting styles according to metadata in states and transitions")
    private File stylingFile;
    @Option(names = {"-e", "--enablement"},paramLabel = "enablement-properties-file", description = "Use the properties file for enablement properties")
    private File enablementPropertiesFile;
    @Option(names = {"-p", "--prefix"},paramLabel = "prefix", description = "The prefix for all properties")
    private String prefix;
    private final CLIHelper cliHelper = new CLIHelper();

    public static void main(String... args) {
        System.exit(new CommandLine(new CLI()).execute(args));
    }
    @Override
    public void run() {
        CLIParams params = makeCLIParams();
        try {
            if (exclusive.umlStateDiagram) {
                cliHelper.renderStateDiagram(params,outputFile);
            } else if (exclusive.stateForAllowedActions != null && !exclusive.stateForAllowedActions.isEmpty()) {
                cliHelper.allowedActions(params,outputFile);
            } else if (exclusive.toJson) {
                cliHelper.toJson(params,outputFile);
            } else if (exclusive.genTestCase) {
                cliHelper.renderTestCases(params,outputFile);
            } else if (exclusive.visualizeTestCase) {
                cliHelper.renderTestPuml(params,outputFile);
            } else if (exclusive.renderTestsAsStateDiagram) {
                cliHelper.visualizeTestcaseAsStateDiagram(params,outputFile);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CLIParams makeCLIParams() {
        CLIParams params = new CLIParams();
        params.enablementPropertiesFile = this.enablementPropertiesFile;
        params.prefix = this.prefix;
        params.stateForAllowedActions = this.exclusive.stateForAllowedActions;
        params.stylingFile = this.stylingFile;
        params.xmlFiles = xmlFileNames;
        return params;
    }
}
