package me.singun.restapiwithspring.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
@WebMvcTest
// 웹과 관련되 빈들이 모두 등록
// slicing test -> web 계층만 테스트
// 단위 테스트라고 보기에는 어려움
// 왜 ? 너무 많은게 관련
public class EventControllerTests {

	@Autowired
	MockMvc mockMvc;
	// mock으로 만들어져 있는 dispatcherServlet 를 사용할 수 있음
	// data mapper, convert 등 많은게 포함
	// 요청을 만들수 있고, 응답을 테스트 해볼 수 있음
	// 웹서버를 띄우지 않기 때문에 빠름

	@MockBean
	EventRepository eventRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	public void createEvent() throws Exception {
		Event event = Event.builder()
			.name("Spring")
			.description("REST API Development with Spring")
//			.beginEnrollmentDateTime(LocalDateTime.of(2019, 03, ))
			.basePrice(100)
			.maxPrice(200)
			.limitOfEnrollment(100)
			.location("D2 Startup Factory")
			.build();
		event.setId(10);
		Mockito.when(eventRepository.save(event)).thenReturn(event);

		mockMvc.perform(
			post("/api/events/")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaTypes.HAL_JSON)
				.content(objectMapper.writeValueAsString(event)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("id")
				.exists())
		.andExpect(header().exists(HttpHeaders.LOCATION))
		.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE));
	}
}
