package org.mule.templates.flows;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.config.MuleProperties;
import org.mule.construct.Flow;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.FunctionalTestCase;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Mule Kick.
 * 
 * @author cesar.garcia
 */
public class FlowsTest extends FunctionalTestCase {
	private static final String CONTACTS_FROM_ORG_A = "contactsFromOrgA";
	private static final String CONTACTS_FROM_ORG_B = "contactsFromOrgB";

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {
	}

	@Override
	protected String getConfigResources() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(
					"./src/main/app/mule-deploy.properties"));
			return props.getProperty("config.resources");
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not find mule-deploy.properties file on classpath. Please add any of those files or override the getConfigResources() method to provide the resources by your own.");
		}
	}

	@Override
	protected Properties getStartUpProperties() {
		Properties properties = new Properties(super.getStartUpProperties());

		String pathToResource = "./mappings";
		File graphFile = new File(pathToResource);

		properties.put(MuleProperties.APP_HOME_DIRECTORY_PROPERTY,
				graphFile.getAbsolutePath());

		return properties;
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testAggregationFlow() throws Exception {
		List<Map<String, String>> contactsFromOrgA = createContactLists("A", 0,
				1);
		List<Map<String, String>> contactsFromOrgB = createContactLists("B", 1,
				2);

		MuleEvent testEvent = getTestEvent("",
				MessageExchangePattern.REQUEST_RESPONSE);
		testEvent.getMessage().setInvocationProperty(CONTACTS_FROM_ORG_A,
				contactsFromOrgA.iterator());
		testEvent.getMessage().setInvocationProperty(CONTACTS_FROM_ORG_B,
				contactsFromOrgB.iterator());

		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("aggregationFlow");
		flow.initialise();
		MuleEvent event = flow.process(testEvent);

		Assert.assertTrue("The payload should not be null.", event.getMessage()
				.getPayload() != null);
		Assert.assertFalse("The contact list should not be empty.",
				((List) event.getMessage().getPayload()).isEmpty());
	}

	@Test
	public void testFormatOutputFlow() throws Exception {
		List<Map<String, String>> contactsFromOrgA = createContactLists("A", 0,
				1);
		List<Map<String, String>> contactsFromOrgB = createContactLists("B", 1,
				2);

		MuleEvent testEvent = getTestEvent("",
				MessageExchangePattern.REQUEST_RESPONSE);
		testEvent.getMessage().setInvocationProperty(CONTACTS_FROM_ORG_A,
				contactsFromOrgA.iterator());
		testEvent.getMessage().setInvocationProperty(CONTACTS_FROM_ORG_B,
				contactsFromOrgB.iterator());

		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("aggregationFlow");
		flow.initialise();
		MuleEvent event = flow.process(testEvent);

		flow = getSubFlow("formatOutputFlow");
		flow.initialise();
		event = flow.process(event);

		Assert.assertTrue("The payload should not be null.", event.getMessage()
				.getPayload() != null);
	}

	private Flow getFlow(String flowName) {
		return (Flow) muleContext.getRegistry().lookupObject(flowName);
	}

	private List<Map<String, String>> createContactLists(String orgId,
			int start, int end) {
		List<Map<String, String>> contactList = new ArrayList<Map<String, String>>();
		for (int i = start; i <= end; i++) {
			contactList.add(createContact(orgId, i));
		}
		return contactList;
	}

	private Map<String, String> createContact(String orgId, int sequence) {
		Map<String, String> contact = new HashMap<String, String>();

		contact.put("Id", new Integer(sequence).toString());
		contact.put("Name", "SomeName_" + sequence);
		contact.put("Email", "some.email." + sequence + "@fakemail.com");

		return contact;
	}

}
