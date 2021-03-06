package org.edu_sharing.alfresco.fixes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.edu_sharing.repository.client.tools.CCConstants;

public class MetadataPresettingPolicy implements
		NodeServicePolicies.OnCreateNodePolicy,
		NodeServicePolicies.BeforeDeleteNodePolicy {

	private static final Log logger = LogFactory
			.getLog(MetadataPresettingPolicy.class);

	private static final QName CONTENT_TYPE = QName
			.createQName(CCConstants.CCM_TYPE_IO);

	private static final QName ASPECT_TYPE = QName
			.createQName(CCConstants.CCM_ASPECT_METADATA_PRESETTING);

	private static final QName ASPECT_PROP = QName
			.createQName(CCConstants.CCM_PROP_METADATA_PRESETTING_PROPERTIES);

	private static final QName ASPECT_ASSOC = QName
			.createQName(CCConstants.CCM_ASSOC_METADATA_PRESETTING_TEMPLATE);

	private final List<QName> defaultQNames = new ArrayList<QName>();

	/**
	 * The common node service
	 */
	protected NodeService nodeService;

	/**
	 * Policy component
	 */
	protected PolicyComponent policyComponent;

	/**
	 * Default Properties
	 */
	protected List<String> defaultProperties;

	/**
	 * Flag for delete policy
	 */
	protected boolean protectTemplatesInUse;

	/**
	 * Spring bean init method
	 */
	public void init() {

		logger.info("called!");

		policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME,
				CONTENT_TYPE, new JavaBehaviour(this, "onCreateNode"));
		
		policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME,
				ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode"));

		policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME,
				CONTENT_TYPE, new JavaBehaviour(this, "beforeDeleteNode"));

		defaultQNames.clear();
		if (defaultProperties != null) {
			for (String defaultProperty : defaultProperties) {
				defaultQNames.add(QName.createQName(defaultProperty));
			}
		}
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setDefaultProperties(List<String> defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	public void setProtectTemplatesInUse(boolean protectTemplatesInUse) {
		this.protectTemplatesInUse = protectTemplatesInUse;
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		NodeRef targetRef = childAssocRef.getChildRef();
		
		if (ContentModel.TYPE_CONTENT.equals(nodeService.getType(targetRef))) {
			nodeService.setType(targetRef, CONTENT_TYPE);
		}

		if (!CONTENT_TYPE.equals(nodeService.getType(targetRef))) {
			return;
		}
		
		logger.debug("childAssocRef:" + childAssocRef +" nodeIdchild:" +  childAssocRef.getChildRef().getId());
		

		NodeRef parentRef = childAssocRef.getParentRef();

		if (nodeService.hasAspect(parentRef, ASPECT_TYPE)) {

			List<AssociationRef> templates = nodeService.getTargetAssocs(
					parentRef, ASPECT_ASSOC);

			if (templates.size() < 1) {

				logger.error("metadataPresettingPolicy for folder(" + parentRef
						+ ") failed: there's no template specified .");
				return;
			}

			NodeRef sourceRef = templates.get(0).getTargetRef();

			if (!nodeService.exists(sourceRef)) {
				logger.error("metadataPresettingPolicy for folder(" + parentRef
						+ ") failed: template (" + sourceRef
						+ ") doesn't exist.");
				return;
			}

			@SuppressWarnings("unchecked")
			List<QName> props = (List<QName>) nodeService.getProperty(
					parentRef, ASPECT_PROP);

			if (props == null || props.size() < 1) {
				props = defaultQNames;
			}

			for (QName prop : props) {

				Serializable value = nodeService.getProperty(sourceRef, prop);

				if (value != null) {
					nodeService.setProperty(targetRef, prop, value);
				}

			}
		}

	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {

		if (protectTemplatesInUse
				&& nodeService.getSourceAssocs(nodeRef, ASPECT_ASSOC).size() > 0) {

			throw new NodeLockedException(nodeRef);

		}

	}

}
