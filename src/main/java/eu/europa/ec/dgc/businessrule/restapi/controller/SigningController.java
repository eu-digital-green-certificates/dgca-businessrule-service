package eu.europa.ec.dgc.businessrule.restapi.controller;

import eu.europa.ec.dgc.businessrule.service.SigningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publickey")
@Slf4j
@RequiredArgsConstructor
public class SigningController {
    private final Optional<SigningService> signingService;

    /**
     * Http Method for getting the business rules list.
     */
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Gets the signing public key (der base64 encoded)",
            description = "Gets the signing public key (der base64 encoded)",
            tags = {"Business Rules"},
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "public key"),
                @ApiResponse(
                        responseCode = "404",
                        description = "signing not supported"),
            }
    )
    public ResponseEntity<String> getPublicKey() {
        if (signingService.isPresent()) {
            return ResponseEntity.ok(signingService.get().getPublicKey());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
