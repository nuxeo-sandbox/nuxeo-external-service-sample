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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.nuxeo.lib.stream.codec.AvroMessageCodec;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogRecord;
import org.nuxeo.lib.stream.log.LogTailer;
import org.nuxeo.lib.stream.log.Name;

public class GetRequestFromNuxeo extends BaseCLI {
	
	public static void main(String[] args) {
		new GetRequestFromNuxeo().run(args);
	}
	
	protected void declareAdditionalOptions(Options options) {

		options.addOption("o", "out", true, "path to store the message (stdout used otherwise)");
		options.addOption("t", "timeout", true, "number of seconds to wait for a messsage");

	}

	protected void handleCommand(CommandLine cmd, Options options, LogManager lm, String serviceName) {

		String out = cmd.getOptionValue("o");
		long t = Long.parseLong(cmd.getOptionValue("t", "1"));

		LogTailer<Record> tailer = lm.createTailer(Name.of(serviceName, "CLI"), Name.of(serviceName, "request"), new AvroMessageCodec(Record.class));

		LogRecord<Record> logEntry;
		try {
			logEntry = tailer.read(Duration.ofSeconds(t));
		} catch (InterruptedException e) {
			System.err.println("Unable read from stream :" + e.getMessage());
			e.printStackTrace();
			return;
		} finally {
			tailer.close();
		}

		if (logEntry == null) {
			System.out.println("No message found within the provided timeout");
			return;
		} else {
			Record record = logEntry.message();
			String json;
			try {
				json = new String(record.getData(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				System.err.println("Unable decode message payload:" + e.getMessage());
				e.printStackTrace();
				return;
			}

			if (out == null) {
				System.out.println(json);
			} else {
				try {
					FileUtils.writeStringToFile(new File(out), json, "UTF-8");
				} catch (IOException e) {
					System.err.println("Unable same messate to file:" + out);
					e.printStackTrace();
					return;
				}
			}
		}

	}
}
