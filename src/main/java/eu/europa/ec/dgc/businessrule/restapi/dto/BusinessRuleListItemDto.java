package eu.europa.ec.dgc.businessrule.restapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema(
    name = "BusinessRuleListItem",
    type = "object",
    example = "{"
        + "\"identifier\":\"VR-DE-1\","
        + "\"version\":\"1.0.0\","
        + "\"country\":\"DE\","
        + "\"hash\":\"6821d518570fe9f4417c482ff0d2582a7b6440f243a9034f812e0d71611b611f\""
        + "}"
)

@Value
public class BusinessRuleListItemDto {
    String identifier;
    String version;
    String country;
    String hash;
}
