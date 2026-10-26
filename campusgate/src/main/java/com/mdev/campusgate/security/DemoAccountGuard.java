package com.mdev.campusgate.security;

import com.mdev.campusgate.configuration.DemoProperties;
import com.mdev.campusgate.exception.BusinessRuleException;
import com.mdev.campusgate.model.UserSec;
import org.springframework.stereotype.Component;

@Component
public class DemoAccountGuard {
    public static final String DEMO_DOMAIN = "campusgate.demo";

    private final DemoProperties demoProperties;

    public DemoAccountGuard(DemoProperties demoProperties) {
        this.demoProperties = demoProperties;
    }

    public boolean isProtected(UserSec user) {
        return demoProperties.enabled() && user.getEmail().endsWith("@" + DEMO_DOMAIN);
    }

    public void assertNotProtected(UserSec user) {
        if (isProtected(user)) {
            throw new BusinessRuleException("Demo accounts cannot be modified");
        }
    }
}
