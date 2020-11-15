/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Tiry
 */

package com.nuxeo.service.cli;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogOffset;
import org.nuxeo.lib.stream.log.Name;

public class SendResponseToNuxeo extends BaseCLI {
	
	public static void main(String[] args) {
		new SendResponseToNuxeo().run(args);
	}

	protected void declareAdditionalOptions(Options options) {

		options.addOption("m", "message", true, "json message (file or inline content)");

	}

	protected void handleCommand(CommandLine cmd, Options options, LogManager lm, String serviceName) {

		String json = cmd.getOptionValue("m");
		if (json == null) {
			System.err.println("no input message found ");
			new HelpFormatter().printHelp("SendResponseToNuxeo", options);
			return;
		}
		json = json.trim();
		if (!json.startsWith("{")) {
			// load from file
			try {
				json = FileUtils.readFileToString(new File(json), "UTF-8");
			} catch (IOException e) {
				System.err.println("Unable to json from file " + json + ":" + e.getMessage());
				return;
			}
		}

		LogAppender<Externalizable> appender = lm.getAppender(Name.of(serviceName, "response"));

		String key = UUID.randomUUID().toString();

		Record record = Record.of(key, json.getBytes());

		LogOffset offset = appender.append(key, record);

		System.out.println("Message appended to offset " + offset.toString());

	}
}
