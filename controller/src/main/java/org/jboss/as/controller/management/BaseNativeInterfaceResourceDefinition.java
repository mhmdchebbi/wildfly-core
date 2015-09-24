/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.controller.management;

import static org.jboss.as.controller.management.Capabilities.NATIVE_MANAGEMENT_CAPABILITY;
import static org.jboss.as.controller.management.Capabilities.SASL_SERVER_AUTHENTICATION_CAPABILITY;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NATIVE_INTERFACE;

import java.util.List;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.access.management.AccessConstraintDefinition;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Base class for the {@link ResourceDefinition} instances to extend from.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public abstract class BaseNativeInterfaceResourceDefinition extends SimpleResourceDefinition {

    public static final RuntimeCapability<Void> NATIVE_MANAGEMENT_RUNTIME_CAPABILITY = RuntimeCapability.Builder
            .of(NATIVE_MANAGEMENT_CAPABILITY).build();

    protected static final PathElement RESOURCE_PATH = PathElement.pathElement(MANAGEMENT_INTERFACE, NATIVE_INTERFACE);

    public static final SimpleAttributeDefinition SECURITY_REALM = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.SECURITY_REALM, ModelType.STRING, true)
        .setAlternatives(ModelDescriptionConstants.SASL_SERVER_AUTHENTICATION)
        .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, true, false))
        .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
        .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.SECURITY_REALM_REF)
        .setNullSignificant(true)
        .build();

    public static final SimpleAttributeDefinition SERVER_NAME = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.SERVER_NAME, ModelType.STRING, true)
        .setRequires(ModelDescriptionConstants.SECURITY_REALM)
        .setAllowExpression(true)
        .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, true, true))
        .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .build();

    public static final SimpleAttributeDefinition SASL_PROTOCOL = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.SASL_PROTOCOL, ModelType.STRING, true)
        .setRequires(ModelDescriptionConstants.SECURITY_REALM)
        .setAllowExpression(true)
        .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, true, true))
        .setDefaultValue(new ModelNode(ModelDescriptionConstants.REMOTE))
        .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .build();

    public static final SimpleAttributeDefinition SASL_SERVER_AUTHENTICATION = new SimpleAttributeDefinitionBuilder(ModelDescriptionConstants.SASL_SERVER_AUTHENTICATION, ModelType.STRING, true)
        .setAlternatives(ModelDescriptionConstants.SECURITY_REALM)
        .setMinSize(1)
        .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
        .setCapabilityReference(SASL_SERVER_AUTHENTICATION_CAPABILITY, NATIVE_MANAGEMENT_RUNTIME_CAPABILITY)
        .build();

    protected static final AttributeDefinition[] COMMON_ATTRIBUTES = new AttributeDefinition[] { SECURITY_REALM, SERVER_NAME, SASL_PROTOCOL, SASL_SERVER_AUTHENTICATION };

    private final List<AccessConstraintDefinition> accessConstraints;

    protected BaseNativeInterfaceResourceDefinition(Parameters parameters) {
        super(parameters);
        this.accessConstraints = SensitiveTargetAccessConstraintDefinition.MANAGEMENT_INTERFACES.wrapAsList();
        setDeprecated(ModelVersion.create(1, 7));
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        AttributeDefinition[] attributeDefinitions = getAttributeDefinitions();
        OperationStepHandler writeHandler = new ManagementWriteAttributeHandler(attributeDefinitions);
        for (AttributeDefinition attr : attributeDefinitions) {
            resourceRegistration.registerReadWriteAttribute(attr, null, writeHandler);
        }
    }

    @Override
    public List<AccessConstraintDefinition> getAccessConstraints() {
        return accessConstraints;
    }

    @Override
    public void registerCapabilities(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerCapability(NATIVE_MANAGEMENT_RUNTIME_CAPABILITY);
    }

    protected abstract AttributeDefinition[] getAttributeDefinitions();

    protected static AttributeDefinition[] combine(AttributeDefinition[] commonAttributes, AttributeDefinition... additionalAttributes) {
        AttributeDefinition[] combined = new AttributeDefinition[commonAttributes.length + additionalAttributes.length];
        System.arraycopy(commonAttributes, 0, combined, 0, commonAttributes.length);
        System.arraycopy(additionalAttributes, 0, combined, commonAttributes.length, additionalAttributes.length);

        return combined;
    }
}
