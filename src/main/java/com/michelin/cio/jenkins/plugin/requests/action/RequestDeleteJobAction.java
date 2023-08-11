/*
 * The MIT License
 *
 * Copyright (c) 2011-2012, Manufacture Francaise des Pneumatiques Michelin, Daniel Petisme, Romain Seguy
 * Copyright 2019 Lexmark
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.michelin.cio.jenkins.plugin.requests.action;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import com.michelin.cio.jenkins.plugin.requests.RequestsPlugin;
import com.michelin.cio.jenkins.plugin.requests.model.DeleteJobRequest;

import hudson.Functions;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;

// Represents the "Request for deletion" action appearing on a given job's page.
// @author Daniel Petisme <daniel.petisme@gmail.com> <http://danielpetisme.blogspot.com/>

public class RequestDeleteJobAction implements Action {

	private Job<?, ?> project;
	private Job<?, ?> project2;
	private static final Logger LOGGER = Logger.getLogger(RequestDeleteJobAction.class.getName());

	public RequestDeleteJobAction(Job<?, ?> target) {
		project2 = (Job<?, ?>) target.getTarget();
		this.project = project2;
	}

	@POST
	public HttpResponse doCreateDeleteJobRequest(StaplerRequest request, StaplerResponse response) throws IOException, ServletException, MessagingException {

		try {
			if (isIconDisplayed()) {
				// errors.clear();
				final String username = request.getParameter("username");
				RequestsPlugin plugin = Jenkins.get().getPlugin(RequestsPlugin.class);
				if (plugin == null) {
					LOGGER.log(Level.SEVERE, "[ERROR] Jenkins.get().getPlugin(RequestsPlugin.class) is null: ");
					return null;
				}
				String jobName = project.getFullName();
				String fullJobURL = "";
				String jobNameSlash = jobName;
				String jobNameJelly = "";
				LOGGER.info("Delete Job Request project.getFullName(): " + jobName);
				String[] emailData = { project.getName(), username, "A Delete Job", project.getAbsoluteUrl() };

				if (jobName.contains("/")) {
					String[] projectnameList = jobName.split("/");
					int nameCount = projectnameList.length;
					jobName = projectnameList[nameCount - 1];
				}

				fullJobURL = project.getAbsoluteUrl();
				LOGGER.info("Delete Job Request fullJobURL: " + fullJobURL);

				jobNameJelly = jobNameSlash;
				if (jobNameJelly.contains("%20")) {
					jobNameJelly = jobNameJelly.replace("%20", " ");
				}

				// CHECK for null values:
				// -----------------------------------
				// username =
				// jobName = Good
				// fullJobURL = Good
				// jobNameSlash = Good
				// jobNameJelly =
				// emailData =

				plugin.addRequestPlusEmail(new DeleteJobRequest("deleteJob", username, jobName, "", fullJobURL, jobNameSlash, jobNameJelly, ""), emailData);
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "[ERROR] doCreateDeleteJobRequest() Exception: " + e.getMessage());

			return null;
		}

		LOGGER.info("Delete Job Request request.getContextPath()/project.getUrl(): " + request.getContextPath() + '/' + project.getUrl());

		return new HttpRedirect(request.getContextPath() + '/' + project.getUrl());
	}

	public String getDisplayName() {
		if (isIconDisplayed()) {
			return Messages.RequestDeleteJobAction_DisplayName();
		}
		return null;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	public String getIconClassName() {
		if (isIconDisplayed()) {
			return "icon-edit-delete";
		}
		return null;
	}

	public Job<?, ?> getProject() {
		Job<?, ?> project2a;
		project2a = (Job<?, ?>) project2.getTarget();
		return project2a;
	}

	public String getUrlName() {
		return "request-delete-job";
	}

	/**
	 * Displays the icon when the user can configure and !delete.
	 */
	private boolean isIconDisplayed() {
		boolean isDisplayed = false;
		try {
			// isDisplayed = hasConfigurePermission() && !hasDeletePermission();
			isDisplayed = !hasDeletePermission();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Impossible to know if the icon has to be displayed", e);
		}

		return isDisplayed;
	}

	// private boolean hasConfigurePermission() throws IOException, ServletException
	// {
	// return Functions.hasPermission(project, Item.CONFIGURE);
	// }

	private boolean hasDeletePermission() throws IOException, ServletException {
		return Functions.hasPermission(project, Item.DELETE);
	}

}
