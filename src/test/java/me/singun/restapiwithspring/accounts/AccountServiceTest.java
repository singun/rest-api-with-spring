package me.singun.restapiwithspring.accounts;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceTest {

	@Autowired
	AccountService accountService;

	@Autowired
	AccountRepository accountRepository;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void findByUsername() {
		// given
		String password = "keesun";
		String username = "keesun@email.com";
		Set<AccountRole> accountRoles = new HashSet<AccountRole>();
		accountRoles.add(AccountRole.ADMIN);
		accountRoles.add(AccountRole.USER);

		Account account = Account.builder()
			.email(username)
			.password(password)
			.roles(accountRoles)
			.build();

		this.accountRepository.save(account);

		// when
		UserDetailsService userDetailsService = accountService;
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);

		// then
		assertThat(userDetails.getPassword()).isEqualTo(password);
	}

	@Test(expected = UsernameNotFoundException.class)
	public void findByUserNameFail_way1() {
		// given
		String username = "random@email.com";

		// then
		accountService.loadUserByUsername(username);
	}

	@Test
	public void findByUserNameFail_way2() {
		// given
		String username = "random@email.com";

		// then
		try {
			accountService.loadUserByUsername(username);
			fail("supposed to be failed");
		} catch (UsernameNotFoundException e) {
			assertThat(e.getMessage()).containsSequence(username);
		}
	}

	@Test
	public void findByUserNameFail_way3() {
		// given
		String username = "random@email.com";
		expectedException.expect(UsernameNotFoundException.class);
		expectedException.expectMessage(Matchers.containsString(username));

		// when
		accountService.loadUserByUsername(username);
	}
}