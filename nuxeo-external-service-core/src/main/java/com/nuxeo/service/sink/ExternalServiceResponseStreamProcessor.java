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
package com.nuxeo.service.sink;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.AbstractComputation;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.AvroCodecFactory;
import org.nuxeo.runtime.stream.StreamProcessorTopology;

import com.nuxeo.service.ExternalServiceWrapper;
import com.nuxeo.service.messages.ExternalServiceMessage;

/**
 * 
 * Computation used to consume the messages sent by the external service.
 * The actual processing of the message is delegated to {@link ExternalServiceWrapper}.
 * 
 * @author tiry
 *
 */
public class ExternalServiceResponseStreamProcessor implements StreamProcessorTopology {

	private static final Log log = LogFactory.getLog(ExternalServiceResponseStreamProcessor.class);

	@Override
	public Topology getTopology(Map<String, String> options) {
		String serviceName = options.get("serviceName");
		String inputStream = options.get("inputStream");
		String computationName = serviceName + "ReponseHandlerComputation";
		return Topology.builder()
				.addComputation(() -> new ExternalServiceResponseComputation(computationName, serviceName),
						Arrays.asList("i1:" + inputStream))
				.build();
	}

	// Simple computation that forward a record
	protected static class ExternalServiceResponseComputation extends AbstractComputation {

		protected String serviceName;

		protected Codec<ExternalServiceMessage> codec;

		public ExternalServiceResponseComputation(String name, String serviceName) {
			super(name, 1, 1);
			this.serviceName = serviceName;
			AvroCodecFactory avroFactory = new AvroCodecFactory();
			Map<String, String> options = new HashMap<>();
			options.put(AvroCodecFactory.KEY_ENCODING, "json");
			avroFactory.init(options);

			codec = avroFactory.newCodec(ExternalServiceMessage.class);

		}

		@Override
		public void processRecord(ComputationContext context, String inputStreamName, Record record) {

			log.debug(metadata.name() + " got record: " + record);

			System.out.println(metadata.name() + " got record: " + record);

			ExternalServiceMessage message = codec.decode(record.getData());
			Framework.getService(ExternalServiceWrapper.class).handleResponseMessage(serviceName, message);
		}
	}
}
