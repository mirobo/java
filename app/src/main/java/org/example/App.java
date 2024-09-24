
package org.example;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openapi4j.core.model.v3.OAI3;
import org.openapi4j.core.model.v3.OAI3Context;
import org.openapi4j.core.util.TreeUtil;
import org.openapi4j.operation.validator.model.impl.Body;
import org.openapi4j.operation.validator.validation.OperationValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.schema.validator.ValidationContext;
import org.openapi4j.schema.validator.ValidationData;
import org.openapi4j.schema.validator.v3.ValidationOptions;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.parser.core.models.ParseOptions;

public class App {

  public static void main(String[] args) throws Exception {

    validateReponse("""
        {
          "bark": true,
          "breed": "Dingo"
        }
        """, "./src/main/resources/oas.yaml", "/oneOf");

    validateReponse("""
        {
          "result": {
            "whatever": "so what"
          },
          "messages": {
            "extendedErrorModel": {
              "code": 100,
              "message": "hoi",
              "rootCause": "hey"
            }
          }
        }
        """, "./src/main/resources/oas.yaml", "/allOf");

  }

  public static void validateReponse(String response, String oasSpecFile, String path) throws Exception {

    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolveFully(true);
    parseOptions.setResolveCombinators(true);

    OpenApiInteractionValidator val = OpenApiInteractionValidator.createForSpecificationUrl(oasSpecFile)
        .withParseOptions(parseOptions).build();

    // .withLevelResolver(
    // LevelResolver.create()
    // .withLevel("validation.request.body.schema.additionalProperties",
    // ValidationReport.Level.IGNORE)
    // .build())

    // atlassian swagger request validator
    Response res = SimpleResponse.Builder.ok().withBody(response).withContentType("application/json").build();
    ValidationReport report = val.validateResponse(path, Method.GET, res);

    System.out.println("Validate response: " + response);
    System.out.println("\r\natlassian validator");
    System.out.println("has errors: " + report.hasErrors());
    report.getMessages().forEach(System.out::println);

    // openapi4j validator
    URL OAS_SPEC_FILE_AS_URL = new File(oasSpecFile).getAbsoluteFile().toURI().toURL();
    OpenApi3 api = new OpenApi3Parser().parse(new File(oasSpecFile), true);
    JsonNode schemaNode = TreeUtil.load(OAS_SPEC_FILE_AS_URL);
    OAI3Context apiContext = new OAI3Context(OAS_SPEC_FILE_AS_URL, schemaNode);
    ValidationContext<OAI3> validationContext = new ValidationContext<>(apiContext);
    validationContext.setOption(ValidationOptions.ADDITIONAL_PROPS_RESTRICT, true);
    OperationValidator val2 = new OperationValidator(validationContext, api, api.getPath(path),
        api.getPath(path).getOperation("get"));
    ValidationData<Void> validationData = new ValidationData<>();
    val2.validateResponse(new Response2(response), validationData);
    System.out.println("\r\nopenapi4j validator");
    System.out.println("has errors: " + !validationData.results().isValid());
    validationData.results().items().forEach(System.out::println);
  }

}

class Response2 implements org.openapi4j.operation.validator.model.Response {

  String response;

  public Response2(String response) {
    this.response = response;
  }

  @Override
  public int getStatus() {
    return 200;
  }

  @Override
  public Body getBody() {
    return Body.from(this.response);
  }

  @Override
  public Map<String, Collection<String>> getHeaders() {
    return new HashMap<>();
  }

  @Override
  public Collection<String> getHeaderValues(String name) {
    List<String> headers = new ArrayList<>();
    if ("Content-Type".equals(name)) {
      headers.add("application/json");
    }
    return headers;
  }

}