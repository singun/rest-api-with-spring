package me.singun.restapiwithspring.events;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class EventTest {

	@Test
	public void builder() {
		Event event = Event.builder().build();
		assertThat(event).isNotNull();
	}

	@Test
	public void javaBean() {
		// given
		String name = "Event";
		String description = "Spring";

		// when
		Event event = new Event();
		event.setName(name);
		event.setDescription(description);

		// then
		assertThat(event.getName()).isEqualTo(name);
		assertThat(event.getDescription()).isEqualTo(description);
	}

	private Object[] parametersForTestFree() {
		return new Object[] {
			new Object[] {0,0,true},
			new Object[] {100,0,false},
			new Object[] {0,100,false},
			new Object[] {200,100,false}
		};
	}

	@Test
	@Parameters
	public void testFree(int basePrice, int maxPrice, boolean isFree) {
		// given
		Event event = Event.builder()
			.basePrice(basePrice)
			.maxPrice(maxPrice)
			.build();

		// when
		event.update();

		// then
		assertThat(event.isFree()).isEqualTo(isFree);
	}

	private Object[] parametersForTestOffLine() {
		return new Object[] {
			new Object[] {"GangNam", true},
			new Object[] {null, false}
		};
	}

	@Test
	@Parameters
	public void testOffLine(String location, boolean isOffLine) {
		// given
		Event event = Event.builder()
			.location(location)
			.build();

		// when
		event.update();

		// then
		assertThat(event.isOffline()).isEqualTo(isOffLine);
	}
}