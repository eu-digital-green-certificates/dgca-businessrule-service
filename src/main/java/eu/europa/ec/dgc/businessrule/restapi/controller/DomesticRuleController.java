/*-
 * ---license-start
 * eu-digital-green-certificates / dgca-businessrule-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc.businessrule.restapi.controller;

import eu.europa.ec.dgc.businessrule.entity.SignedListEntity;
import eu.europa.ec.dgc.businessrule.exception.DgcaBusinessRulesResponseException;
import eu.europa.ec.dgc.businessrule.model.DomesticRuleItem;
import eu.europa.ec.dgc.businessrule.restapi.dto.DomesticRuleListItemDto;
import eu.europa.ec.dgc.businessrule.restapi.dto.ProblemReportDto;
import eu.europa.ec.dgc.businessrule.service.DomesticRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/bnrules")
@Slf4j
@ConditionalOnExpression("${dgc.domestic-mode.enabled:false} == true")
@RequiredArgsConstructor
public class DomesticRuleController {

    private static final String API_VERSION_HEADER = "X-VERSION";

    public static final String X_SIGNATURE_HEADER = "X-SIGNATURE";

    private final DomesticRuleService domesticRuleService;

    /**
     * Http Method for getting the rules list.
     */
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Gets the a list of all booster notification rule ids and hash values.",
        description = "This method returns a list containing the ids and hash values of all booster notification "
            + "rules. The hash value can be used to check, if a rule has changed and needs to be updated. "
            + "The hash value can also be used to download a specific rule afterwards.",
        tags = {"Booster Notification Rules"},
        parameters = {
            @Parameter(
                in = ParameterIn.HEADER,
                name = "X-VERSION",
                description = "Version of the API. In preparation of changes in the future. Set it to \"1.0\"",
                required = true,
                schema = @Schema(implementation = String.class))
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Returns a list of all rule ids and hash values.",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = DomesticRuleListItemDto.class))))
        }
    )
    public ResponseEntity<List<DomesticRuleListItemDto>> getRules(
        @RequestHeader(value = API_VERSION_HEADER, required = false) String apiVersion
    ) {
        Optional<SignedListEntity> rulesList = domesticRuleService.getRulesSignedList();
        ResponseEntity responseEntity;
        if (rulesList.isPresent()) {
            ResponseEntity.BodyBuilder respBuilder = ResponseEntity.ok();
            String signature = rulesList.get().getSignature();
            if (signature != null && signature.length() > 0) {
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set(X_SIGNATURE_HEADER, signature);
                respBuilder.headers(responseHeaders);
            }
            responseEntity = respBuilder.body(rulesList.get().getRawData());
        } else {
            responseEntity = ResponseEntity.ok(domesticRuleService.getRulesList());
        }
        return responseEntity;
    }


    /**
     * Http Method for getting  specific rule set .
     */
    @GetMapping(path = "/{hash}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Gets a specific rule by its  hash value.",
        description = "This method can be used to download a specific rule. Therefore the hash value "
            + "of the rule must be provided as path parameter.",
        tags = {"Booster Notification Rules"},
        parameters = {
            @Parameter(
                in = ParameterIn.PATH,
                name = "hash",
                description = "Hash of the rule to download.",
                required = true,
                schema = @Schema(implementation = String.class)),
            @Parameter(
                in = ParameterIn.HEADER,
                name = "X-VERSION",
                description = "Version of the API. In preparation of changes in the future.",
                required = true,
                schema = @Schema(implementation = String.class))
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Returns the specified rule.",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class),
                    examples = {
                        @ExampleObject(value = "{\n"
                            + "  \"Identifier\": \"VR-DE-1\",\n"
                            + "  \"Version\": \"1.0.0\",\n"
                            + "  \"SchemaVersion\":\"1.0.0\",\n"
                            + "  \"Engine\":\"CERTLOGIC\",\n"
                            + "  \"EngineVersion\":\"1.0.0\",\n"
                            + "  \"Type\":\"Acceptance\",\n"
                            + "  \"Country\":\"DE\",\n"
                            + "  \"CertificateType\":\"Vaccination\",\n"
                            + "  \"Description\":[{\"lang\":\"en\",\"desc\":\"Vaccination must be from June and "
                            + "doses must be 2\"}],\n"
                            + "  \"ValidFrom\":\"2021-06-27T07:46:40Z\",\n"
                            + "  \"ValidTo\":\"2021-08-01T07:46:40Z\",\n"
                            + "  \"AffectedFields\":[\"dt\",\"dn\"],\n"
                            + "  \"Logic\":{\n"
                            + "    \"and\": [\n"
                            + "      {\">=\":[ {\"var\":\"dt\"}, \"2021-06-01T00:00:00Z\" ]},\n"
                            + "      {\">=\":[ {\"var\":\"dn\"}, 2 ]}\n"
                            + "    ]\n"
                            + "  }\n"
                            + "}")
                    })),
            @ApiResponse(
                responseCode = "404",
                description = "Rule could not be found for the given hash value.",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemReportDto.class)))
        })
    public ResponseEntity<String> getRuleByHash(
        @RequestHeader(value = API_VERSION_HEADER, required = false) String apiVersion,
        @Valid @PathVariable("hash") String hash
    ) {
        ResponseEntity<String> responseEntity;

        if (hash == null || hash.isBlank()) {
            throw new DgcaBusinessRulesResponseException(HttpStatus.BAD_REQUEST, "0x005", "Possible reasons: "
                + "The provided hash value is not correct", hash, "");
        }
        DomesticRuleItem rule =
            domesticRuleService.getRuleByHash(hash);

        if (rule == null) {
            throw new DgcaBusinessRulesResponseException(HttpStatus.NOT_FOUND, "0x006", "Possible reasons: "
                + "The provided hash may not be correct.", "hash: " + hash, "");
        }

        if (rule.getSignature() != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(DomesticRuleController.X_SIGNATURE_HEADER, rule.getSignature());
            responseEntity = ResponseEntity.ok().headers(responseHeaders).body(rule.getRawData());
        } else {
            responseEntity = ResponseEntity.ok(rule.getRawData());
        }

        return responseEntity;
    }


}
