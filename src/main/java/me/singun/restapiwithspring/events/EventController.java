package me.singun.restapiwithspring.events;

import me.singun.restapiwithspring.common.ErrorResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

	private final EventRepository eventRepository;

	private final ModelMapper modelMapper;

	private final EventValidator eventValidator;

	public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
		this.eventRepository = eventRepository;
		this.modelMapper = modelMapper;
		this.eventValidator = eventValidator;
	}

	@PostMapping
	public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
		if (errors.hasErrors()) {
			return badRequest(errors);
		}

		eventValidator.validate(eventDto, errors);
		if (errors.hasErrors()) {
			return badRequest(errors);
		}

		Event event = modelMapper.map(eventDto, Event.class);
		event.update();
		Event newEvent = this.eventRepository.save(event);
		ControllerLinkBuilder selfLinkBuild = linkTo(EventController.class).slash(newEvent.getId());
		URI createdUri = selfLinkBuild.toUri();
		EventResource eventResource = new EventResource(newEvent);
		eventResource.add(linkTo(EventController.class).withRel("query-events"));
		// eventResource 내부로 이동
//		eventResource.add(selfLinkBuild.withSelfRel());
		eventResource.add(selfLinkBuild.withRel("update-event"));
		eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));
		return ResponseEntity.created(createdUri).body(eventResource);
	}

	private ResponseEntity<ErrorResource> badRequest(Errors errors) {
		return ResponseEntity.badRequest().body(new ErrorResource(errors));
	}

	@GetMapping
	public ResponseEntity queryEvent(Pageable pageable, PagedResourcesAssembler assembler) {
		Page<Event> page = eventRepository.findAll(pageable);
		PagedResources pagedResources = assembler.toResource(page, e -> new EventResource((Event) e));
		pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
		return ResponseEntity.ok(pagedResources);
	}

	@GetMapping("/{id}")
	public ResponseEntity getEvent(@PathVariable Integer id) {
		Optional<Event> optionalEvent = eventRepository.findById(id);
		if (!optionalEvent.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		Event event = optionalEvent.get();
		EventResource eventResource = new EventResource(event);
		eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
		return ResponseEntity.ok(eventResource);
	}
}
