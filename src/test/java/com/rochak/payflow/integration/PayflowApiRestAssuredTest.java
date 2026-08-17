package com.rochak.payflow.integration;

import com.rochak.payflow.entity.Role;
import com.rochak.payflow.entity.User;
import com.rochak.payflow.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PayflowApiRestAssuredTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void protectedEndpoint_withoutToken_shouldReturn401() {
        given()
                .when()
                .get("/api/wallets/me")
                .then()
                .statusCode(401);
    }

    @Test
    void userRegistrationAndLogin_shouldWorkEndToEnd() {
        String suffix = UUID.randomUUID().toString();
        String email = "api-" + suffix + "@test.com";
        String name = "api-" + suffix;
        String password = "password123";

        String userId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "%s",
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(name, email, password))
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .body("email", equalTo(email))
                .body("name", equalTo(name))
                .extract()
                .path("id")
                .toString();

        assertThat(userId).isNotEmpty();

        String token = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(email, password))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", not(emptyString()))
                .body("refreshToken", not(emptyString()))
                .extract()
                .path("accessToken");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/wallets/me")
                .then()
                .statusCode(200)
                .body("balance", equalTo(0.0f));
    }

    @Test
    void regularUser_shouldNotAccessAdminEndpoint() {
        String suffix = UUID.randomUUID().toString();
        User user = userRepository.save(
                new User(
                        null,
                        "api-user-" + suffix + "@test.com",
                        "api-user-" + suffix,
                        passwordEncoder.encode("password123"),
                        Role.USER
                )
        );

        String token = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "password123"
                        }
                        """.formatted(user.getEmail()))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/admin/users")
                .then()
                .statusCode(403);
    }

    @Test
    void refreshAndLogout_shouldWorkThroughApi() {
        String suffix = UUID.randomUUID().toString();
        String email = "refresh-" + suffix + "@test.com";

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "refresh-%s",
                          "email": "%s",
                          "password": "password123"
                        }
                        """.formatted(suffix, email))
                .when()
                .post("/api/users")
                .then()
                .statusCode(201);

        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "email": "%s",
                          "password": "password123"
                        }
                        """.formatted(email))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("refreshToken");

        String newAccessToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", not(emptyString()))
                .body("refreshToken", not(emptyString()))
                .extract()
                .path("accessToken");

        given()
                .header("Authorization", "Bearer " + newAccessToken)
                .when()
                .get("/api/wallets/me")
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200)
                .body(equalTo("Logged out successfully"));
    }
}
