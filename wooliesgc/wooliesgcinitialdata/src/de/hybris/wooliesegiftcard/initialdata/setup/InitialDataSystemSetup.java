/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.wooliesegiftcard.initialdata.setup;

import de.hybris.platform.commerceservices.dataimport.impl.CoreDataImportService;
import de.hybris.platform.commerceservices.dataimport.impl.SampleDataImportService;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.wooliesegiftcard.initialdata.constants.WooliesgcInitialDataConstants;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * This class provides hooks into the system's initialization and update processes.
 */
@SystemSetup(extension = WooliesgcInitialDataConstants.EXTENSIONNAME)
public class InitialDataSystemSetup extends AbstractSystemSetup
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(InitialDataSystemSetup.class);

	private static final String IMPORT_CORE_DATA = "importCoreData";
	private static final String IMPORT_SAMPLE_DATA = "importSampleData";
	private static final String ACTIVATE_SOLR_CRON_JOBS = "activateSolrCronJobs";
	private static final String WOOLIES_IMPORT_HOME = "/wooliesgcinitialdata/import";

	private CoreDataImportService coreDataImportService;
	private SampleDataImportService sampleDataImportService;
	private ConfigurationService configurationService;


	/**
	 * Generates the Dropdown and Multi-select boxes for the project data import
	 */
	@Override
	@SystemSetupParameterMethod
	public List<SystemSetupParameter> getInitializationOptions()
	{
		final List<SystemSetupParameter> params = new ArrayList<SystemSetupParameter>();

		//params.add(createBooleanSystemSetupParameter(IMPORT_CORE_DATA, "Import Core Data", true));
		//		params.add(createBooleanSystemSetupParameter(IMPORT_SAMPLE_DATA, "Import Sample Data", true));
		//		params.add(createBooleanSystemSetupParameter(ACTIVATE_SOLR_CRON_JOBS, "Activate Solr Cron Jobs", true));
		// Add more Parameters here as you require

		return params;
	}

	/**
	 * Implement this method to create initial objects. This method will be called by system creator during
	 * initialization and system update. Be sure that this method can be called repeatedly.
	 *
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
	public void createEssentialData(final SystemSetupContext context)
	{
		// Add Essential Data here as you require

		if (getConfigurationService().getConfiguration().getBoolean("import.essentialData.forUpdate", false))
		{
			LOG.info("createEssentialData start");
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/essential-data.impex", true);
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/productCatalogs/catalogName/catalog.impex", true);
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/contentCatalogs/catalogName/catalog.impex", true);
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/stores/storeName/store.impex", true);
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/stores/storeName/site.impex", true);
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/sampledata/stores/storeName/warehouses.impex", true);
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/delivery-modes.impex", true);
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/backoffice-user-rights.impex", true);
			importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/productcockpit-constraints.impex", true);
			LOG.info("createEssentialData end");
		}
	}

	/**
	 * Implement this method to create data that is used in your project. This method will be called during the system
	 * initialization. <br>
	 * Add import data for each site you have configured
	 *
	 * <pre>
	 * final List<ImportData> importData = new ArrayList<ImportData>();
	 *
	 * final ImportData sampleImportData = new ImportData();
	 * sampleImportData.setProductCatalogName(SAMPLE_PRODUCT_CATALOG_NAME);
	 * sampleImportData.setContentCatalogNames(Arrays.asList(SAMPLE_CONTENT_CATALOG_NAME));
	 * sampleImportData.setStoreNames(Arrays.asList(SAMPLE_STORE_NAME));
	 * importData.add(sampleImportData);
	 *
	 * getCoreDataImportService().execute(this, context, importData);
	 * getEventService().publishEvent(new CoreDataImportedEvent(context, importData));
	 *
	 * getSampleDataImportService().execute(this, context, importData);
	 * getEventService().publishEvent(new SampleDataImportedEvent(context, importData));
	 * </pre>
	 *
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.PROJECT, process = Process.ALL)
	public void createProjectData(final SystemSetupContext context)
	{


		// Import essential data
		LOG.info("createProjectData start");
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/essential-data.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/productCatalogs/catalogName/catalog.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/contentCatalogs/catalogName/catalog.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/stores/storeName/store.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/stores/storeName/site.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/sampledata/stores/storeName/warehouses.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/sampledata/productCatalogs/catalogName/products-promotions.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/sampledata/productCatalogs/catalogName/categories.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/sampledata/productCatalogs/catalogName/products.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/sampledata/productCatalogs/catalogName/products-media.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/sampledata/productCatalogs/catalogName/products-prices.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/sampledata/productCatalogs/catalogName/products-pos-stocklevels.impex",
				true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/syncjobs.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/delivery-modes.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/backoffice-user-rights.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/productcockpit-constraints.impex", true);
		importImpexFile(context, WOOLIES_IMPORT_HOME + "/coredata/common/cronjobs.impex", true);
		LOG.info("createProjectData end");
	}

	public CoreDataImportService getCoreDataImportService()
	{
		return coreDataImportService;
	}

	@Required
	public void setCoreDataImportService(final CoreDataImportService coreDataImportService)
	{
		this.coreDataImportService = coreDataImportService;
	}

	public SampleDataImportService getSampleDataImportService()
	{
		return sampleDataImportService;
	}

	@Required
	public void setSampleDataImportService(final SampleDataImportService sampleDataImportService)
	{
		this.sampleDataImportService = sampleDataImportService;
	}

	/**
	 * @return the configurationService
	 */
	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	/**
	 * @param configurationService
	 *           the configurationService to set
	 */
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}
}
