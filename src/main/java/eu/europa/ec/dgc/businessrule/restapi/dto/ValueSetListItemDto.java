package eu.europa.ec.dgc.businessrule.restapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema(
    name = "ValueSetListItem",
    type = "object",
    example = "{"
        + "\"id\":\"disease-agent-targeted\","
        + "\"hash\":\"d4bfba1fd9f2eb29dfb2938220468ccb0b481d348f192e6015d36da4b911a83a\","
        + "}"
)

@Value
public class ValueSetListItemDto {
    String id;
    String hash;
}
