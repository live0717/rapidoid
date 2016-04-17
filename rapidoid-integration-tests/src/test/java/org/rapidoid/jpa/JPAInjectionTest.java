package org.rapidoid.jpa;

import org.junit.Test;
import org.rapidoid.annotation.*;
import org.rapidoid.http.HttpTestCommons;
import org.rapidoid.http.HttpUtils;
import org.rapidoid.http.Req;
import org.rapidoid.ioc.IoCContext;
import org.rapidoid.ioc.IoCContextWrapper;
import org.rapidoid.setup.On;
import org.rapidoid.u.U;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/*
 * #%L
 * rapidoid-integration-tests
 * %%
 * Copyright (C) 2014 - 2016 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

@Authors("Nikolche Mihajlovski")
@Since("5.1.0")
public class JPAInjectionTest extends HttpTestCommons {

	@Test
	public void testJPAInjection() {
		JPA.bootstrap(path());
		On.path(path()).bootstrapControllers();

		postData("/books?title=a", U.map("title", "My Book 1"));
		postData("/books?title=b", U.map("title", "My Book 2"));
		postData("/books?title=c", U.map("title", "My Book 3"));

		onlyGet("/allBooks");

		onlyGet("/del?id=1");
		getAndPost("/del2?id=2");
		onlyPost("/del3?id=3");

		onlyGet("/allBooks?finally");
	}

}

@Controller
class MyCtrl {

	@Inject
	private IoCContext ioc;

	@javax.inject.Inject
	private EntityManager em;

	@Inject
	private EntityManagerFactory emf;

	@GET
	public Object allBooks() {
		checkInjected();
		return JPA.getAll(Book.class);
	}

	@POST("/books")
	@Transaction
	public Object insertBook(Book b) {
		checkInjected();
		return JPA.insert(b);
	}

	@GET
	@Transaction(TransactionMode.READ_WRITE)
	public Object del(long id) {
		checkInjected();

		U.must(!em.getTransaction().getRollbackOnly());

		em.remove(JPA.find(Book.class, id));
		em.flush(); // optional

		return U.list("DEL #" + id, JPA.getAllEntities().size() + " remaining");
	}

	@Page(raw = true)
	@Transaction
	public Object del2(long id, Req req) {
		checkInjected();

		U.must(em.getTransaction().getRollbackOnly() == HttpUtils.isGetReq(req));

		JPA.delete(Book.class, id);

		return U.list("DEL #" + id, JPA.getAllEntities().size() + " remaining");
	}

	@POST
	@Transaction(TransactionMode.READ_ONLY)
	public Object del3(long id) {
		checkInjected();

		U.must(em.getTransaction().getRollbackOnly());

		em.remove(em.find(Book.class, id));
		em.flush();

		return U.list("DEL #" + id, JPA.getAllEntities().size() + " remaining");
	}

	private void checkInjected() {
		U.notNull(emf, "emf");
		U.notNull(em, "em");
		U.notNull(ioc, "ioc");

		U.must(emf == SharedEntityManagerFactoryProxy.INSTANCE, "wrong emf");
		U.must(em == SharedContextAwareEntityManagerProxy.INSTANCE, "wrong em");

		U.must(ioc.singleton(MyCtrl.class) == this);
		U.must(ioc.singleton(EntityManager.class) == em);
		U.must(ioc.singleton(EntityManagerFactory.class) == emf);

		U.must(ioc.getClass().equals(IoCContextWrapper.class));
	}

}
