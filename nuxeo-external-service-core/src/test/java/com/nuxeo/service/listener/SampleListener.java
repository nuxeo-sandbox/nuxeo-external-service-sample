package com.nuxeo.service.listener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.blob.ManagedBlob;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

import com.nuxeo.service.ExternalServiceWrapper;
import com.nuxeo.service.messages.ExternalServiceMessage;

public class SampleListener implements PostCommitFilteringEventListener {

	protected final List<String> handled = Arrays.asList("documentCreated", "documentModified");

	// Async Listener receive a list (bundle) of 
	// all the events that were generated during the transaction
	@Override
	public void handleEvent(EventBundle events) {
		for (Event event : events) {
			if (acceptEvent(event)) {
				handleEvent(event);
			}
		}
	}

	@Override
	public boolean acceptEvent(Event event) {
		return handled.contains(event.getName());
	}

	public void handleEvent(Event event) {
		EventContext ctx = event.getContext();
		if (!(ctx instanceof DocumentEventContext)) {
			return;
		}

		DocumentEventContext docCtx = (DocumentEventContext) ctx;
		DocumentModel doc = docCtx.getSourceDocument();
		
		ExternalServiceMessage msg = new ExternalServiceMessage();
		msg.command=event.getName();		
		msg.getParameters().put("repository", doc.getRepositoryName());
		msg.getParameters().put("docId", doc.getId());
		msg.getParameters().put("docType", doc.getType());
		msg.getParameters().put("title", (String) doc.getPropertyValue("dc:title"));
		Blob blob = (Blob) doc.getPropertyValue("file:content");
		
		if (blob instanceof ManagedBlob) {
			String blobKey = ((ManagedBlob)blob).getKey();
			// using the key, the external service could read the blob directly
			msg.getParameters().put("blob_key", blobKey);		
		}
		msg.getParameters().put("blob_name", blob.getFilename());
		msg.getParameters().put("blob_type", blob.getMimeType());
		
		if ("text/plain".equals(blob.getMimeType())) {
			try {
				msg.getParameters().put("blob_text", new String(blob.getByteArray(), "UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else {
			try {
				msg.getParameters().put("blob_b64", new String(Base64.getEncoder().encode(blob.getByteArray())
, "UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}						
		}
		
		ExternalServiceWrapper wrapper = Framework.getService(ExternalServiceWrapper.class);
		wrapper.postMessage("externalservice", msg);		
		
	}
}