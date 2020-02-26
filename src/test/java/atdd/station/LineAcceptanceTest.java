package atdd.station;

import atdd.station.model.CreateLineRequestView;
import atdd.station.model.dto.LineResponseDto;
import atdd.station.model.dto.StationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class LineAcceptanceTest {
    private StationTestUtils stationTestUtils;
    private LineTestUtils lineTestUtils;

    @BeforeEach
    void setUp() {
        this.stationTestUtils = new StationTestUtils(webTestClient);
        this.lineTestUtils = new LineTestUtils(webTestClient);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void createLine() {
        //when
        CreateLineRequestView createLineRequestView = CreateLineRequestView.builder()
                .name("2호선")
                .startTime(LocalTime.of(5, 0))
                .endTime(LocalTime.of(23, 50))
                .intervalTime(10)
                .build();

        LineResponseDto lineResponseDto = lineTestUtils.createLine(createLineRequestView);

        // then
        assertThat(lineResponseDto.getName()).isEqualTo("2호선");
    }

    @Test
    public void findAllLines() {
        // given
        LineResponseDto lineResponseDto = lineTestUtils.createLine("2호선",
                LocalTime.of(5, 0),
                LocalTime.of(5, 0),
                10);

        // when
        List<LineResponseDto> lineResponseDtos = lineTestUtils.findAll();

        //then
        assertThat(lineResponseDtos.size()).isEqualTo(1);
        assertThat(lineResponseDtos.get(0).getName()).isEqualTo("2호선");
    }

    @Test
    public void findLine() {
        // given
        long lineId = lineTestUtils.createLine("2호선",
                LocalTime.of(5, 0),
                LocalTime.of(5, 0),
                10).getId();

        // when
        LineResponseDto lineResponseDto = lineTestUtils.findById(lineId);

        // then
        assertThat(lineResponseDto.getName()).isEqualTo("2호선");
    }

    @Test
    public void deleteLine() {
        // given
        long lineId = lineTestUtils.createLine("2호선",
                LocalTime.of(5, 0),
                LocalTime.of(5, 0),
                10).getId();

        // when
        lineTestUtils.deleteLine(lineId);

        // then
        List<LineResponseDto> lineResponseDtos = lineTestUtils.findAll();

        assertThat(lineResponseDtos.size()).isEqualTo(0);
    }

    @Test
    public void addEdge() {
        // given
        StationDto station1 = stationTestUtils.createStation("강남역");
        StationDto station2 = stationTestUtils.createStation("역삼역");
        StationDto station3 = stationTestUtils.createStation("선릉역");

        LineResponseDto lineResponseDto = lineTestUtils.createLine("2호선",
                LocalTime.of(5, 0),
                LocalTime.of(5, 0),
                10);

        // when
        LineResponseDto resultLine = lineTestUtils.addEdge(lineResponseDto.getId(), station1.getId(), station2.getId());

        // then
        assertThat(resultLine.getStations().size()).isEqualTo(2);
        assertThat(resultLine.getStations().get(0).getName()).isEqualTo("강남역");
        assertThat(resultLine.getStations().get(1).getName()).isEqualTo("역삼역");
    }

    @Test
    public void deleteEdge() {
        // given
        StationDto station1 = stationTestUtils.createStation("강남역");
        StationDto station2 = stationTestUtils.createStation("역삼역");
        StationDto station3 = stationTestUtils.createStation("선릉역");

        LineResponseDto lineResponseDto = lineTestUtils.createLine("2호선",
                LocalTime.of(5, 0),
                LocalTime.of(5, 0),
                10);

        lineTestUtils.addEdge(lineResponseDto.getId(), station1.getId(), station2.getId());
        lineTestUtils.addEdge(lineResponseDto.getId(), station2.getId(), station3.getId());

        // when
        lineTestUtils.deleteEdge(lineResponseDto.getId(), station2.getId());

        // then
        LineResponseDto resultLine = lineTestUtils.findById(lineResponseDto.getId());

        assertThat(resultLine.getStations().size()).isEqualTo(2);
        assertThat(resultLine.getStations().get(0).getName()).isEqualTo("강남역");
        assertThat(resultLine.getStations().get(1).getName()).isEqualTo("선릉역");
    }
}
