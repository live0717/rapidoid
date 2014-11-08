package org.rapidoid.pages.bootstrap;

import org.rapidoid.html.Tag;
import org.rapidoid.http.HttpExchange;
import org.rapidoid.pages.PageComponent;
import org.rapidoid.pages.impl.PageRenderer;
import org.rapidoid.util.U;

/*
 * #%L
 * rapidoid-pages
 * %%
 * Copyright (C) 2014 Nikolche Mihajlovski
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

public abstract class BootstrapPage extends BootstrapWidget implements PageComponent {

	protected Tag<?> page() {
		return template("bootstrap-page.html", "title", pageTitle(), "style", pageStyle(), "head", pageHead(), "body",
				pageBody());
	}

	protected abstract Object pageBody();

	protected Object pageHead() {
		return "";
	}

	protected Object pageStyle() {
		return "";
	}

	protected Object pageTitle() {
		String pageName = getClass().getSimpleName();

		if (pageName.endsWith("Page")) {
			pageName = U.mid(pageName, 0, -4);
		}

		return pageName;
	}

	@Override
	public void render(HttpExchange x) {
		PageRenderer.get().render(content(), x);
	}

}
