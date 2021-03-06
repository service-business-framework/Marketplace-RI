package org.fiware.apps.marketplace.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fiware.apps.marketplace.helpers.AttributeTypeResolver;
import org.fiware.apps.marketplace.helpers.AttributeTypeStatisticsResolver;
import org.fiware.apps.marketplace.helpers.OfferingResolver;
import org.fiware.apps.marketplace.helpers.ServiceManifestationResolver;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.ServiceAttributeType;
import org.fiware.apps.marketplace.model.ServiceAttributeTypeStatistics;
import org.fiware.apps.marketplace.model.ServiceManifestation;
import org.junit.BeforeClass;
import org.junit.Test;

public class AttributeTypeStatisticsResolverTest {

	private static final String amazonUri = "http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/cloudServices/Amazon_EC2_001";
	private static final String rackspaceUri = "http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/cloudServices/Rackspace_CloudServers_001";
	private static final String elasticHostsUri = "http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/cloudServices/ElasticHosts_CloudHosting_001";
	private static final String reliaCloudUri = "http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/cloudServices/Visi_ReliaCloud_001";
	private static final String cloudVocUri = "http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/vocabulary/Vocabulary_Cloud_004";
	private static final String supportVocUri = "http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/vocabulary/Vocabulary_Support_003";
	private static final String osVocUri = "http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/vocabulary/Vocabulary_OperatingSystem_003";
	private static final String generalVocUri = "http://appsnserv.testbed.fi-ware.eu/cloudservices/rdf/vocabulary/Vocabulary_General_001";
	private static final double epsilon = 1.0 / 1000000000.0;

	private static HashMap<String, ServiceAttributeType> types = new HashMap<String, ServiceAttributeType>();
	private static List<ServiceManifestation> serviceManifestationList = new ArrayList<ServiceManifestation>();

	@BeforeClass
	public static void setUpBeforeClass() {
		List<String> uris = new ArrayList<String>();
		uris.add(generalVocUri);
		uris.add(cloudVocUri);
		uris.add(supportVocUri);
		uris.add(osVocUri);
		uris.add(amazonUri);
		uris.add(rackspaceUri);
		uris.add(elasticHostsUri);
		uris.add(reliaCloudUri);

		for (String uri : uris) {
			for (ServiceAttributeType type : AttributeTypeResolver.resolveAttributeTypesFromUri(uri)) {
				types.put(type.getUri(), type);
			}
		}

		List<Offering> offerings = new ArrayList<Offering>();
		offerings.addAll(OfferingResolver.resolveOfferingsFromServiceDescription(amazonUri, ""));
		offerings.addAll(OfferingResolver.resolveOfferingsFromServiceDescription(rackspaceUri, ""));
		offerings.addAll(OfferingResolver.resolveOfferingsFromServiceDescription(elasticHostsUri, ""));
		offerings.addAll(OfferingResolver.resolveOfferingsFromServiceDescription(reliaCloudUri, ""));
		for (Offering offering : offerings) {
			serviceManifestationList.addAll(ServiceManifestationResolver.resolveServiceManifestations(offering));
		}
	}

	@Test
	public void testResolveStatistics_Keys() {
		HashMap<String, ServiceAttributeTypeStatistics> statisticsMap = AttributeTypeStatisticsResolver.resolveStatistics(types,
				serviceManifestationList);
		assertNotNull(statisticsMap);
		for (String firstLvlKey : statisticsMap.keySet()) {
			assertNotNull(firstLvlKey);
			if (statisticsMap.get(firstLvlKey).getTypeStatsMap().size() > 0) {
				for (String secondLvlKey : statisticsMap.get(firstLvlKey).getTypeStatsMap().keySet()) {
					assertNotNull(secondLvlKey);
					if (statisticsMap.get(firstLvlKey).getTypeStatsMap().get(secondLvlKey).getTypeStatsMap().keySet().size() > 0) {
						for (String thirdLvlKey : statisticsMap.get(firstLvlKey).getTypeStatsMap().get(secondLvlKey).getTypeStatsMap()
								.keySet()) {
							assertNotNull(thirdLvlKey);
						}
					}
				}
			}
		}
	}

	@Test
	public void testResolveStatistics_BranchSum() {
		HashMap<String, ServiceAttributeTypeStatistics> statisticsMap = AttributeTypeStatisticsResolver.resolveStatistics(types,
				serviceManifestationList);
		assertNotNull(statisticsMap);
		assertTrue(statisticsMap.keySet().size() > 0);

		Double probWin = statisticsMap.get(osVocUri + "#Windows").getOccurrenceProbability();
		Double probWinServ = statisticsMap.get(osVocUri + "#WindowsServer").getOccurrenceProbability();
		Double probWinServ2003 = statisticsMap.get(osVocUri + "#WindowsServer2003").getOccurrenceProbability();
		Double probWinServ2008 = statisticsMap.get(osVocUri + "#WindowsServer2008").getOccurrenceProbability();
		Double probWinServ2008Sp2 = statisticsMap.get(osVocUri + "#WindowsServer2008SP2").getOccurrenceProbability();
		Double probWinServ2008Sp2Standard = statisticsMap.get(osVocUri + "#WindowsServer2008SP2Standard").getOccurrenceProbability();
		Double probWinServ2008Sp2Enterprise = statisticsMap.get(osVocUri + "#WindowsServer2008SP2Enterprise").getOccurrenceProbability();
		Double probWinServ2008R2 = statisticsMap.get(osVocUri + "#WindowsServer2008R2").getOccurrenceProbability();
		Double probWinServ2008R2Standard = statisticsMap.get(osVocUri + "#WindowsServer2008R2Standard").getOccurrenceProbability();
		Double probWinServ2008R2Enterprise = statisticsMap.get(osVocUri + "#WindowsServer2008R2Enterprise").getOccurrenceProbability();

		// Every node on a branch must at least be as probable as its sub nodes
		assertTrue(probWinServ2008R2 - probWinServ2008R2Enterprise + probWinServ2008R2Standard >= 0.0);
		assertTrue(probWinServ2008Sp2 - probWinServ2008Sp2Enterprise + probWinServ2008Sp2Standard >= 0.0);
		assertTrue(probWinServ2008 - probWinServ2008R2 + probWinServ2008Sp2 >= 0.0);
		assertTrue(probWinServ - probWinServ2003 + probWinServ2008 >= 0.0);
		assertTrue(probWin - probWinServ >= 0.0);
	}

	@Test
	public void testResolveStatistics_SumIsOne() {
		assertTrue(getSumOfProbabilities(AttributeTypeStatisticsResolver.resolveStatistics(types, serviceManifestationList)) - 1.0 < epsilon);
	}

	private double getSumOfProbabilities(HashMap<String, ServiceAttributeTypeStatistics> stats) {
		double sum = 0.0;
		for (String key : stats.keySet()) {
			sum += stats.get(key).getOccurrenceProbability();
			if (stats.get(key).getTypeStatsMap().keySet().size() > 0)
				assertTrue(getSumOfProbabilities(stats.get(key).getTypeStatsMap()) - 1.0 < epsilon);
		}
		return sum;
	}
}
