package nextstep.subway.acceptance;

import static nextstep.subway.test.RequestUtils.requestDeleteById;
import static nextstep.subway.test.RequestUtils.requestGetAll;
import static nextstep.subway.test.RequestUtils.requestGetById;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.PATH;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nextstep.subway.test.DatabaseClean;
import nextstep.subway.test.ExtractUtils;
import nextstep.subway.test.RequestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@DisplayName("구간 관련 기능 인수테스트")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SectionAcceptanceTest {

    private static final String path = "lines/{id}/sections";
    private static final Map<String, Object> 이호선 = new HashMap<>();
    private static final Map<String, Long> STATION_IDS = new HashMap<>();

    @Autowired
    private DatabaseClean databaseClean;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void init() {
        이호선.put("name", "2호선");
        이호선.put("color", "bg-green-200");
        이호선.put("distance", 10);
    }

    @BeforeEach
    private void setUp() {
        if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = this.port;
        }
        databaseClean.truncateAll();

        //given
        STATION_IDS.put("당산역", ExtractUtils.extractId(StationAcceptanceTest.createStation("당산역")));
        STATION_IDS.put("신도림역", ExtractUtils.extractId(StationAcceptanceTest.createStation("신도림역")));
        STATION_IDS.put("신대방역", ExtractUtils.extractId(StationAcceptanceTest.createStation("신대방역")));
        STATION_IDS.put("봉천역", ExtractUtils.extractId(StationAcceptanceTest.createStation("봉천역")));
        STATION_IDS.put("사당역", ExtractUtils.extractId(StationAcceptanceTest.createStation("사당역")));
        이호선.put("upStationId", STATION_IDS.get("신도림역"));
        이호선.put("downStationId", STATION_IDS.get("봉천역"));

        ExtractableResponse<Response> response = LineAcceptanceTest.creatLine(이호선);
        이호선.put("id", ExtractUtils.extractId(response));
    }

    /**
     * When 상행역과 하행역 사이에 새로운 구간을 등록하면(상행역 기준)
     * Then 새로운 구간이 생성 된다.
     * When 노선 목록 조회 시
     * Then 새로등록한 구간의 지하철역을 구간순서에 맞게 확인할 수 있다.
     */
    @DisplayName("구간 사이에 새로운 역을 등록한다.(상행기준)")
    @Test
    void addSection_between_up() {

        //when
        ExtractableResponse<Response> createResponse = 구간_생성_요청(이호선, "신도림역", "신대방역", 3);

        //then
        생성_되었는지_상태_검증(createResponse);

        //when
        ExtractableResponse<Response> response = requestGetAll(LineAcceptanceTest.LINE_PATH);

        //then
        추가된_구간에_대한_지하철역이_순서대로_조회_되는지_검증(response, "[신도림역, 신대방역, 봉천역]");

    }

    /**
     * When 상행역과 하행역 사이에 새로운 구간을 등록하면(하행역 기준)
     * Then 새로운 구간이 생성 된다.
     * When 노선 목록 조회 시
     * Then 새로등록한 구간의 지하철역을 구간순서에 맞게 확인할 수 있다.
     */
    @DisplayName("구간 사이에 새로운 역을 등록한다.(하행기준)")
    @Test
    void addSection_between_down() {

        //when
        ExtractableResponse<Response> createResponse = 구간_생성_요청(이호선, "신대방역", "봉천역", 3);

        //then
        생성_되었는지_상태_검증(createResponse);

        //when
        ExtractableResponse<Response> response = requestGetAll(LineAcceptanceTest.LINE_PATH);

        //then
        추가된_구간에_대한_지하철역이_순서대로_조회_되는지_검증(response, "[신도림역, 신대방역, 봉천역]");

    }

   /**
     * When 새로운 구간을 상행 종점으로 등록하면
     * Then 새로운 역이 상행 좀점으로 추가 생성 된다.
     * When 노선 목록 조회 시
     * Then 연결된 구간의 지하철역들을 구간순서에 맞게 확인할 수 있다.
     */
    @DisplayName("새로운 구간을 상행 종점으로 등록한다.")
    @Test
    void addSection_start_station() {

        //when
        ExtractableResponse<Response> createResponse = 구간_생성_요청(이호선, "당산역", "신도림역", 10);

        //then
        생성_되었는지_상태_검증(createResponse);

        //when
        ExtractableResponse<Response> response = requestGetAll(LineAcceptanceTest.LINE_PATH);

        //then
        추가된_구간에_대한_지하철역이_순서대로_조회_되는지_검증(response, "[당산역, 신도림역, 봉천역]");

    }

   /**
     * When 새로운 구간을 하행 종점으로 등록하면
     * Then 새로운 역이 하행 좀점으로 추가 생성 된다.
     * When 노선 목록 조회 시
     * Then 연결된 구간의 지하철역들을 구간순서에 맞게 확인할 수 있다.
     */
    @DisplayName("새로운 구간을 하행 종점으로 등록한다.")
    @Test
    void addSection_end_station() {

        //when
        ExtractableResponse<Response> createResponse = 구간_생성_요청(이호선, "봉천역", "사당역", 15);

        //then
        생성_되었는지_상태_검증(createResponse);

        //when
        ExtractableResponse<Response> response = requestGetAll(LineAcceptanceTest.LINE_PATH);

        //then
        추가된_구간에_대한_지하철역이_순서대로_조회_되는지_검증(response, "[신도림역, 봉천역, 사당역]");

    }

    /**
     * When 정상적인 여러개의 구간을 등록하고
     * When 노선 목록 조회 시
     * Then 연결된 구간의 지하철역들을 구간순서에 맞게 모두 확인할 수 있다.
     */
    @DisplayName("정상적인 여러개의 구간을 등록한다.")
    @Test
    void addSection_all() {

        //when
        구간_생성_요청(이호선, "봉천역", "사당역", 15);
        구간_생성_요청(이호선, "신도림역", "신대방역", 5);
        구간_생성_요청(이호선, "당산역", "신도림역", 15);

        //when
        ExtractableResponse<Response> response = requestGetAll(LineAcceptanceTest.LINE_PATH);

        //then
        추가된_구간에_대한_지하철역이_순서대로_조회_되는지_검증(response, "[당산역, 신도림역, 신대방역, 봉천역, 사당역]");

    }

    /**
     * When 등록 되어있지 않는 지하철역들로 구간을 요청하면
     * Then 찾을 수 없기 때문에 등록에 실패한다.
     */
    @DisplayName("상행&하행역이 둘 중 하나도 포함되어 있지 않으면 실패한다.")
    @Test
    void addSection_not_found() {

        //when
        ExtractableResponse<Response> createResponse = 구간_생성_요청(이호선, "신대방역", "사당역", 15);

        //then
        생성에_실패_했는지_상태_검증(createResponse);
    }

   /**
     * When 이미 등록된 구간으로 요청하면
     * Then 등록에 실패한다.
     */
    @DisplayName("상행역과 하행역이 이미 노선에 모두 등록되어 있다면 실패한다.")
    @Test
    void addSection_already_section() {

        //when
        ExtractableResponse<Response> createResponse = 구간_생성_요청(이호선, "신도림역", "봉천역", 10);

        //then
        생성에_실패_했는지_상태_검증(createResponse);
    }

    /**
     * When 기존 역 구간 길이보다 등록할 길이가 큰 경우
     * Then 등록에 실패한다.
     * When 기존 역 구간 길이와 등록할 길이가 같은 경우
     * Then 등록에 실패한다.
     */
    @DisplayName("기존 역 구간 길이보다 크거나 같으면 실패한다.")
    @Test
    void addSection_equal_or_greater_distence() {

        //when
        ExtractableResponse<Response> createResponse = 구간_생성_요청(이호선, "신도림역", "신대방역", 30);

        //then
        생성에_실패_했는지_상태_검증(createResponse);

        //when
        ExtractableResponse<Response> createResponse2 = 구간_생성_요청(이호선, "신대방역", "봉천역", 10);

        //then
        생성에_실패_했는지_상태_검증(createResponse2);
    }

    /**
    * Given 구간을 등록하고
    * When  해당 구간에 대한 지하철역 삭제 요청 하면
    * Then  해당 지하철역을 포함하는 구간이 삭제 된다.
    * When  노선에 대한 구간을 조회하면
    * Then  삭제된 구간에 대한 지하철역을 찾을 수 없다.
    */
    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void deleteSection() {
        //given
        구간_생성_요청(이호선, "신대방역", "봉천역", 3);

        //when
        ExtractableResponse<Response> deleteResponse = 구간_삭제_요청((Long) 이호선.get("id"), STATION_IDS.get("봉천역"));

        //then
        구간_삭제_되었는지_상태_검증(deleteResponse);

        //when
        ExtractableResponse<Response> response = requestGetById(LineAcceptanceTest.LINE_PATH, (Long) 이호선.get("id"));

        //then
        구간에서_지하철역이_삭제되었는지_검증(response, "봉천역");

    }

    private ExtractableResponse<Response> 구간_생성_요청(
            Map<String, Object> line,
            String upStationName,
            String downStationName,
            long distance) {

        Map<String, Object> sectionParams = new HashMap<>();
        sectionParams.put("upStationId", STATION_IDS.get(upStationName));
        sectionParams.put("downStationId", STATION_IDS.get(downStationName));
        sectionParams.put("distance", distance);
        return RequestUtils.requestCreate((Long) line.get("id"), sectionParams, path);
    }

    private ExtractableResponse<Response> 구간_삭제_요청(long lineId, long stationId) {
        Map<String, Object> queryParam = new HashMap<>();
        queryParam.put("stationId", stationId);
        return requestDeleteById(path, queryParam, lineId);
    }

    private void 생성_되었는지_상태_검증(ExtractableResponse<Response> createResponse) {
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void 추가된_구간에_대한_지하철역이_순서대로_조회_되는지_검증(ExtractableResponse<Response> response, String orderStations) {
        List<String> extract = ExtractUtils.extract("stations.name", response, String.class);
        assertThat(extract).containsExactly(orderStations);
    }

    private void 생성에_실패_했는지_상태_검증(ExtractableResponse<Response> createResponse) {
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 구간_삭제_되었는지_상태_검증(ExtractableResponse<Response> deleteResponse) {
        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 구간에서_지하철역이_삭제되었는지_검증(ExtractableResponse<Response> response, String stationName) {
        assertThat(ExtractUtils.extract("stations.name", response, String.class))
                .doesNotContain(stationName);
    }

}
