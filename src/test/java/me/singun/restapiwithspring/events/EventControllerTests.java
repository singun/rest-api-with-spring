package me.singun.restapiwithspring.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.singun.restapiwithspring.common.RestDocsConfiguration;
import me.singun.restapiwithspring.common.TestDescription;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
//@WebMvcTest
// 웹과 관련되 빈들이 모두 등록
// slicing test -> web 계층만 테스트
// 단위 테스트라고 보기에는 어려움
// 왜 ? 너무 많은게 관련
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
public class EventControllerTests {

	@Autowired
	MockMvc mockMvc;
	// mock으로 만들어져 있는 dispatcherServlet 를 사용할 수 있음
	// data mapper, convert 등 많은게 포함
	// 요청을 만들수 있고, 응답을 테스트 해볼 수 있음
	// 웹서버를 띄우지 않기 때문에 빠름

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	EventRepository eventRepository;

	@Test
	@TestDescription("정상적으로 이벤트를 생성하는 테스트")
	public void createEvent() throws Exception {
		EventDto event = EventDto.builder()
			.name("Spring")
			.description("REST API Development with Spring")
			.beginEnrollmentDateTime(LocalDateTime.now())
			.closeEnrollmentDateTime(LocalDateTime.now())
			.beginEventDateTime(LocalDateTime.now())
			.endEventDateTime(LocalDateTime.now())
			.basePrice(100)
			.maxPrice(200)
			.limitOfEnrollment(100)
			.location("D2 Startup Factory")
			.build();

		mockMvc.perform(
			post("/api/events/")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaTypes.HAL_JSON)
				.content(objectMapper.writeValueAsString(event)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("id").exists())
			.andExpect(header().exists(HttpHeaders.LOCATION))
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
			.andExpect(jsonPath("free").value(false))
			.andExpect(jsonPath("offline").value(true))
			.andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
			.andDo(document("create-event",
				links(
					linkWithRel("self").description("link to self"),
					linkWithRel("query-events").description("link to query event"),
					linkWithRel("update-event").description("link to update an existing"),
					linkWithRel("profile").description("profile")
				),
				requestHeaders(
					headerWithName(HttpHeaders.ACCEPT).description("accept header"),
					headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
				),
				requestFields(
					fieldWithPath("name").description("name of new event"),
					fieldWithPath("description").description("description of new event"),
					fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
					fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
					fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
					fieldWithPath("endEventDateTime").description("date time of end of new event"),
					fieldWithPath("location").description("location of new event"),
					fieldWithPath("basePrice").description("base price of new event"),
					fieldWithPath("maxPrice").description("max price of new event"),
					fieldWithPath("limitOfEnrollment").description("limit of enrollment")
				),
				responseHeaders(
					headerWithName(HttpHeaders.LOCATION).description("location of response header"),
					headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
				),
				responseFields(
					fieldWithPath("id").description("id of new event"),
					fieldWithPath("name").description("name of new event"),
					fieldWithPath("description").description("description of new event"),
					fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
					fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
					fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
					fieldWithPath("endEventDateTime").description("date time of end of new event"),
					fieldWithPath("location").description("location of new event"),
					fieldWithPath("basePrice").description("base price of new event"),
					fieldWithPath("maxPrice").description("max price of new event"),
					fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
					fieldWithPath("free").description("it tells if this event is free or not"),
					fieldWithPath("offline").description("it tells if this event is free or not"),
					fieldWithPath("eventStatus").description("event status"),

					fieldWithPath("_links.self.href").description("hyper link for self link"),
					fieldWithPath("_links.query-events.href").description("hyper link for query events link"),
					fieldWithPath("_links.update-event.href").description("hyper link for update event link"),
					fieldWithPath("_links.profile.href").description("profile")
				)
			))
		;
	}

	@Test
	@TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
	public void createEvent_Bad_Request() throws Exception {
		Event event = Event.builder()
			.id(100)
			.name("Spring")
			.description("REST API Development with Spring")
			.basePrice(100)
			.maxPrice(200)
			.limitOfEnrollment(100)
			.location("D2 Startup Factory")
			.free(true)
			.offline(false)
			.eventStatus(EventStatus.PUBLISHED)
			.build();

		mockMvc.perform(post("/api/events/")
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.accept(MediaTypes.HAL_JSON)
			.content(objectMapper.writeValueAsString(event)))
			.andDo(print())
			.andExpect(status().isBadRequest())
		;
	}

	@Test
	@TestDescription("입력 값이 비어있는 경우에 에러가 발생하는 테스트")
	public void createEvent_Bad_Request_Empty_Input() throws Exception {
		EventDto eventDto = EventDto.builder().build();

		mockMvc.perform(post("/api/events")
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(objectMapper.writeValueAsString(eventDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@TestDescription("입력 값이 잘못 된 경우에 에러가 발생하는 테스트")
	public void createEvent_Bad_Request_Wrong_Input() throws Exception{
		EventDto eventDto = EventDto.builder()
			.name("Spring")
			.description("REST API Development with Spring")
			.beginEnrollmentDateTime(LocalDateTime.now())
			.closeEnrollmentDateTime(LocalDateTime.now())
			.beginEventDateTime(LocalDateTime.now())
			.endEventDateTime(LocalDateTime.now())
		 	.basePrice(200)
			.maxPrice(100)
			.limitOfEnrollment(100)
			.location("D2 Startup Factory")
			.build();

		this.mockMvc.perform(post("/api/events")
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(this.objectMapper.writeValueAsString(eventDto)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("content[0].objectName").exists())
			.andExpect(jsonPath("content[0].defaultMessage").exists())
			.andExpect(jsonPath("content[0].code").exists())
			.andExpect(jsonPath("_links.index").exists())
		;
	}

	@Test
	@TestDescription("30개의 이벤트를 10개씩 두번쨰 페이지 조회하기")
	public void queryEvents() throws Exception {
		// given
		IntStream.range(0, 30).forEach(this::generateEvent);

		// when
		mockMvc.perform(get("/api/events")
			.param("page", "1")
			.param("size", "10")
			.param("sort", "name,DESC"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("page").exists())
			.andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
			.andExpect(jsonPath("_links.self").exists())
			.andExpect(jsonPath("_links.profile").exists())
			.andDo(document("query-events"))
		;
	}

	private Event generateEvent(int i) {
		Event event = Event.builder()
			.name("event" + i)
			.description("test event")
			.build();

		return eventRepository.save(event);
	}

	@Test
	@TestDescription("기존의 이벤트를 하나 조회하기")
	public void getEvent() throws Exception {
		// given
		Event event = this.generateEvent(100);

		// when & then
		mockMvc.perform(get("/api/events/{id}", event.getId()))
			.andDo(print())
			.andExpect(jsonPath("name").exists())
			.andExpect(jsonPath("id").exists())
			.andExpect(jsonPath("_links.self").exists())
			.andExpect(jsonPath("_links.profile").exists())
			.andDo(document("get-an-event"))
			;
	}

	@Test
	@TestDescription("없는 이벤트는 조회했을 떄 404 응답받기")
	public void getEvent404() throws Exception {
		// when & then
		mockMvc.perform(get("/api/events/{id}", 1231231232))
			.andDo(print())
			.andExpect(status().isNotFound())
		;
	}


	//////////////////////////////////////////////////////////////////////////////
	@Test
	@TestDescription("수정하려는 이벤트가 없을 때, 404 응답받기")
	public void updateEvent404() throws Exception {
		// given
		EventDto event = EventDto.builder()
			.name("Spring")
			.description("REST API Development with Spring")
			.beginEnrollmentDateTime(LocalDateTime.now())
			.closeEnrollmentDateTime(LocalDateTime.now())
			.beginEventDateTime(LocalDateTime.now())
			.endEventDateTime(LocalDateTime.now())
			.basePrice(100)
			.maxPrice(200)
			.limitOfEnrollment(100)
			.location("D2 Startup Factory")
			.build();

		// when
		mockMvc.perform(put("/api/events/{id}", 123124124)
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.accept(MediaTypes.HAL_JSON)
			.content(objectMapper.writeValueAsString(event)))
			.andDo(print())
			.andExpect(status().isNotFound())
			;
	}

	@Test
	@TestDescription("입력 데이터가 이상한 경우에 400 응답받기")
	public void updateEvent400() throws Exception {
		EventDto eventDto = EventDto.builder().build();

		mockMvc.perform(put("/api/events/123124")
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(objectMapper.writeValueAsString(eventDto)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@TestDescription("도메인 로직으로 데이터 검증 실패하면 400 응답받기")
	public void updateEvent400_validation_fail() throws Exception {
		EventDto eventDto = EventDto.builder()
			.name("Spring")
			.description("REST API Development with Spring")
			.beginEnrollmentDateTime(LocalDateTime.now())
			.closeEnrollmentDateTime(LocalDateTime.now())
			.beginEventDateTime(LocalDateTime.now())
			.endEventDateTime(LocalDateTime.now())
			.basePrice(200)
			.maxPrice(100)
			.limitOfEnrollment(100)
			.location("D2 Startup Factory")
			.build();

		this.mockMvc.perform(put("/api/events/12312412")
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(this.objectMapper.writeValueAsString(eventDto)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("content[0].objectName").exists())
			.andExpect(jsonPath("content[0].defaultMessage").exists())
			.andExpect(jsonPath("content[0].code").exists())
			.andExpect(jsonPath("_links.index").exists())
		;
	}

	@Test
	@TestDescription("정상적으로 수정한 경우에 이벤트 리소스 응답")
	public void updateEvent() throws Exception {
		Event event1 = generateEvent(1);

		EventDto event = EventDto.builder()
			.name("Spring")
			.description("REST API Development with Spring")
			.beginEnrollmentDateTime(LocalDateTime.now())
			.closeEnrollmentDateTime(LocalDateTime.now())
			.beginEventDateTime(LocalDateTime.now())
			.endEventDateTime(LocalDateTime.now())
			.basePrice(100)
			.maxPrice(200)
			.limitOfEnrollment(100)
			.location("D2 Startup Factory")
			.build();

		mockMvc.perform(
			put("/api/events/{id}", event1.getId())
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaTypes.HAL_JSON)
				.content(objectMapper.writeValueAsString(event)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("id").exists())
			.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
			.andExpect(jsonPath("basePrice").value(100))
			.andExpect(jsonPath("free").value(false))
			.andExpect(jsonPath("offline").value(true))
			.andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
			.andDo(document("create-event",
				links(
					linkWithRel("self").description("link to self"),
					linkWithRel("query-events").description("link to query event"),
					linkWithRel("update-event").description("link to update an existing"),
					linkWithRel("profile").description("profile")
				),
				requestHeaders(
					headerWithName(HttpHeaders.ACCEPT).description("accept header"),
					headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
				),
				requestFields(
					fieldWithPath("name").description("name of new event"),
					fieldWithPath("description").description("description of new event"),
					fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
					fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
					fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
					fieldWithPath("endEventDateTime").description("date time of end of new event"),
					fieldWithPath("location").description("location of new event"),
					fieldWithPath("basePrice").description("base price of new event"),
					fieldWithPath("maxPrice").description("max price of new event"),
					fieldWithPath("limitOfEnrollment").description("limit of enrollment")
				),
				responseHeaders(
					headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
				),
				responseFields(
					fieldWithPath("id").description("id of new event"),
					fieldWithPath("name").description("name of new event"),
					fieldWithPath("description").description("description of new event"),
					fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
					fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
					fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
					fieldWithPath("endEventDateTime").description("date time of end of new event"),
					fieldWithPath("location").description("location of new event"),
					fieldWithPath("basePrice").description("base price of new event"),
					fieldWithPath("maxPrice").description("max price of new event"),
					fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
					fieldWithPath("free").description("it tells if this event is free or not"),
					fieldWithPath("offline").description("it tells if this event is free or not"),
					fieldWithPath("eventStatus").description("event status"),

					fieldWithPath("_links.self.href").description("hyper link for self link"),
					fieldWithPath("_links.query-events.href").description("hyper link for query events link"),
					fieldWithPath("_links.update-event.href").description("hyper link for update event link"),
					fieldWithPath("_links.profile.href").description("profile")
				)
			))
		;
	}
}
