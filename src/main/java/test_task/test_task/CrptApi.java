package test_task.test_task;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final Bucket bucket;
    private final WebClient webClient;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.bucket = Bucket.builder()
                .addLimit(Bandwidth.classic(
                        requestLimit,
                        Refill.intervally(requestLimit, Duration.ofMillis(timeUnit.toMillis(1)))
                )) // 10 requests per 1 second/minute/other
                .build();
        this.webClient = WebClient.builder()
                .baseUrl("https://ismp.crpt.ru/api/v3")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @SneakyThrows // for interruptedException in consume()
    public void createDocument(CrptDocument document, String signature) {
        bucket.asBlocking().consume(1);
        webClient.post()
                .uri("/lk/documents/create")
                .header("X-Signature", signature)
                .body(BodyInserters.fromValue(document))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrptDocument {
        private Description description;
        @JsonProperty("doc_id")
        private String documentId;
        @JsonProperty("doc_status")
        private String documentStatus;
        @JsonProperty("doc_type")
        private String documentType;
        @JsonProperty("importRequest")
        private Boolean importRequest;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("participant_inn")
        private String participantInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private LocalDate productionDate;
        @JsonProperty("production_type")
        private String productionType;
        private List<Product> products;
        @JsonProperty("reg_date")
        private LocalDate registrationDate;
        @JsonProperty("reg_number")
        private String registrationNumber;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Description {
        @JsonProperty("participantInn")
        private String participantInn;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        @JsonProperty("certificate_document")
        private String certificateDocument;
        @JsonProperty("certificate_document_date")
        private LocalDate certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        private String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private LocalDate productionDate;
        @JsonProperty("tnved_code")
        private String tnvedCode;
        @JsonProperty("uit_code")
        private String uitCode;
        @JsonProperty("uitu_code")
        private String uitUCode;
    }
}
