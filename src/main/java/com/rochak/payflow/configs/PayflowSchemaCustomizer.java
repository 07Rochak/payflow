package com.rochak.payflow.configs;

import com.rochak.payflow.dto.SessionAuditReport;
import com.rochak.payflow.dto.SessionSecurityReport;
import com.rochak.payflow.dto.razorpay.RazorpayOrderRequest;
import com.rochak.payflow.dto.razorpay.RazorpayOrderResponse;
import com.rochak.payflow.entity.Payment;
import com.rochak.payflow.entity.PaymentFailureReason;
import com.rochak.payflow.entity.PaymentStatus;
import com.rochak.payflow.entity.RefreshToken;
import com.rochak.payflow.entity.Role;
import com.rochak.payflow.entity.Transaction;
import com.rochak.payflow.entity.TransactionCategory;
import com.rochak.payflow.entity.TransactionStatus;
import com.rochak.payflow.entity.TransactionType;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.entity.Wallet;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PayflowSchemaCustomizer implements OpenApiCustomizer {

    private final ModelConverters modelConverters =
            ModelConverters.getInstance();

    @Override
    public void customise(OpenAPI openAPI) {

        if (openAPI.getComponents() == null) {
            return;
        }

        if (openAPI.getComponents().getSchemas() == null) {
            openAPI.getComponents().setSchemas(new LinkedHashMap<>());
        }

        /*
         * ============================================================
         * PROVIDER MODELS
         * ============================================================
         */

        registerClassSchema(
                openAPI,
                RazorpayOrderRequest.class
        );

        registerClassSchema(
                openAPI,
                RazorpayOrderResponse.class
        );

        /*
         * ============================================================
         * PERSISTENCE ENTITIES
         * ============================================================
         */

        registerClassSchema(
                openAPI,
                User.class
        );

        registerClassSchema(
                openAPI,
                Wallet.class
        );

        registerClassSchema(
                openAPI,
                Payment.class
        );

        registerClassSchema(
                openAPI,
                Transaction.class
        );

        registerClassSchema(
                openAPI,
                RefreshToken.class
        );

        /*
         * ============================================================
         * INTERNAL REPORTS
         * ============================================================
         */

        registerClassSchema(
                openAPI,
                SessionAuditReport.class
        );

        registerClassSchema(
                openAPI,
                SessionSecurityReport.class
        );

        /*
         * ============================================================
         * ENUMS
         * ============================================================
         */

        registerEnumSchema(
                openAPI,
                Role.class
        );

        registerEnumSchema(
                openAPI,
                PaymentStatus.class
        );

        registerEnumSchema(
                openAPI,
                PaymentFailureReason.class
        );

        registerEnumSchema(
                openAPI,
                TransactionStatus.class
        );

        registerEnumSchema(
                openAPI,
                TransactionType.class
        );

        registerEnumSchema(
                openAPI,
                TransactionCategory.class
        );

        /*
         * ============================================================
         * CLEANUP
         * ============================================================
         *
         * The existing application configuration is responsible for
         * public API DTOs. This customizer is responsible only for
         * developer-reference models.
         */
    }

    private void registerClassSchema(
            OpenAPI openAPI,
            Class<?> modelClass
    ) {

        Map<String, Schema> resolvedSchemas =
                modelConverters.readAll(
                        new AnnotatedType(modelClass)
                                .resolveAsRef(false)
                );

        if (resolvedSchemas == null || resolvedSchemas.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Schema> entry :
                resolvedSchemas.entrySet()) {

            String schemaName = entry.getKey();

            Schema schema = entry.getValue();

            /*
             * Do not overwrite schemas already generated by
             * Springdoc. This prevents the customizer from changing
             * existing API DTO documentation.
             */
            if (!openAPI.getComponents()
                    .getSchemas()
                    .containsKey(schemaName)) {

                sanitizeSchema(schema);

                openAPI.getComponents()
                        .addSchemas(schemaName, schema);
            }
        }
    }

    private void registerEnumSchema(
            OpenAPI openAPI,
            Class<? extends Enum<?>> enumClass
    ) {

        String schemaName = enumClass.getSimpleName();

        /*
         * Do not overwrite anything already generated by Springdoc.
         */
        if (openAPI.getComponents()
                .getSchemas()
                .containsKey(schemaName)) {

            return;
        }

        List<String> values =
                Arrays.stream(enumClass.getEnumConstants())
                        .map(Enum::name)
                        .toList();

        StringSchema schema = new StringSchema();

        schema.setDescription(
                "Enumeration values used by the PayFlow domain model."
        );

        schema.setEnum(values);

        openAPI.getComponents()
                .addSchemas(schemaName, schema);
    }

    private void sanitizeSchema(Schema<?> schema) {

        if (schema == null) {
            return;
        }

        /*
         * Prevent Java time objects from being placed directly into
         * OpenAPI examples. Springdoc serializes the final OpenAPI
         * document separately from model resolution, and Java time
         * objects can otherwise cause serialization problems.
         */
        if (schema.getExample() != null
                && !(schema.getExample() instanceof String)
                && !(schema.getExample() instanceof Number)
                && !(schema.getExample() instanceof Boolean)) {

            schema.setExample(
                    schema.getExample().toString()
            );
        }

        /*
         * Sanitize nested properties as well.
         */
        if (schema.getProperties() != null) {

            for (Object property :
                    schema.getProperties().values()) {

                if (property instanceof Schema<?> childSchema) {

                    sanitizeSchema(childSchema);
                }
            }
        }

        /*
         * Sanitize array item schemas.
         */
        if (schema.getItems() instanceof Schema<?> itemSchema) {

            sanitizeSchema(itemSchema);
        }

        /*
         * Sanitize additionalProperties when represented as
         * another schema.
         */
        Object additionalProperties =
                schema.getAdditionalProperties();

        if (additionalProperties instanceof Schema<?> additionalSchema) {

            sanitizeSchema(additionalSchema);
        }
    }
}