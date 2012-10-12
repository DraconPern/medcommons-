package net.medcommons.modules.cxp.server;

import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;
import net.medcommons.router.services.repository.RepositoryException;

public interface RLSHandler {
	public void newDocumentEvent(ServicesFactory servicesFactory, String storageId, String guid, String trackingNumber,CXPConstants.MergeCCRValues mergeIncomingCCR, String pin, String notificationSubject, String fromEmail) throws RepositoryException,ServiceException,Exception;
}
