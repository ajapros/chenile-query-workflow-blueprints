package org.chenile.workflow.cli;

import org.chenile.stm.ConfigProvider;
import org.chenile.stm.EnablementStrategy;
import org.chenile.stm.State;
import org.chenile.stm.dummy.DummyStore;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.*;
import org.chenile.workflow.puml.STMPlantUmlSDGenerator;
import org.chenile.workflow.testcases.STMTestCaseGenerator;
import org.chenile.workflow.testcases.Testcase;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CLIHelper {

    public void allowedActions(CLIParams params, String outputFile) throws Exception {
        out(allowedActions(params),outputFile);
    }

    public String allowedActions(CLIParams params) throws Exception {
        process(params);
        String defaultFlowId = this.stmFlowStore.getDefaultFlow();
        State state = new State(params.stateForAllowedActions, defaultFlowId);
        List<String> allowedActions = this.infoProvider.getAllowedActions(state);
        return allowedActions.toString();
    }
    public void renderStateDiagram(CLIParams params,String outputFile) throws Exception {
        out(renderStateDiagram(params),outputFile);
    }
    public void renderTestCases(CLIParams params,String outputFile) throws Exception {
        out(renderTestCases(params),outputFile);
    }

    public void toJson(CLIParams params,String outputFile) throws Exception {
        out(toJson(params),outputFile);
    }

    public String toJson(CLIParams params) throws Exception {
        process(params);
        return this.stmFlowStore.toJson();
    }

    public int numTests(CLIParams params) throws Exception {
        process(params);
        return this.stmTestCaseGenerator.buildFlow().size();
    }

    public void renderTestPuml(CLIParams params,String outputFile) throws Exception {
        out(renderTestPuml(params),outputFile);
    }

    public void visualizeTestcaseAsStateDiagram(CLIParams params,String outputDir) throws Exception {
        Collection<String> strings = visualizeTestcaseAsStateDiagram(params).values();
        new File(outputDir).mkdirs();
        int index = 1;
        for (String s:strings){
            String file = outputDir + File.separator + index++ + ".puml";
            out(s,file);
        }
    }

    public Map<String,String> visualizeTestcaseAsStateDiagram(CLIParams params) throws Exception {
        process(params);
        return this.stmTestCaseGenerator.visualizeTestcasesWithStateDiagram();
    }


    public String renderTestPuml(CLIParams params) throws Exception {
        process(params);
        return this.stmTestCaseGenerator.visualizeTestcases();
    }

    public String renderStateDiagram(CLIParams params) throws Exception {
        process(params);
        if (params.stylingFile != null || params.stylingPropertiesText != null){
            loadStylingProperties(params);
        }
        return this.generator.toStateDiagram();
    }

    public String renderTestCases(CLIParams params) throws Exception {
        process(params);
        return this.stmTestCaseGenerator.toTestCase();
    }

    public Map<String,Object> toMap(CLIParams params) throws Exception {
        process(params);
        return this.stmFlowStore.toMap();
    }

    public List<Testcase> renderTestCasesAsObject(CLIParams params) throws Exception {
        process(params);
        return this.stmTestCaseGenerator.buildFlow();
    }

    public void process(CLIParams params) throws Exception {
        if (params.xmlText != null)
            processText(params);
        else if (params.xmlFile != null)
            processXmlFile(params);
    }

    private void processXmlFile(CLIParams params) throws Exception {
        try (InputStream inputStream = Files.newInputStream(params.xmlFile.toPath())) {
            processStream(inputStream,params);
        }
    }

    private void processText(CLIParams params) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(params.xmlText.getBytes())) {
            processStream(inputStream,params);
        }
    }

    private void processStream(InputStream inputStream,CLIParams params) throws Exception {
        STMFlowStoreImpl stmFlowStoreImpl = obtainFlowStore(params);
        XmlFlowReader xfr = new XmlFlowReader(stmFlowStoreImpl);
        xfr.parse(inputStream);
        initProcessors(stmFlowStoreImpl);
    }

    private void out(String s, String outputFile) throws IOException{
        if (outputFile != null && !outputFile.isEmpty()){
            writeFile(s,outputFile);
        }else {
            System.out.println(s);
        }
    }

    public void writeFile(String s,String outputFile)
            throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(s);
        printWriter.close();
    }

    private STMFlowStoreImpl obtainFlowStore(CLIParams params) throws Exception{
        if (params.enablementPropertiesFile == null &&
                params.enablementPropertiesText == null ) return new DummyStore();
        EnablementStrategy enablementStrategy = new ConfigBasedEnablementStrategy(obtainConfigProvider(params),params.prefix);
        DummyStore dummyStore = new DummyStore(){
            @Override
            public EnablementStrategy makeEnablementStrategy(String componentName) throws STMException {
                try{
                    return enablementStrategy;
                }catch(Exception e){
                    throw new STMException("Cannot create enablement strategy", 5000, e);
                }
            }
        };
        dummyStore.setEnablementStrategy(enablementStrategy);
        return dummyStore;

    }

    private ConfigProvider obtainConfigProvider(CLIParams params) throws Exception {
        if (params.enablementPropertiesFile != null)
            return obtainConfigProviderFromFile(params);
        else if (params.enablementPropertiesText != null)
            return obtainConfigProviderFromText(params);
        return null;
    }

    private ConfigProvider obtainConfigProviderFromFile(CLIParams params) throws Exception {
        try (InputStream inputStream = Files.newInputStream(params.enablementPropertiesFile.toPath())){
            return new ConfigProviderImpl(inputStream);
        }
    }

    private ConfigProvider obtainConfigProviderFromText(CLIParams params) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(params.enablementPropertiesText.getBytes())){
            return new ConfigProviderImpl(inputStream);
        }
    }

    private void loadStylingProperties(CLIParams params) throws Exception {
        if (params.stylingFile != null)
            loadStylingPropertiesFromFile(params);
        else if (params.stylingPropertiesText != null)
           loadStylingPropertiesFromText(params);
    }

    private void loadStylingPropertiesFromFile(CLIParams params) throws Exception{
        try (InputStream inputStream = Files.newInputStream(params.stylingFile.toPath())){
            this.generator.pumlStyler.load(inputStream);
        }
    }

    private void loadStylingPropertiesFromText(CLIParams params) throws Exception{
        try (InputStream inputStream = new ByteArrayInputStream(params.stylingPropertiesText.getBytes())){
            this.generator.pumlStyler.load(inputStream);
        }
    }

    private void initProcessors(STMFlowStoreImpl stmFlowStoreImpl) {
        this.generator = new STMPlantUmlSDGenerator(stmFlowStoreImpl);
        this.infoProvider = new STMActionsInfoProvider(stmFlowStoreImpl);
        this.stmTestCaseGenerator = new STMTestCaseGenerator(stmFlowStoreImpl);
        this.stmFlowStore = stmFlowStoreImpl;
    }
    private STMPlantUmlSDGenerator generator;
    private STMActionsInfoProvider infoProvider;
    private STMFlowStoreImpl stmFlowStore;
    private STMTestCaseGenerator stmTestCaseGenerator;
}
