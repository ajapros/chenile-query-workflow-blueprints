package com.chenile.puml.puml;

import com.chenile.puml.puml.ui.InputModel;
import jakarta.validation.Valid;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.chenile.workflow.cli.CLIHelper;
import org.chenile.workflow.cli.CLIParams;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Controller
public class UiController {
    private final CLIHelper cliHelper = new CLIHelper();

    @GetMapping("/")
    public String showSignUpForm(@Valid @ModelAttribute("formData") InputModel inputModel, Model model) {
        model.addAttribute("inputModel", inputModel);
        return "index";
    }

    @GetMapping("/tc")
    public String showTestcase(@Valid @ModelAttribute("formData") InputModel inputModel, Model model) {
        model.addAttribute("inputModel", inputModel);
        return "tc";
    }

    @PostMapping("/convert")
    public String convert(@Valid @ModelAttribute("formData") InputModel inputModel,
                          BindingResult result, Model model) {
        model.addAttribute("inputModel", inputModel);
        try {
            model.addAttribute("imageData", getImage(inputModel));
            model.addAttribute("testImageData", testcaseDiagram(inputModel));
            Map<String,String>allTests= numTests(inputModel);
            model.addAttribute("imageTabs",allTests);
        }
        catch (Exception e){
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }

    @PostMapping("/visualize")
    public String visualize(@Valid @ModelAttribute("formData") InputModel inputModel,
                          BindingResult result, Model model) {
        model.addAttribute("inputModel", inputModel);
        try {
            model.addAttribute("imageData", testcaseDiagram(inputModel));

        }
        catch (Exception e){
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
        }
        return "tc";
    }

    private String getImage(InputModel inputModel) throws Exception{
        String script = generatePuml(inputModel);
        return makeImageFromScript(script.replace(';', '\n'));
    }

    private String testcaseDiagram(InputModel inputModel) throws Exception{
        return makeImageFromScript(visualizeTestcases(inputModel));
    }

    private String makeImageFromScript(String script){
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(script);
            reader.generateImage(bos, new FileFormatOption(FileFormat.PNG));
            byte[] array = bos.toByteArray();

            ByteArrayResource resource = new ByteArrayResource(array);
            return Base64.getEncoder().encodeToString( resource.getByteArray());
        } catch (Exception e) {
            throw new FileProcessingException("Error generating image from UML script.", e);
        }
    }


    private String generatePuml(InputModel inputModel) throws Exception{
        CLIParams params = new CLIParams();
        params.xmlText = inputModel.getStmXml();
        if (inputModel.getEnablementProperties() != null &&
                !inputModel.getEnablementProperties().isEmpty())
            params.enablementPropertiesText = inputModel.getEnablementProperties();
        if (inputModel.getStylingProperties() != null &&
                !inputModel.getStylingProperties().isEmpty())
            params.stylingPropertiesText = inputModel.getStylingProperties();
        params.prefix = inputModel.getPrefix();
        return cliHelper.renderStateDiagram(params);
    }

    private String visualizeTestcases(InputModel inputModel) throws Exception{
        CLIParams params = new CLIParams();
        params.xmlText = inputModel.getStmXml();
        return cliHelper.renderTestPuml(params);
    }

    private Map<String,String> numTests(InputModel inputModel) throws Exception{
        CLIParams params = new CLIParams();
        params.xmlText = inputModel.getStmXml();

        Map<String,String> allTests = cliHelper.visualizeTestcaseAsStateDiagram(params);
        allTests.forEach((key, value) -> allTests.replace(key, makeImageFromScript(value)));
        return allTests;
        //return cliHelper.numTests(params);

    }
}
