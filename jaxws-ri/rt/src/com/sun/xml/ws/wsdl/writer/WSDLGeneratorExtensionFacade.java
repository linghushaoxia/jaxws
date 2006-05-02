/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.wsdl.writer;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;

import java.lang.reflect.Method;

/**
 * {@link WSDLGeneratorExtension} that delegates to
 * multiple {@link WSDLGeneratorExtension}s.
 *
 * <p>
 * This simplifies {@link WSDLGenerator} since it now
 * only needs to work with one {@link WSDLGeneratorExtension}.
 *
 *
 * @author Doug Kohlert
 */
final class WSDLGeneratorExtensionFacade extends WSDLGeneratorExtension {
    private final WSDLGeneratorExtension[] extensions;

    WSDLGeneratorExtensionFacade(WSDLGeneratorExtension... extensions) {
        assert extensions!=null;
        this.extensions = extensions;
    }

    public void start(TypedXmlWriter root, SEIModel model, WSBinding binding, Container container) {
        for (WSDLGeneratorExtension e : extensions)
            e.start(root, model, binding, container);
    }

    public void addDefinitionsExtension(TypedXmlWriter definitions) {
        for (WSDLGeneratorExtension e : extensions)
            e.addDefinitionsExtension(definitions);
    }

    public void addServiceExtension(TypedXmlWriter service) {
        for (WSDLGeneratorExtension e : extensions)
            e.addServiceExtension(service);
    }

    public void addPortExtension(TypedXmlWriter port) {
        for (WSDLGeneratorExtension e : extensions)
            e.addPortExtension(port);
    }

    public void addPortTypeExtension(TypedXmlWriter portType) {
        for (WSDLGeneratorExtension e : extensions)
            e.addPortTypeExtension(portType);
    }

    public void addBindingExtension(TypedXmlWriter binding) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingExtension(binding);
    }

    public void addOperationExtension(TypedXmlWriter operation, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOperationExtension(operation, method);
    }

    public void addBindingOperationExtension(TypedXmlWriter operation, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingOperationExtension(operation, method);
    }

    public void addInputMessageExtension(TypedXmlWriter message, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addInputMessageExtension(message, method);
    }

    public void addOutputMessageExtension(TypedXmlWriter message, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOutputMessageExtension(message, method);
    }

    public void addOperationInputExtension(TypedXmlWriter input, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOperationInputExtension(input, method);
    }

    public void addOperationOutputExtension(TypedXmlWriter output, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOperationOutputExtension(output, method);
    }

    public void addBindingOperationInputExtension(TypedXmlWriter input, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingOperationInputExtension(input, method);
    }

    public void addBindingOperationOutputExtension(TypedXmlWriter output, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingOperationOutputExtension(output, method);
    }

    public void addBindingOperationFaultExtension(TypedXmlWriter fault, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingOperationFaultExtension(fault, method);
    }

    public void addFaultMessageExtension(TypedXmlWriter message, Method method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addFaultMessageExtension(message, method);
    }

    public void addOperationFaultExtension(TypedXmlWriter fault, Method method, CheckedException ce) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOperationFaultExtension(fault, method, ce);
    }
}
