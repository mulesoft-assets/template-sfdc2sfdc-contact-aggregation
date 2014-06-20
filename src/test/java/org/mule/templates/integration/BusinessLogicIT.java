/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import org.junit.*;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.streaming.ConsumerIterator;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.templates.builders.SfdcObjectBuilder;

import static org.mule.templates.builders.SfdcObjectBuilder.aContact;

import com.sforce.soap.partner.SaveResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Mule Template that make calls to external systems.
 * 
 * @author cesar.garcia
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {
	protected static final String TEMPLATE_NAME = "contact-aggregation";

	private static final String CONTACTS_FROM_ORG_A = "contactsFromOrgA";
	private static final String CONTACTS_FROM_ORG_B = "contactsFromOrgB";

	private List<Map<String, Object>> createdContactsInA = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> createdContactsInB = new ArrayList<Map<String, Object>>();

	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	@Before
	public void setUp() throws Exception {

		createContacts();
	}

	@SuppressWarnings("unchecked")
	private void createContacts() throws Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("createContactInAFlow");
		flow.initialise();

		Map<String, Object> contact = createContact("A", 0);
		createdContactsInA.add(contact);

		MuleEvent event = flow.process(getTestEvent(createdContactsInA,
				MessageExchangePattern.REQUEST_RESPONSE));
		List<SaveResult> results = (List<SaveResult>) event.getMessage()
				.getPayload();
		for (int i = 0; i < results.size(); i++) {
			createdContactsInA.get(i).put("Id", results.get(i).getId());
		}

		flow = getSubFlow("createContactInBFlow");
		flow.initialise();

		contact = createContact("B", 0);
		createdContactsInB.add(contact);

		event = flow.process(getTestEvent(createdContactsInB,
				MessageExchangePattern.REQUEST_RESPONSE));
		results = (List<SaveResult>) event.getMessage().getPayload();

		for (int i = 0; i < results.size(); i++) {
			createdContactsInB.get(i).put("Id", results.get(i).getId());
		}
	}

	protected Map<String, Object> createContact(String orgId, int sequence) {
		return SfdcObjectBuilder
				.aContact()
				.with("FirstName", "FirstName_" + orgId + sequence)
				.with("LastName",
						buildUniqueName(TEMPLATE_NAME, "LastName_" + sequence
								+ "_"))
				.with("Email", buildUniqueEmail("some.email." + sequence))
				.with("Description", "Some fake description")
				.with("MailingCity", "Denver").with("MailingCountry", "US")
				.with("MobilePhone", "123456789")
				.with("Department", "department_" + sequence + "_" + orgId)
				.with("Phone", "123456789").with("Title", "Dr").build();
	}

	protected String buildUniqueName(String templateName, String name) {
		String timeStamp = new Long(new Date().getTime()).toString();

		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(templateName);
		builder.append(timeStamp);

		return builder.toString();
	}

	protected String buildUniqueEmail(String contact) {
		String server = "fakemail";

		StringBuilder builder = new StringBuilder();
		builder.append(TEMPLATE_NAME + contact);
		builder.append("@");
		builder.append(server);
		builder.append(".com");

		return builder.toString();
	}

	@After
	public void tearDown() throws Exception {

		deleteTestContactFromSandBox(createdContactsInA,
				"deleteContactFromAFlow");
		deleteTestContactFromSandBox(createdContactsInB,
				"deleteContactFromBFlow");

	}

	protected void deleteTestContactFromSandBox(
			List<Map<String, Object>> createdContacts, String deleteFlow)
			throws Exception {
		List<String> idList = new ArrayList<String>();

		// Delete the created contacts in A
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow(deleteFlow);
		flow.initialise();
		for (Map<String, Object> c : createdContacts) {
			idList.add((String) c.get("Id"));
		}
		flow.process(getTestEvent(idList,
				MessageExchangePattern.REQUEST_RESPONSE));
		idList.clear();

	}

	@Test
	public void testGatherDataFlow() throws Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("gatherDataFlow");
		flow.initialise();

		MuleEvent event = flow.process(getTestEvent("",
				MessageExchangePattern.REQUEST_RESPONSE));
		Set<String> flowVariables = event.getFlowVariableNames();

		Assert.assertTrue("The variable contactsFromOrgA is missing.",
				flowVariables.contains(CONTACTS_FROM_ORG_A));
		Assert.assertTrue("The variable contactsFromOrgB is missing.",
				flowVariables.contains(CONTACTS_FROM_ORG_B));

		ConsumerIterator<Map<String, String>> contactsFromOrgA = event
				.getFlowVariable(CONTACTS_FROM_ORG_A);
		ConsumerIterator<Map<String, String>> contactsFromOrgB = event
				.getFlowVariable(CONTACTS_FROM_ORG_B);

		Assert.assertTrue(
				"There should be contacts in the variable contactsFromOrgA.",
				contactsFromOrgA.size() != 0);
		Assert.assertTrue(
				"There should be contacts in the variable contactsFromOrgB.",
				contactsFromOrgB.size() != 0);

	}

}
