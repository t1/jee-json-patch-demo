package test;

import com.github.t1.jeejsonpatchdemo.Gender;
import com.github.t1.jeejsonpatchdemo.RestApp;
import com.github.t1.jeejsonpatchdemo.Student;
import com.github.t1.jeejsonpatchdemo.Students;
import com.github.t1.testcontainers.jee.JeeContainer;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import static com.github.t1.jeejsonpatchdemo.Gender.female;
import static com.github.t1.jeejsonpatchdemo.Gender.male;
import static com.github.t1.testcontainers.tools.DeployableBuilder.war;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDSoftAssertions.thenSoftly;
import static org.slf4j.event.Level.DEBUG;

@Slf4j
@Testcontainers
class StudentsIT {
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Container static JeeContainer CONTAINER = JeeContainer.create()
            .withDeployment(war("ROOT").withClasses(RestApp.class, Students.class, Student.class, Gender.class))
            .withLogLevel(Students.class, DEBUG);

    @BeforeEach
    void setUp() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(CONTAINER.baseUri().resolve("/rest/students/reset"))
                .POST(noBody())
                .build();

        var response = HTTP.send(request, BodyHandlers.ofString());

        then(response.statusCode()).as(response::body).isEqualTo(204);
    }

    @Test void shouldGetStudent() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(CONTAINER.baseUri().resolve("/rest/students/1"))
                .GET()
                .build();

        var response = HTTP.send(request, BodyHandlers.ofString());

        then(response.statusCode()).as(response::body).isEqualTo(200);
        then(response.headers().firstValue("Content-Type")).contains("application/json");

        var student = JSONB.fromJson(response.body(), Student.class);
        thenSoftly(softly -> {
            softly.then(student.getId()).isEqualTo(1);
            softly.then(student.getFirstName()).isEqualTo("Alice");
            softly.then(student.getLastName()).isEqualTo("Doe");
            softly.then(student.getGender()).isEqualTo(female);
            softly.then(student.getSchool()).isEqualTo("MiddleWood School");
        });
    }

    @Test void shouldJsonPatchStudent() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(CONTAINER.baseUri().resolve("/rest/students/1"))
                .header("Content-Type", "application/json-patch+json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                        [
                            {"op":"copy","from":"/firstName","path":"/lastName"},
                            {"op":"replace","path":"/firstName","value":"John"},
                            {"op":"remove","path":"/school"},
                            {"op":"add","path":"/gender","value":"male"}
                        ]
                        """))
                .build();

        var response = HTTP.send(request, BodyHandlers.ofString());

        then(response.statusCode()).as(response::body).isEqualTo(200);
        then(response.headers().firstValue("Content-Type")).contains("application/json");

        var student = JSONB.fromJson(response.body(), Student.class);
        thenSoftly(softly -> {
            softly.then(student.getId()).isEqualTo(1);
            softly.then(student.getFirstName()).isEqualTo("John");
            softly.then(student.getLastName()).isEqualTo("Alice");
            softly.then(student.getGender()).isEqualTo(male);
            softly.then(student.getSchool()).isNull();
        });
    }

    @Test void shouldJsonMergePatchStudent() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(CONTAINER.baseUri().resolve("/rest/students/1"))
                .header("Content-Type", "application/merge-patch+json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString("""
                        {
                            "firstName":"Green",
                            "school":null
                        }
                        """))
                .build();

        var response = HTTP.send(request, BodyHandlers.ofString());

        then(response.statusCode()).as(response::body).isEqualTo(200);
        then(response.headers().firstValue("Content-Type")).contains("application/json");

        var student = JSONB.fromJson(response.body(), Student.class);
        thenSoftly(softly -> {
            softly.then(student.getId()).isEqualTo(1);
            softly.then(student.getFirstName()).isEqualTo("Green");
            softly.then(student.getLastName()).isEqualTo("Doe");
            softly.then(student.getGender()).isEqualTo(female);
            softly.then(student.getSchool()).isNull();
        });
    }
}
