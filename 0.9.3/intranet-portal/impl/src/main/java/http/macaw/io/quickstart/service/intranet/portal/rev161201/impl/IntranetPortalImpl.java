//
//  This file was auto-generated by Macaw tools 0.3.0-SNAPSHOT version built on Tue, 14 Feb 2017 14:52:15 +0530 
//
/**
 * Copyright © 2015-2016, Macaw Software Inc.
 * All rights reserved.
 * 
 * This software and related documentation are provided under a
 * license agreement containing restrictions on use and
 * disclosure and are protected by intellectual property laws.
 * Except as expressly permitted in your license agreement or
 * allowed by law, you may not use, copy, reproduce, translate,
 * broadcast, modify, license, transmit, distribute, exhibit,
 * perform, publish, or display any part, in any form, or by
 * any means. Reverse engineering, disassembly, or
 * decompilation of this software, unless required by law for
 * interoperability, is prohibited.
 * 
 * The information contained herein is subject to change
 * without notice and is not warranted to be error-free. If you
 * find any errors, please report them to us in writing.
 */
package http.macaw.io.quickstart.service.intranet.portal.rev161201.impl;

import java.util.HashMap;
import java.util.Map;

import http.macaw.io.quickstart.service.employee.rev161201.Employee;

public class IntranetPortalImpl implements com.cfx.service.api.Service, http.macaw.io.quickstart.service.intranet.portal.rev161201.IntranetPortal {

    private final static String EMPLOYEE_ADDED_NOTIFICATION_ID = "NEW_EMPLOYEE_ADDED";
    private final static String EMPLOYEE_RELIEVED_NOTIFICATION_ID = "EMPLOYEE_RELIEVED";

    private final Map<String, String> userDetails = new HashMap<String, String>();

    @Override
    public void initialize(com.cfx.service.api.config.Configuration config) throws com.cfx.service.api.ServiceException {
    }

    @Override
    public void start(com.cfx.service.api.StartContext startContext) throws com.cfx.service.api.ServiceException {
    }

    @Override
    public void stop(com.cfx.service.api.StopContext stopContext) throws com.cfx.service.api.ServiceException {
    }

    public void onNotification(com.cfx.service.api.notification.Notification notification) {
        switch (notification.getIdentifier().getNotificationId()) {
        case EMPLOYEE_ADDED_NOTIFICATION_ID:
            System.out.println("proccessing notification for newly added employee to the organisation.");
            Employee addedEmployee = (Employee) notification.getContent();
            userDetails.put(addedEmployee.getEmail(), addedEmployee.getPassword());
            break;
        case EMPLOYEE_RELIEVED_NOTIFICATION_ID:
            System.out.println("proccessing notification for employee relieved.");
            String deletedUserEmailId = (String) notification.getContent();
            userDetails.remove(deletedUserEmailId);
            break;
        default:
            throw new IllegalArgumentException("Unknown notification received :" + notification.getIdentifier().getNotificationId());
        }
    }

    @Override
    public void login(String email, String password) {
        if (!userDetails.containsKey(email) || !userDetails.get(email).equals(password))
            throw new RuntimeException("Invalid email/password");

        System.out.println("Loggedin successfully to the portal.");
    }
}
