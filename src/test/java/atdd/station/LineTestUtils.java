package atdd.station;

import atdd.station.model.CreateEdgeRequestView;
import atdd.station.model.CreateLineRequestView;
import atdd.station.model.entity.Line;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class LineTestUtils {
    private final String LINES_PATH = "/lines";
    private final String EDGES_PATH = "/edge";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    private static final ObjectMapper mapper = new ObjectMapper();

    public WebTestClient webTestClient;

    public LineTestUtils(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    public Line createLine(final String name,
                           final LocalTime startTime,
                           final LocalTime endTime,
                           final int intervalTime) {
        CreateLineRequestView createLineRequestView = CreateLineRequestView.builder()
                .name(name)
                .startTime(startTime)
                .endTime(endTime)
                .intervalTime(intervalTime)
                .build();

        return createLine(createLineRequestView);
    }

    public Line createLine(CreateLineRequestView createLineRequestView) {

        String inputJson = createLineRequestViewToJson(createLineRequestView);

        EntityExchangeResult result = webTestClient.post().uri("/lines")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(inputJson), String.class)
                .exchange()
                .expectBody(Line.class).returnResult();

        Line line = (Line) result.getResponseBody();

        return line;
    }

    public List<Line> findAll() {

        EntityExchangeResult result = webTestClient.get().uri(LINES_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(List.class).returnResult();

        return (List<Line>) result.getResponseBody();
    }

    public Optional<Line> findById(final long id) {
        EntityExchangeResult result = webTestClient.get().uri(LINES_PATH + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Line.class).returnResult();


        return Optional.ofNullable((Line) result.getResponseBody());
    }

    public void deleteLine(final long id) {
        EntityExchangeResult result = webTestClient.delete().uri(LINES_PATH + "/" + id)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().returnResult();
    }

    public Line addEdge(final long lineId, final long sourceStationId, final long targetStationId) {
        CreateEdgeRequestView createEdgeRequestView = new CreateEdgeRequestView();
        createEdgeRequestView.setSourceStationId(sourceStationId);
        createEdgeRequestView.setTargetStationId(targetStationId);

        EntityExchangeResult result = webTestClient.post().uri("/lines/" + lineId + "/edge")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(writeValueAsString(createEdgeRequestView)), String.class)
                .exchange()
                .expectBody(Line.class).returnResult();

        return (Line) result.getResponseBody();
    }

    public void deleteEdge(final long id, final long stationId) {
        webTestClient.delete().uri(LINES_PATH + "/" + id + EDGES_PATH + "?stationId=" + stationId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().returnResult();
    }


    public String writeLineListAsString(final List<Line> lines) {
        final StringBuilder stringBuilder = new StringBuilder();
        final String lineValue = "{\"id\":%d" +
                ",\"name\":\"%s" +
                "\",\"startTime\":\"%s" +
                "\",\"endTime\":\"%s" +
                "\",\"intervalTime\":%d" +
                ",\"stations\":%s}";

        for (Line line : lines) {
            if (stringBuilder.length() > 0)
                stringBuilder.append(",");

            stringBuilder.append(String.format(lineValue,
                    line.getId(),
                    line.getName(),
                    line.getStartTime().format(formatter),
                    line.getEndTime().format(formatter),
                    line.getIntervalTime(),
                    writeValueAsString(line.getStationDtos())));
        }

        return "[" + stringBuilder.toString() + "]";
    }

    public String writeValueAsString(Object object) {
        String result = null;
        try {
            result = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String createLineRequestViewToJson(CreateLineRequestView createLineRequestView) {
        return "{\"name\":\"" + createLineRequestView.getName() +
                "\",\"startTime\":\"" + createLineRequestView.getStartTime().format(formatter) +
                "\",\"endTime\":\"" + createLineRequestView.getEndTime().format(formatter) +
                "\",\"intervalTime\":" + createLineRequestView.getIntervalTime() + "}";
    }
}
