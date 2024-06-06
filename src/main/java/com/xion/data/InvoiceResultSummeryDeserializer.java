//package com.xion.data;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
//import com.xion.resultObjectModel.resultSummeries.InvoiceResultSummery;
//
//import java.io.IOException;
//
//public class InvoiceResultSummeryDeserializer extends StdDeserializer<InvoiceResultSummery> {
//
//    public InvoiceResultSummeryDeserializer() {
//        this(null);
//    }
//
//    public InvoiceResultSummeryDeserializer(Class<?> vc) {
//        super(vc);
//    }
//
//    @Override
//    public InvoiceResultSummery deserialize(JsonParser jp, DeserializationContext ctxt)
//            throws IOException, JsonProcessingException {
//        JsonNode node = jp.getCodec().readTree(jp);
//        JsonNode resultSummeryNode = node.path("payload").path("data").get(0).path("resultSummery");
//
//        InvoiceResultSummeryIntermediate dto = jp.getCodec().treeToValue(resultSummeryNode, InvoiceResultSummeryIntermediate.class);
//
//        InvoiceResultSummery invoice = new InvoiceResultSummery();
//        // Map ResultSummery fields
//        invoice.setType(dto.getType());
//        invoice.setName(dto.getName());
//        invoice.setUniversalDocumentId(dto.getUniversalDocumentId());
//        invoice.setCompanyId(dto.getCompanyId());
//        invoice.setId(dto.getId());
//        invoice.setLabels(dto.getLabels());
//        invoice.setStatus(dto.getStatus());
//        invoice.setNotes(dto.getNotes());
//        invoice.setDate(dto.getDate());
//        invoice.setCreationDate(dto.getCreationDate());
//        invoice.setFlag(dto.getFlag());
//        invoice.setReviewed(dto.getReviewed());
//        invoice.setGl(dto.getGl());
//        invoice.setMatchId(dto.getMatchId().toString());
//        invoice.setDocumentClassification(dto.getDocumentClassification());
//
//        // Map InvoiceResultSummery fields
//        invoice.setPreTaxAmount(dto.getPreTaxAmount());
//        invoice.setTaxAmount(dto.getTaxAmount());
//        invoice.setSupplierName(dto.getSupplierName());
//        invoice.setCustomerName(dto.getCustomerName());
//        invoice.setSupplierAddress(dto.getSupplierAddress());
//        invoice.setCustomerAddress(dto.getCustomerAddress());
//        invoice.setInvoiceNumber(dto.getInvoiceNumber());
//        invoice.setDueOn(dto.getDueOn());
//        invoice.setPoNumber(dto.getPoNumber());
//        invoice.setGstNumber(dto.getGstNumber());
//        invoice.setTotalAmount(dto.getTotalAmount());
//        invoice.setCurrency(dto.getCurrency());
//
//        return invoice;
//    }
//}
