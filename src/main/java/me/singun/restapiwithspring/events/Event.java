package me.singun.restapiwithspring.events;

import lombok.*;
import me.singun.restapiwithspring.accounts.Account;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
public class Event {
	@Id @GeneratedValue
	private Integer id;
	private String name;
	private String description;
	private LocalDateTime beginEnrollmentDateTime;
	private LocalDateTime closeEnrollmentDateTime;
	private LocalDateTime beginEventDateTime;
	private LocalDateTime endEventDateTime;
	private String location; // (optional) 이게 없으면 온라인 모임
	private int basePrice; // (optional)
	private int maxPrice; // (optional)
	private int limitOfEnrollment;
	private boolean offline;
	private boolean free;
	@Enumerated(EnumType.STRING) // ordinal 은 enum 의 순서대로 숫자가 생성되는데, enum 의 순서가 변경되면 데이터가 꼬일 수 있음
	private EventStatus eventStatus = EventStatus.DRAFT;
	@ManyToOne
	private Account manager;

	public void update() {
		if (this.basePrice == 0 && this.maxPrice == 0) {
			this.free = true;
		} else {
			this.free = false;
		}

		if (this.location == null || this.location.isEmpty()) {
			this.offline = false;
		} else {
			this.offline = true;
		}

		if (this.eventStatus == null) {
			this.eventStatus = EventStatus.DRAFT;
		}
	}
}
