package io.track4j.beans.conditions;

import io.track4j.annotation.conditions.ConditionalOnTrack4jInternalEnabled;
import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.properties.Track4jProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ConditionalOnTrack4jInternalCallEnabled implements Condition {
    private final Track4jProperties track4jProperties;

    public ConditionalOnTrack4jInternalCallEnabled() {
        Track4jServiceManager.initialize();
        this.track4jProperties = Track4jServiceManager.getInstance().getProperties();
    }

    @Override
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
        if (md.isAnnotated(ConditionalOnTrack4jInternalEnabled.class.getName())) {
            return track4jProperties.isEnabled() && track4jProperties.isInternalCallTrackingEnabled();
        }
        return false;
    }
}