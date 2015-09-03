package com.redhat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class DroolsTest {

	private StatelessDecisionService service = BrmsHelper.newStatelessDecisionServiceBuilder().auditLogName("audit").build();

	
	public void helloWorldTest() {
		// given
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setName("test");
		facts.add(business);

		// when
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);

		// then
		Assert.assertEquals("test", response.getBusiness().getName());
	}
	
	@Test
	public void shouldFilterOutAllRequestsFromKansas(){
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setStateCode("KS");
		facts.add(business);
		
		// when I apply the filtering rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class); 
		
		// then the business should be filtered
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBusiness());
		//Assert.assertEquals("filtered", response.getResponseCode());
		
		// and the reason message should be "business filtered from Kansas"
		boolean found = false;
		for (Reason reason : response.getReasons()){
			if ( reason.getReasonMessage().equals( "business filtered: from Kansas") ){
				found = true;
			}
		}
		Assert.assertTrue( "business filtered from Kansas: from Kansas", found );
	}
	
	public void shouldProcessAllBusinessesNotFromKansas(){
		// scenario: we are responsible for all businesses not from Kansas
		// given a business from New York
		Collection<Object> facts = new ArrayList<Object>();
		Business business = new Business();
		business.setStateCode("NY");
		facts.add(business);
		
		// when I apply the filtering rules
		RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);
		
		// then the business should be not be filtered
		Assert.assertNotNull(response);
		Assert.assertNull(response.getResponseCode());
		
		// and the validation rules should be applied to the business
		Assert.assertNotNull(response.getReasons());
		Assert.assertTrue(response.getReasons().isEmpty());
	}

	public void shouldCreateValidationErrorsForAnyFieldThatAreEmptyOrNull(){
		// scenario: all fields must have values. 
				// given a business 
				// and the business' zipcode is empty
				// and the business' address line 1 is null
				Collection<Object> facts = new ArrayList<Object>();
				Business business = new Business();
				business.setName("test");
				business.setAddressLine2("addr line 2");
				business.setFederalTaxId("federalTaxId");
				business.setCity("city");
				business.setPhoneNumber("phoneNumber");
				business.setStateCode("stateCode");
				business.setZipCode("");
				facts.add(business);
				// when I apply the validation rules
				RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);
				// then the business should be return a validation error
				// and a message should say the zipcode is empty
				// and a message should say the address is null
				Assert.assertNotNull(response);
				Assert.assertTrue(response.getResponseCode().equals("validation error"));
				Assert.assertEquals(2, response.getReasons().size());
				Reason zipReason = new Reason();
				zipReason.setReasonMessage("zipcode is empty");
				Assert.assertTrue(response.getReasons().contains(zipReason));
				Reason addrReason = new Reason();
				addrReason.setReasonMessage("address line 1 is null");
				Assert.assertTrue(response.getReasons().contains(addrReason));
	}
	@Test
	public void shouldEnrichTheTaxIdWithZipCode(){
		// scenario: we need to enrich the taxId with the zipcode for system XYZ
				// given a business
				// and the business' zipcode is 10002
				// and the business' taxId is 98765

				Collection<Object> facts = new ArrayList<Object>();
				Business business = new Business();
				business.setZipCode("10002");
				business.setFederalTaxId("98765");
				facts.add(business);

				// when I apply the enrichment rules

				RuleResponse response = service.runRules(facts, "VerifySupplier", RuleResponse.class);

				// then the business' taxId should be enriched to 98765-10002
				Assert.assertNotNull(business);
				Assert.assertEquals("98765-10002", business.getFederalTaxId());
	}
	
}
