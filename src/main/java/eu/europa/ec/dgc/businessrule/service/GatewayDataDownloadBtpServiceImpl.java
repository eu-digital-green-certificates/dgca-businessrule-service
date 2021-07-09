package eu.europa.ec.dgc.businessrule.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import eu.europa.ec.dgc.businessrule.model.BusinessRuleItem;
import eu.europa.ec.dgc.businessrule.model.ValueSetItem;
import eu.europa.ec.dgc.businessrule.utils.btp.JsonNodeDeserializer;
import eu.europa.ec.dgc.gateway.connector.dto.TrustListItemDto;
import eu.europa.ec.dgc.gateway.connector.dto.ValidationRuleDto;
import eu.europa.ec.dgc.gateway.connector.model.ValidationRule;
import eu.europa.ec.dgc.signing.SignedStringMessageParser;
import eu.europa.ec.dgc.utils.CertificateUtils;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("btp")
public class GatewayDataDownloadBtpServiceImpl implements GatewayDataDownloadService {

    private static final String DGCG_DESTINATION = "dgcg-destination";
    private static final String DCCG_UPLOAD_CERTS_ENDPOINT = "/trustList/UPLOAD";
    private static final String DCCG_BUSINESS_RULES_ENDPOINT = "/rules";
    private static final String DCCG_VALUE_SETS_ENDPOINT = "/valuesets";
    private static final String DCCG_COUNTRY_LIST_ENDPOINT = "/countrylist";

    private final BusinessRuleService businessRuleService;
    private final ValueSetService valueSetService;
    private final CountryListService countryListService;
    private final CertificateUtils certificateUtils;

    @Override
    @Scheduled(fixedDelayString = "${dgc.businessRulesDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadBusinessRules", lockAtLeastFor = "PT0S",
            lockAtMostFor = "${dgc.businessRulesDownload.lockLimit}")
    public void downloadBusinessRules() {
        try {
            initializeLogging();
            log.debug("Business rules download started.");

            //List<X509CertificateHolder> uploadCerts = fetchUploadCerts(httpClient);
            List<String> countryCodes = fetchCountryList();

            List<BusinessRuleItem> ruleItems = new ArrayList<>();
            try {
                ruleItems = businessRuleService.createBusinessRuleItemList(fetchValidationRulesAndVerify(countryCodes));
            } catch (NoSuchAlgorithmException e) {
                log.error("Could not create business rule item list: {}", e.getMessage(), e);
            }

            if (!ruleItems.isEmpty()) {
                businessRuleService.updateBusinessRules(ruleItems);
            } else {
                log.warn("The download of the business rules seems to fail, as the download connector "
                        + "returns an empty list. No data will be changed.");
            }

            log.info("Business rules download finished.");
        } finally {
            cleanLogging();
        }

    }

    @Override
    @Scheduled(fixedDelayString = "${dgc.valueSetsDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadValueSets", lockAtLeastFor = "PT0S",
            lockAtMostFor = "${dgc.valueSetsDownload.lockLimit}")
    public void downloadValueSets() {
        try {
            initializeLogging();
            log.debug("Value sets download started.");
            List<ValueSetItem> valueSetItems;
            List<String> valueSetIds = fetchValueSetIds();

            try {
                valueSetItems = valueSetService.createValueSetItemListFromMap(fetchValueSets(valueSetIds));
                log.debug("Downloaded {} value set items.", valueSetItems.size());
            } catch (NoSuchAlgorithmException e) {
                log.error("Failed to hash value set on download.",e);
                return;
            }

            if (!valueSetItems.isEmpty()) {
                valueSetService.updateValueSets(valueSetItems);
            } else {
                log.warn("The download of the value sets seems to fail, as the download connector "
                        + "returns an empty list. No data will be changed.");
            }

            log.debug("Value sets download finished.");
        } finally {
            cleanLogging();
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${dgc.countryListDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadCountryList", lockAtLeastFor = "PT0S",
            lockAtMostFor = "${dgc.countryListDownload.lockLimit}")
    public void downloadCountryList() {
        try {
            initializeLogging();
            log.debug("Country list download started.");

            List<String> countryList = fetchCountryList();
            log.debug("Downloaded {} country codes.", countryList.size());

            if (!countryList.isEmpty()) {
                countryListService.updateCountryList(gson().toJson(countryList));
            } else {
                log.warn("The download of the country list seems to fail as the gateway "
                        + "returns an empty list. No data will be changed.");
            }

            log.debug("Country list download finished.");
        } finally {
            cleanLogging();
        }
    }

    private List<X509CertificateHolder> fetchUploadCerts(HttpClient httpClient) {
        List<X509CertificateHolder> listOfUploadCerts = new ArrayList<>();

        try {
            HttpResponse response = httpClient.execute(RequestBuilder.get(DCCG_UPLOAD_CERTS_ENDPOINT).build());
            List<TrustListItemDto> trustListItems = gson().fromJson(toJsonString(response.getEntity()),
                    new TypeToken<List<TrustListItemDto>>() {}.getType());

            listOfUploadCerts = trustListItems.stream()
                    .filter(this::checkThumbprintIntegrity)
                    .filter(this::checkTrustAnchorSignature)
                    .map(this::getCertificateFromTrustListItem)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Fetching upload verts from gateway failed: {}", e.getMessage(), e);
        }

        return listOfUploadCerts;
    }

    private List<String> fetchCountryList() {
        HttpDestination httpDestination = DestinationAccessor.getDestination(DGCG_DESTINATION).asHttp();
        HttpClient httpClient = HttpClientAccessor.getHttpClient(httpDestination);
        List<String> countryList = new ArrayList<>();

        try {
            HttpResponse response = httpClient.execute(RequestBuilder.get(DCCG_COUNTRY_LIST_ENDPOINT).build());
            countryList = new ArrayList<>(gson().fromJson(toJsonString(response.getEntity()),
                    new TypeToken<List<String>>() {}.getType()));
        } catch (IOException e) {
            log.error("Could not fetch country list from gateway: {}", e.getMessage(), e);
        }

        return countryList;
    }

    private List<String> fetchValueSetIds() {
        HttpDestination httpDestination = DestinationAccessor.getDestination(DGCG_DESTINATION).asHttp();
        HttpClient httpClient = HttpClientAccessor.getHttpClient(httpDestination);
        List<String> valueSetIds = new ArrayList<>();

        try {
            HttpResponse response = httpClient.execute(RequestBuilder.get(DCCG_VALUE_SETS_ENDPOINT).build());
            valueSetIds = new ArrayList<>(gson().fromJson(toJsonString(response.getEntity()),
                    new TypeToken<List<String>>() {}.getType()));
        } catch (IOException e) {
            log.error("Could not fetch value set IDs from gateway: {}", e.getMessage());
        }

        return valueSetIds;
    }

    private Gson gson() {
        return new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                out.value(value.toString());
            }

            @Override
            public ZonedDateTime read(JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString());
            }
        })
                .enableComplexMapKeySerialization()
                .create();
    }

    private Gson gsonForValidationRule() {
        return new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                out.value(value.toString());
            }

            @Override
            public ZonedDateTime read(JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString());
            }
        })
                .registerTypeAdapter(JsonNode.class, new JsonNodeDeserializer())
                .setFieldNamingStrategy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .enableComplexMapKeySerialization()
                .create();
    }

    private String toJsonString(HttpEntity entity) throws IOException {
        return EntityUtils.toString(entity);
    }

    private boolean checkThumbprintIntegrity(TrustListItemDto trustListItem) {
        byte[] certificateRawData = Base64.getDecoder().decode(trustListItem.getRawData());

        try {
            if (trustListItem.getThumbprint()
                    .equals(this.getCertThumbprint(new X509CertificateHolder(certificateRawData)))) {
                return true;
            } else {
                log.debug("Thumbprint of trust list item '{}' did not match.", trustListItem.getKid());
                return false;
            }
        } catch (IOException e) {
            log.error("Could not parse certificate raw data: {}", e.getMessage());
            return false;
        }
    }

    private String getCertThumbprint(X509CertificateHolder x509CertificateHolder) {
        try {
            byte[] data = x509CertificateHolder.getEncoded();
            return certificateUtils.calculateHash(data);
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Could not calculate thumbprint of certificate '{}': {}.",
                    x509CertificateHolder.getSubject(), e.getMessage());
            return null;
        }
    }

    private X509CertificateHolder getCertificateFromTrustListItem(TrustListItemDto trustListItem) {
        byte[] decodedBytes = Base64.getDecoder().decode(trustListItem.getRawData());
        try {
            return new X509CertificateHolder(decodedBytes);
        } catch (IOException e) {
            log.error("Failed to parse Certificate Raw Data. KID: {}, Country: {}", trustListItem.getKid(),
                    trustListItem.getCountry());
            return null;
        }
    }

    private boolean checkTrustAnchorSignature(TrustListItemDto trustListItemDto) {
        // Implement me...
        return true;
    }

    private List<ValidationRule> fetchValidationRulesAndVerify(List<String> countryCodes) {
        HttpDestination httpDestination = DestinationAccessor.getDestination(DGCG_DESTINATION).asHttp();
        HttpClient httpClient = HttpClientAccessor.getHttpClient(httpDestination);
        List<ValidationRule> allRules = new ArrayList<>();

        for (String countryCode : countryCodes) {
            log.debug("Fetching rules for country '{}'...", countryCode);
            try {
                HttpResponse response = httpClient
                        .execute(RequestBuilder.get(DCCG_BUSINESS_RULES_ENDPOINT + "/" + countryCode).build());
                Map<String, ValidationRuleDto[]> fetchedForCountry = gson().fromJson(toJsonString(response.getEntity()),
                        new TypeToken<Map<String, ValidationRuleDto[]>>() {}.getType());

                log.debug("Fetched {} rule(s) for country '{}'. Parsing now...", fetchedForCountry.values().size(),
                        countryCode);
                allRules.addAll(fetchedForCountry.values().stream().flatMap(Arrays::stream).map(this::mapRule)
                        .filter(Objects::nonNull).collect(Collectors.toList()));
            } catch (IOException | JsonSyntaxException e) {
                log.warn("Could not fetch rules for country '{}': {}", countryCode, e.getMessage(), e);
            }
        }

        return allRules;
    }

    private ValidationRule mapRule(ValidationRuleDto dto) {
        try {
            SignedStringMessageParser parser = new SignedStringMessageParser(dto.getCms());
            ValidationRule validationRule = gsonForValidationRule().fromJson(parser.getPayload(), ValidationRule.class);
            validationRule.setRawJson(parser.getPayload());
            return validationRule;
        } catch (JsonSyntaxException e) {
            log.warn("Could not parse validation rule: {}", e.getMessage(), e);
        }
        return null;
    }

    private Map<String, String> fetchValueSets(List<String> valueSetIds) {
        HttpDestination httpDestination = DestinationAccessor.getDestination(DGCG_DESTINATION).asHttp();
        HttpClient httpClient = HttpClientAccessor.getHttpClient(httpDestination);
        Map<String, String> valueSets = new HashMap<>();

        for (String valueSetId : valueSetIds) {
            try {
                HttpResponse response = httpClient
                        .execute(RequestBuilder.get(DCCG_VALUE_SETS_ENDPOINT + "/" + valueSetId).build());
                valueSets.put(valueSetId, toJsonString(response.getEntity()));
            } catch (IOException e) {
                log.warn("Could not fetch value set with ID '{}': {}", valueSetId, e.getMessage(), e);
            }
        }

        return valueSets;
    }

    private static final String CORRELATION_ID_LOG_VAR_NAME = "correlation_id";

    private void initializeLogging() {
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, UUID.randomUUID().toString());
    }

    private void cleanLogging() {
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }

}
