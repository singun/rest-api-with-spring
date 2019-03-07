package me.singun.restapiwithspring.accounts;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

	@Id @GeneratedValue
	private Integer id;

	private String email;

	private String password;

	@ElementCollection(fetch = FetchType.EAGER) // todo 너는 뭐징 ?
	@Enumerated(EnumType.STRING)
	private Set<AccountRole> roles;
}
