# PayFlow Swagger / OpenAPI Documentation

## Purpose
Swagger is the formal API reference. It is intentionally documentation-only: request execution is disabled in Swagger UI. Use the Postman collection for API testing.

## Access
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- OpenAPI YAML: `/v3/api-docs.yaml`

These documentation resources are public and do not require a JWT. Business APIs retain their existing authentication and RBAC rules.

## Authentication
Protected APIs use the `bearerAuth` JWT scheme. Obtain an access token from `POST /api/auth/login` and send it to protected APIs as `Authorization: Bearer <access-token>`. Swagger does not execute the requests.

## Response documentation rule
Success responses contain the actual response schema. Authentication/authorization and error responses contain descriptions only unless a distinct, stable response model exists. This avoids repeating the same business response schema under 401/403/400 responses and avoids misleading generated `additionalProp1` fields.

## Model coverage
The OpenAPI Models section includes public API DTOs plus explicitly labelled internal developer-reference models: Razorpay order request/response models, session audit/security reports, JPA entities and relevant enums. Mappers are implementation components rather than HTTP data contracts, so their responsibilities are described in the API overview rather than represented as fake schemas.

## Browser happy flow
The bundled `index.html` implements the demonstration flow: PayFlow login -> PayFlow/Razorpay order creation -> Razorpay Checkout -> test card/OTP -> Razorpay result -> PayFlow signature verification -> wallet credit.

## Runtime timezone
Run the JVM with `-Duser.timezone=Asia/Kolkata`.
