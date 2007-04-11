/*
 * Smith Servlet - Enable Smith in your webapp
 * Copyright (C) 2007 Federico Fissore
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.fridrik.agent.servlet;

import it.fridrik.agent.Smith;
import it.fridrik.agent.SmithArgs;
import it.fridrik.agent.SmithLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Loads Smith together with the webapp into which SmithServlet is installed.
 * You configure this servlet with the following piece of xml <br/>
 * 
 * <pre>
 *     &lt;servlet&gt;
 *       &lt;servlet-name&gt;SmithServlet&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;it.fridrik.agent.servlet.SmithServlet&lt;/servlet-class&gt;
 *         &lt;init-param&gt;
 *           &lt;param-name&gt;smith.jar&lt;/param-name&gt;
 *           &lt;param-value&gt;/usr/share/smith/lib/smith.jar&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *           &lt;param-name&gt;smith.classes.path&lt;/param-name&gt;
 *           &lt;param-value&gt;/WEB-INF/classes/&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *           &lt;param-name&gt;smith.jars.path&lt;/param-name&gt;
 *           &lt;param-value&gt;/WEB-INF/lib/&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *           &lt;param-name&gt;smith.monitor.period&lt;/param-name&gt;
 *           &lt;param-value&gt;1000&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *         &lt;init-param&gt;
 *           &lt;param-name&gt;smith.log.level&lt;/param-name&gt;
 *           &lt;param-value&gt;SEVERE&lt;/param-value&gt;
 *         &lt;/init-param&gt;
 *       &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *     &lt;/servlet&gt;
 * </pre>
 * 
 * @author Federico Fissore (federico@fsfe.org)
 */
public class SmithServlet extends HttpServlet {

	private static final long serialVersionUID = 4236090740328025343L;

	@Override
	public void init() throws ServletException {
		super.init();

		String smithJar = getParameter("smith.jar");
		String classesPath = getParameter("smith.classes.path");
		String jarsPath = getParameter("smith.jars.path");
		int monitorPeriod = Integer.parseInt(getParameter("smith.monitor.period"));

		String logLevel = getParameter("smith.log.level");

		SmithArgs args = new SmithArgs(classesPath, jarsPath, monitorPeriod, logLevel);

		try {
			SmithLoader.hotStart(smithJar, args);
		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	@Override
	public void destroy() {
		super.destroy();

		Smith.stopAll();
	}

	private String getParameter(String propParam) {
		String result = this.getServletConfig().getInitParameter(propParam);
		if (result.startsWith("/WEB-INF/")) {
			String prefix = this.getServletContext().getRealPath("/");
			result = prefix + result;
		}
		return result;
	}
}
