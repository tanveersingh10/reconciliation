package com.xion.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FetchDataService {
    public List<BankStatementLineIntermediate> fetchBankData(String companyId, String bankName, LocalDate startDate, LocalDate endDate) {

        String baseUrl = "http://ops-bg-flask.xion.ai/get_bank_data";
        RestTemplate restTemplate = new RestTemplate();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("company_id", companyId)
                .queryParam("bank_name", bankName)
                .queryParam("start_date", startDateStr)
                .queryParam("end_date", endDateStr)
                .build()
                .toString();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            SimpleModule module = new SimpleModule();
            module.addDeserializer(BankStatementLineIntermediate.class, new BankStatementLineDeserializer());
            objectMapper.registerModule(module);
            String responseBody = response.getBody();
            try {
                JsonNode rootNode = objectMapper.readTree(responseBody);
                // Extract the array node for "DBS SGD"
                JsonNode bankNode = rootNode.path(bankName);
                return objectMapper.readValue(bankNode.toString(), new TypeReference<List<BankStatementLineIntermediate>>() {});
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Error parsing JSON response.");
            }
        } else {
            System.out.println("Error: " + response.getStatusCode());
        }
        return null;
    }
    public String getToken(String scope) {
        RestTemplate restTemplate = new RestTemplate();

        String clientId = "0oa119mjqmhhpR1dv357";
        String clientSecret = "XzRM4hQc2CR8gCgKOPX2E1mf2hAi9Xdh4NCpHNXi";
        String tokenUrl = "https://dev-754943.okta.com/oauth2/default/v1/token";

        String auth = clientId + ":" + clientSecret;
        byte[] encodedAuth = Base64.encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authHeader);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, entity, Map.class);


        Map<String, Object> responseBody = response.getBody();
        assert responseBody != null;
        return (String) responseBody.get("access_token");
    }

    public ArrayList<InvoiceResultSummeryIntermediate> fetchInvoiceData(String pipelineState, String companyId, LocalDate startDate, LocalDate endDate) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String token = getToken("vault_store");
        String baseUrl = "http://store-service.xion.ai";
        String GENERAL_PULL_DQM = baseUrl + "/dqm/generalPull";

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        if (pipelineState.isEmpty()) pipelineState = "ACCOUNTANT_REVIEW";

        Map<String, Object> jsonRequest = new HashMap<>();
        jsonRequest.put("pipelineState", pipelineState);
        jsonRequest.put("companyId", companyId);
        jsonRequest.put("startDate", startDateStr);
        jsonRequest.put("endDate", endDateStr);
        jsonRequest.put("mergedReview", true);
        jsonRequest.put("docDate", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "bearer " + token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jsonRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(GENERAL_PULL_DQM, HttpMethod.POST, entity, String.class);
        String jsonString = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode rootNode = objectMapper.readTree(jsonString);
        JsonNode dataNode = rootNode.path("payload").path("data");
        ArrayList<InvoiceResultSummeryIntermediate> invoices = new ArrayList<>();

        if (dataNode.isArray()) {
            for (JsonNode node : dataNode) {

                JsonNode resultSummeryNode = node.path("resultSummery");
                InvoiceResultSummeryIntermediate dto = objectMapper.treeToValue(resultSummeryNode, InvoiceResultSummeryIntermediate.class);
                ArrayList<Long> reconciliationIds = new ArrayList<>();
                dto.setBankReconciliationIds(reconciliationIds);

//                invoice.setType(dto.getType());
//                invoice.setName(dto.getName());
//                invoice.setUniversalDocumentId(dto.getUniversalDocumentId());
//                invoice.setCompanyId(dto.getCompanyId());
//                invoice.setId(dto.getId());
//                invoice.setLabels(dto.getLabels());
//                invoice.setStatus(dto.getStatus());
//                invoice.setNotes(dto.getNotes());
//                invoice.setDate(dto.getDate());
//                invoice.setCreationDate(dto.getCreationDate());
//                invoice.setFlag(dto.getFlag());
//                invoice.setReviewed(dto.getReviewed());
//                invoice.setGl(dto.getGl());
//                invoice.setMatchId(dto.getMatchId().toString());
//                invoice.setDocumentClassification(dto.getDocumentClassification());
//
//                invoice.setPreTaxAmount(dto.getPreTaxAmount());
//                invoice.setTaxAmount(dto.getTaxAmount());
//                invoice.setSupplierName(dto.getSupplierName());
//                invoice.setCustomerName(dto.getCustomerName());
//                invoice.setSupplierAddress(dto.getSupplierAddress());
//                invoice.setCustomerAddress(dto.getCustomerAddress());
//                invoice.setInvoiceNumber(dto.getInvoiceNumber());
//                invoice.setDueOn(dto.getDueOn());
//                invoice.setPoNumber(dto.getPoNumber());
//                invoice.setGstNumber(dto.getGstNumber());
//                invoice.setTotalAmount(dto.getTotalAmount());
//                invoice.setCurrency(dto.getCurrency());

                invoices.add(dto);
            }


        }

        return invoices;
    }

    public static CompletableFuture<Void> getAISuggestion(String companyId, String bankName,
                                                          DatePicker startDatePickerBank, DatePicker endDatePickerBank,
                                                          DatePicker startDatePickerInvoice, DatePicker endDatePickerInvoice,
                                                          String hint) {

        if (startDatePickerBank.getValue() == null || endDatePickerBank.getValue() == null
                || startDatePickerInvoice.getValue() == null || endDatePickerInvoice.getValue() == null) {
            Notification.show("Please select date range for invoices and bank statements")
                    .setPosition(Notification.Position.MIDDLE);
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String baseUrl = "http://127.0.0.1:5000/run-session";

        Map<String, Object> jsonRequest = new HashMap<>();
        jsonRequest.put("companyId", companyId);
        jsonRequest.put("bankName", bankName);
        jsonRequest.put("bankStartDate", startDatePickerBank.getValue().format(formatter).toString());
        jsonRequest.put("bankEndDate", endDatePickerBank.getValue().format(formatter).toString());
        jsonRequest.put("invoiceStartDate", startDatePickerInvoice.getValue().format(formatter).toString());
        jsonRequest.put("invoiceEndDate", endDatePickerInvoice.getValue().format(formatter).toString());
        jsonRequest.put("hint", hint);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(jsonRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
        System.out.println(response.getBody());
        return null;
    }


//    public static void getExchangeRateSGD() {
//        String url = "https://open.er-api.com/v6/latest/SGD";
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//        System.out.println(response.getBody());
//    }

    public static String getCurrentHints() {
        return "Hints: \n" +
                "- All payments to Alex happened via bank transfer in bulk.\n" +
                "- All payments to Daniel go out from Wise.";
    }


}
