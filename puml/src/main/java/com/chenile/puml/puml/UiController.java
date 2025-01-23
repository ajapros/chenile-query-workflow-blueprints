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

@Controller
public class UiController {
    private final CLIHelper cliHelper = new CLIHelper();

    @GetMapping("/")
    public String showSignUpForm(@Valid @ModelAttribute("formData") InputModel inputModel, Model model) {
        model.addAttribute("inputModel", inputModel);
        return "index";
    }

    @PostMapping("/convert")
    public String convert(@Valid @ModelAttribute("formData") InputModel inputModel,
                          BindingResult result, Model model) {
        // System.out.println(inputModel);
        model.addAttribute("inputModel", inputModel);
        try {
            model.addAttribute("imageData", getImage(inputModel));
        }
        catch (Exception e){
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }

    private String getImage(InputModel inputModel){
        String script;
        try {
            script = generatePuml(inputModel);
        } catch (Exception e) {
            throw new FileProcessingException("Error generating UML script. Message = " + e.getMessage(), e);
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(script.replace(';', '\n'));
            reader.outputImage(bos, new FileFormatOption(FileFormat.PNG, false));
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

}
