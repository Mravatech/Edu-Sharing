package org.edu_sharing.service.organization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;
import org.edu_sharing.repository.client.rpc.EduGroup;
import org.edu_sharing.restservices.DAOException;

public interface OrganizationService {

	String createOrganization(String orgName, String groupDisplayName) throws Throwable;

	Map<QName, Serializable> getOrganisation(String orgName);

	String createOrganization(String orgName, String groupDisplayName, String metadataSet) throws Throwable;
}
