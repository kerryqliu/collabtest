package org.example.collabtest.health;

import com.codahale.metrics.health.HealthCheck;

public class testHealthCheck extends HealthCheck {
    private final String template;

    public testHealthCheck(String template) {
        this.template = template;
    }

    @Override
    protected Result check() throws Exception {
        final String saying = String.format(template, "TEST");

        if (!saying.contains("TEST")) {
            return Result.unhealthy("template does not contain a name!");
        }

        return Result.healthy();
    }
}