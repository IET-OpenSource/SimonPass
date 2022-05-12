package it.iet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class ReadFromJson {

    ParseOptions parseOptions;

    public ReadFromJson() {
        this.parseOptions = new ParseOptions();
        this.parseOptions.setResolve(true); // implicit
        this.parseOptions.setResolveFully(true);
    }

    public OpenAPI getJsonContent(String fileName) {
        return new OpenAPIV3Parser().read(fileName, null, parseOptions);
    }


}
