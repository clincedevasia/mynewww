package com.statoil.reinvent.workflow.mailinglist;

import java.util.Map;

public interface BrandMasterFacade {
    void distribute(Map<String, String> emailParameters) throws Exception;
    void signup(Map<String, String> formParameters) throws Exception;
}
