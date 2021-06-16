package eu.europa.ec.dgc.businessrule.restapi.dto;

import lombok.Value;

@Value
public class BusinessRuleListItemDto {
    String id;
    String country;
    String hash;
}
