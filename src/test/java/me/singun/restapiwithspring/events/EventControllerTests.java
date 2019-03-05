package me.singun.restapiwithspring.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.singun.restapiwithspring.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class EventControllerTests {

	@Autowired
	MockMvc mockMvc;
	// mock으로 만들어져 있는 dispatcherServlet 를 사용할 수 있음
	// data mapper, convert 등 많은게 포함
	// 요청을 만들수 있고, 응답을 테스트 해볼 수 있음
	// 웹서버를 띄우지 않기 때문에 빠름

	@Autowired
	ObjectMapper objectMapper;

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
			.andExpect(jsonPath("id").value(Matchers.not(100)))
			.andExpect(jsonPath("free").value(Matchers.not(true)))
			.andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()));
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
			.andExpect(status().isBadRequest());
	}
}
