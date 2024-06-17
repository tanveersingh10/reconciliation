package com.xion.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.xion.resultObjectModel.resultSummeries.bank.BankStatementLine;
import com.xion.resultObjectModel.resultSummeries.bank.BankStatementLineStatus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class BankStatementLineDeserializer extends StdDeserializer<BankStatementLineIntermediate> {

    public BankStatementLineDeserializer() {
        this(null);
    }

    public BankStatementLineDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BankStatementLineIntermediate deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        BankStatementLineIntermediate line = new BankStatementLineIntermediate();

        if (node.hasNonNull("id")) {
            line.setId(node.get("id").asLong());
        }

        if (node.hasNonNull("Date")) {
            try {
                line.setDate(new SimpleDateFormat("dd-MM-yyyy").parse(node.get("Date").asText()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (node.hasNonNull("MatchID")) {
            line.setMatchID(node.get("MatchID").asText());
        }

        if (node.hasNonNull("statementInternalID")) {
            line.setStatementInternalID(node.get("statementInternalID").asText());
        }

        if (node.hasNonNull("lineNumber")) {
            line.setLineNumber(node.get("lineNumber").asInt());
        }

        if (node.hasNonNull("Currency")) {
            line.setCurrency(node.get("Currency").asText());
        }

        if (node.hasNonNull("Debit")) {
            line.setDebit(node.get("Debit").asDouble());
        }

        if (node.hasNonNull("Credit")) {
            line.setCredit(node.get("Credit").asDouble());
        }

        if (node.hasNonNull("Balance")) {
            line.setBalance(node.get("Balance").asDouble());
        }

        if (node.hasNonNull("Txn Type")) {
            line.setTxnType(node.get("Txn Type").asText());
        }

        if (node.hasNonNull("Account")) {
            line.setAccount(node.get("Account").asText());
        }

        if (node.hasNonNull("Description")) {
            line.setDescription(node.get("Description").asText());
        }

        if (node.hasNonNull("Comments")) {
            line.setComments(node.get("Comments").asText());
        }

        if (node.hasNonNull("Customer Comment")) {
            line.setCustomerComment(node.get("Customer Comment").asText());
        }

        if (node.hasNonNull("xiboComments")) {
            line.setXiboComments(node.get("xiboComments").asBoolean());
        }

        if (node.hasNonNull("saved")) {
            line.setSaved(node.get("saved").asBoolean());
        }

        if (node.hasNonNull("status")) {
            try {
                line.setStatus(BankStatementLineStatus.valueOf(node.get("status").asText().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Handle invalid status value
                e.printStackTrace();
            }
        }

        if (node.hasNonNull("accountID")) {
            line.setAccountID(node.get("accountID").asText());
        }

        if (node.hasNonNull("parentId")) {
            line.setParentId(node.get("parentId").asLong());
        }

        if (node.hasNonNull("split")) {
            line.setSplit(node.get("split").asBoolean());
        }

        if (node.hasNonNull("documentReconcilationIds")) {
            JsonNode idsNode = node.get("documentReconcilationIds");
            if (idsNode.isArray()) {
                ArrayList<Long> documentReconcilationIds = new ArrayList<>();
                for (JsonNode idNode : idsNode) {
                    documentReconcilationIds.add(idNode.asLong());
                }
                line.setAccountID(documentReconcilationIds);
            }
        } else {
            ArrayList<Long> documentReconcilationIds = new ArrayList<>();
            line.setAccountID(documentReconcilationIds);
        }

        return line;
    }
}
