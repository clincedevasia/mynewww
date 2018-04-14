/*******************************************************************************
 * Copyright (c) 2015 Statoil.
 *
 * All rights reserved. Do not distribute any of these files without prior consent from Statoil.
 *
 * Contributors:
 *     Adobe
 *******************************************************************************/
/**
 * @author raskhura
 */
package com.statoil.reinvent.workflow;


import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.oak.spi.security.principal.EveryonePrincipal;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.workflow.WorkflowService;
@Component
@Service

@Properties({
	@Property(name = Constants.SERVICE_DESCRIPTION, value = "Restricting User access on pages"),
	@Property(name = Constants.SERVICE_VENDOR, value = "Adobe"),
	@Property(name = "process.label", value = "Applying user restrictions on page") })


public class UserAccessRestriction implements WorkflowProcess {
	  
	 @Reference
	    private WorkflowService workflowService;

	 @Reference
	    private ResourceResolverFactory resolverFactory;
	 
	 
	private static final String TYPE_JCR_PATH = "JCR_PATH";
	private static final String IMAGE_PATH = "/content/dam/projects/statoilsite";
	private static final String MIXIN = "rep:AccessControllable";
	private static final String REP_POLICY = "cover";
	private static final String COLON = ":";

	private static final String WORKFLOW_PKG = "/etc/workflow/packages/launches/jcr:content/vlt:definition/filter";

	/** Default log. */
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	@Override
	public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
		
		WorkflowData workflowData = item.getWorkflowData();
		
		//Copy the node into Destination
        if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
            String path = workflowData.getPayload().toString();
            try {
            	
            	Map<String, Object> param = new HashMap<String, Object>();
            	param.put(ResourceResolverFactory.SUBSERVICE, "datawrite");
            	ResourceResolver resolver = null;
            	
            	//Invoke the adaptTo method to create a Session used to create a QueryManager
                resolver = resolverFactory.getServiceResourceResolver(param);
           
            	
            	
            	//get ARGS
				String processArgs = args.get("PROCESS_ARGS", "default value");
				String[] proccesArgsVals = StringUtils.split(processArgs, ",");
              
				String assetProjectPath = StringUtils.EMPTY;
				if (null != proccesArgsVals) {
		            for (int i = 0; i < proccesArgsVals.length; i++) {
		                String nxtStrtArrFull = proccesArgsVals[i];
		                String[] assetArrSplit = nxtStrtArrFull.split(COLON, 2);
		                if (assetArrSplit.length == 2) {
		                    String site = assetArrSplit[0];
		                    if (site.equalsIgnoreCase("beta_statoil_de")) {
		                        assetProjectPath = assetArrSplit[1];

		                    }
		                }
		            }
		        }
            	
				
            	//resolver.adaptTo(Node.class);
                //Session jcrSession = session.adaptTo(Session.class); 
				Session jcrSession = resolver.adaptTo(Session.class);
                Node dstParent = (Node) jcrSession.getItem(path);
                Node src = (Node) jcrSession.getItem(assetProjectPath+"/"+REP_POLICY);
              
                //get the workflow package
                Node workflow_pkg = (Node) jcrSession.getItem(WORKFLOW_PKG);
                String WORKFLOW_PKG;
                
                //Random randomGenerator = new Random();
				//workflow_pkg.addNode("resource_0","nt:unstructured");
				
				
              //check if it has mixin, if not add the mixin
                //dstParent.addMixin("rep:AccessControllable");
                //this won't work as you can not copy the stupid ACLs
               //JcrUtil.copy(src, dstParent,null);
                
                
              //Create ACLs for new campaign assets 
				createACLs(resolver,assetProjectPath,path);
               
                    jcrSession.save();
                    if(jcrSession.isLive()){
                    	jcrSession.logout();
                    }
                   
              
            } catch (RepositoryException | LoginException e) {
                throw new WorkflowException(e.getMessage(), e);
            }
            
            
        }
	}
	
	private void createACLs(ResourceResolver resolver, String assetsPath, String newLaunchPath) throws UnsupportedRepositoryOperationException, RepositoryException{
		Session session = resolver.adaptTo(Session.class);
		 
		 AccessControlManager acm = session.getAccessControlManager();
		 
        JackrabbitAccessControlList acl1 = getACL(session, newLaunchPath); 
        
        JackrabbitAccessControlList acl2 = getACL(session, assetsPath);     

        for (AccessControlEntry e : acl2.getAccessControlEntries()) {
        	if(e.getPrincipal().getName().equalsIgnoreCase(EveryonePrincipal.NAME)){
        		acl1.addEntry(EveryonePrincipal.getInstance(), e.getPrivileges(), false);
        	} else {
               acl1.addAccessControlEntry(e.getPrincipal(), e.getPrivileges());
        	}
        	                      
           session.save();                        
        } 
        
        acm.setPolicy(newLaunchPath, acl1);
        
        session.save();
	}
	
	
	private JackrabbitAccessControlList getACL(Session session, String path) throws UnsupportedRepositoryOperationException, RepositoryException {
        AccessControlManager acMgr = session.getAccessControlManager();

        JackrabbitAccessControlList acl = null;
        AccessControlPolicyIterator app = acMgr.getApplicablePolicies(path);

        while (app.hasNext()) {
            AccessControlPolicy pol = app.nextAccessControlPolicy();

            if (pol instanceof JackrabbitAccessControlPolicy) {
                acl = (JackrabbitAccessControlList) pol;
                break;
            }
        }

        if (acl == null) {
            for (AccessControlPolicy pol: acMgr.getPolicies(path)) {
            	
                if (pol instanceof JackrabbitAccessControlPolicy) {
                    acl = (JackrabbitAccessControlList) pol;
                    break;
                }
            }
        }

        return acl;
    }
	
}


