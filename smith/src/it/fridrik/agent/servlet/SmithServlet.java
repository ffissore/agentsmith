/*
 * Smith Servlet - Enable Smith in your webapp
 * Copyright (C) 2007  Federico Fissore
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.fridrik.agent.servlet;

import it.fridrik.agent.Smith;
import it.fridrik.agent.SmithLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Loads Smith together with the webapp into which SmithServlet is installed.
 * You configure this servlet with the following piece of xml <br/>
 * 
 * <pre>
 *  &lt;servlet&gt;
 *    &lt;servlet-name&gt;SmithServlet&lt;/servlet-name&gt;
 *    &lt;servlet-class&gt;it.fridrik.agent.servlet.SmithServlet&lt;/servlet-class&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;smith.jar&lt;/param-name&gt;
 *        &lt;param-value&gt;/usr/share/smith/lib/smith.jar&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;smith.classes.path&lt;/param-name&gt;
 *        &lt;param-value&gt;/WEB-INF/classes/&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;smith.jars.path&lt;/param-name&gt;
 *        &lt;param-value&gt;/WEB-INF/lib/&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;smith.monitor.period&lt;/param-name&gt;
 *        &lt;param-value&gt;1000&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *    &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *  &lt;/servlet&gt;
 * </pre>
 * 
 * @author Federico Fissore (federico@fsfe.org)
 */
public class SmithServlet extends HttpServlet {

	private static final long serialVersionUID = 4236090740328025343L;

	@Override
	public void init() throws ServletException {
		super.init();

		String smithJar = getPath("smith.jar");
		String classesPath = getPath("smith.classes.path");
		String jarsPath = getPath("smith.jars.path");
		int monitorPeriod = Integer.parseInt(getPath("smith.monitor.period"));

		try {
			SmithLoader.hotStart(smithJar, classesPath, jarsPath, monitorPeriod);
		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

	@Override
	public void destroy() {
		super.destroy();

		Smith.stopAll();
	}

	private String getPath(String propParam) {
		String result = this.getServletConfig().getInitParameter(propParam);
		if (result.startsWith("/WEB-INF/")) {
			String prefix = this.getServletContext().getRealPath("/");
			result = prefix + result;
		}
		return result;
	}
}
