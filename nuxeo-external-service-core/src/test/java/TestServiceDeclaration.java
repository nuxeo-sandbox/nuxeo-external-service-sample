import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogRecord;
import org.nuxeo.lib.stream.log.LogTailer;
import org.nuxeo.lib.stream.log.Name;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.RuntimeStreamFeature;
import org.nuxeo.runtime.stream.StreamService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.nuxeo.service.ExternalServiceConfigDescriptor;
import com.nuxeo.service.ExternalServiceWrapper;
import com.nuxeo.service.ExternalServiceWrapperComponent;
import com.nuxeo.service.messages.ExternalServiceMessage;

@RunWith(FeaturesRunner.class)
@Features(RuntimeStreamFeature.class)
@Deploy("com.nuxeo.external.service.core")
@Deploy("com.nuxeo.external.service.core.test:service-wrapper-config.xml")
public class TestServiceDeclaration {

	@Test
	public void verifyServiceWrapperDeployed() {
		ExternalServiceWrapper wrapper = Framework.getService(ExternalServiceWrapper.class);
		assertNotNull(wrapper);
	}

	@Test
	public void verifyServiceConfig() {
		ExternalServiceWrapper wrapper = Framework.getService(ExternalServiceWrapper.class);
		
		// access methods not exposed via service interface
		ExternalServiceWrapperComponent component = (ExternalServiceWrapperComponent) wrapper;
		
		
		assertEquals(2, component.getConfigs().size());
		
		ExternalServiceConfigDescriptor config = component.getConfig("externalservice");
		assertNotNull(config);
		
		ExternalServiceConfigDescriptor config2 = component.getConfig("externalserviceTest");
		assertNotNull(config2);	
		
		assertEquals("externalserviceTest", config2.getNamespace());		
		
	}
	
}
