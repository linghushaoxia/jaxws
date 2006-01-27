/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.model;

import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.messaging.saaj.soap.SOAPVersionMismatchException;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.*;
import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.util.MessageInfoUtil;
//import com.sun.xml.ws.api.model.JavaMethod;
//import com.sun.xml.ws.api.model.Parameter;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
//import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.api.model.Mode;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.soap.SOAPFault;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Creates SOAP specific RuntimeModel
 *
 * @author Vivek Pandey
 */
public class SOAPSEIModel extends AbstractSEIModelImpl {

    protected void createDecoderInfo() {
        for (JavaMethodImpl m : getJavaMethods()) {
            if(m.isAsync())
                continue;
            com.sun.xml.ws.api.model.soap.SOAPBinding binding = (com.sun.xml.ws.api.model.soap.SOAPBinding) m.getBinding();
            setDecoderInfo(m.getRequestParameters(), binding, Mode.IN);
            setDecoderInfo(m.getResponseParameters(), binding, Mode.OUT);
            for(CheckedExceptionImpl ce:m.getCheckedExceptions()){
                JAXBBridgeInfo bi = new JAXBBridgeInfo(getBridge(ce.getDetailType()));
                addDecoderInfo(ce.getDetailType().tagName, bi);
            }
        }

    }

    private void setDecoderInfo(List<ParameterImpl> params, com.sun.xml.ws.api.model.soap.SOAPBinding binding, Mode mode){
        for (ParameterImpl param : params) {
            ParameterBinding paramBinding = (mode == Mode.IN)?param.getInBinding():param.getOutBinding();
            if (paramBinding.isBody() && binding.isRpcLit()) {
                RpcLitPayload payload = new RpcLitPayload(param.getName());
                WrapperParameter wp = (WrapperParameter) param;
                for (ParameterImpl p : wp.getWrapperChildren()) {
                    if(p.getBinding().isUnbound())
                        continue;
                    JAXBBridgeInfo bi = new JAXBBridgeInfo(p.getBridge(),
                        null);
                    payload.addParameter(bi);
                }
                addDecoderInfo(param.getName(), payload);
            } else {
                JAXBBridgeInfo bi;
                if (param instanceof WrapperParameter) {
                    WrapperParameter wp = (WrapperParameter)param;
                     bi = new JAXBBridgeInfo(wp.getWrapperBridge(),
                        null);
                }
                else
                    bi = new JAXBBridgeInfo(getBridge(param.getBridge().getTypeReference()),
                        null);
                addDecoderInfo(param.getName(), bi);
            }
        }
    }

    /*
     * @see RuntimeModel#populateMaps()
     */
    @Override
    protected void populateMaps() {
        int emptyBodyCount = 0;
        for(JavaMethodImpl jm : getJavaMethods()){
            put(jm.getMethod(), jm);
            boolean bodyFound = false;
            for(ParameterImpl p:jm.getRequestParameters()){
                ParameterBinding binding = p.getBinding();
                if(binding.isBody()){
                    put(p.getName(), jm);
                    bodyFound = true;
                }
            }            
            if(!bodyFound){
                put(emptyBodyName, jm);
//                System.out.println("added empty body for: "+jm.getMethod().getName());
                emptyBodyCount++;
            }
        }
        if(emptyBodyCount > 1){
            //TODO throw exception
//            System.out.println("Error: Unqiue signature violation - more than 1 empty body!");
        }
    }


    /* 
     * @see RuntimeModel#fillTypes(JavaMethod, List)
     */
    @Override
    protected void fillTypes(JavaMethodImpl m, List<TypeReference> types) {
        if(m.getBinding() == null) {
            //TODO throws exception
            System.out.println("Error: Wrong Binding!");
            return;
        }
        if(m.getBinding().isDocLit()){
            super.fillTypes(m, types);
            return;
        }
        
        //else is rpclit
        addTypes(m.getRequestParameters(), types, Mode.IN);
        addTypes(m.getResponseParameters(), types, Mode.OUT);
    }
        
    /**
     * @param params
     * @param types
     * @param mode
     */
    private void addTypes(List<ParameterImpl> params, List<TypeReference> types, Mode mode) {
        for(ParameterImpl p:params){
            ParameterBinding binding = (mode == Mode.IN)?p.getInBinding():p.getOutBinding();
            if(!p.isWrapperStyle()){
                types.add(p.getBridge().getTypeReference());
            }else if(binding.isBody()){
                List<ParameterImpl> wcParams = ((WrapperParameter)p).getWrapperChildren();
                for(ParameterImpl wc:wcParams){
                    types.add(wc.getBridge().getTypeReference());
                }
            }
        }
    }


    public Set<QName> getKnownHeaders() {
        Set<QName> headers = new HashSet<QName>();
        for (JavaMethodImpl method : getJavaMethods()) {
            // fill in request headers
            Iterator<ParameterImpl> params = method.getRequestParameters().iterator();
            fillHeaders(params, headers, Mode.IN);

            // fill in response headers
            params = method.getResponseParameters().iterator();
            fillHeaders(params, headers, Mode.OUT);
        }
        return headers;
    }

    /**
     * @param params
     * @param headers
     */
    private void fillHeaders(Iterator<ParameterImpl> params, Set<QName> headers, Mode mode) {
        while (params.hasNext()) {
            ParameterImpl param = params.next();
            ParameterBinding binding = (mode == Mode.IN)?param.getInBinding():param.getOutBinding();
            QName name = param.getName();
            if (binding.isHeader() && !headers.contains(name)) {
                headers.add(name);
            }
        }
    }
    
    /**
     * Called by server  
     * 
     * @param obj
     * @param actor
     * @param detail
     * @param internalMsg
     * @return the InternalMessage for a fault
     */
    public static InternalMessage createFaultInBody(Object obj, String actor,
            Object detail, InternalMessage internalMsg) {
        SOAPFaultInfo faultInfo;
        if (obj instanceof SOAPFaultInfo) {
            faultInfo = (SOAPFaultInfo)obj;
        } else if (obj instanceof ServerRtException) {
            Throwable cause = ((ServerRtException)obj).getCause();
            Throwable th = (cause == null) ? (ServerRtException)obj : cause;
            faultInfo = createSOAPFaultInfo(th, actor, detail);

        } else if (obj instanceof SOAPFaultException) {
            SOAPFaultException e = (SOAPFaultException)obj;
            faultInfo = new SOAPFaultInfo(e.getFault());
        } else if (obj instanceof SOAPVersionMismatchException) {
            QName faultCode = SOAPConstants.FAULT_CODE_VERSION_MISMATCH;
            String faultString = "SOAP envelope version mismatch";
            faultInfo = new SOAPFaultInfo(faultString, faultCode, actor, null, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING);
        } else if (obj instanceof Exception) {
            faultInfo = createSOAPFaultInfo((Exception)obj, actor, detail);
        } else {
            QName faultCode = SOAPConstants.FAULT_CODE_SERVER;
            String faultString = "Unknown fault type:"+obj.getClass();
            faultInfo = new SOAPFaultInfo(faultString, faultCode, actor, null,javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING);
        }

        if (internalMsg == null) {
            internalMsg = new InternalMessage();
        }

        BodyBlock bodyBlock = internalMsg.getBody();
        if (bodyBlock == null) {
            bodyBlock = new BodyBlock(faultInfo);
            internalMsg.setBody(bodyBlock);
        } else {
            bodyBlock.setFaultInfo(faultInfo);
        }

        return internalMsg;
    }

    public static InternalMessage createSOAP12FaultInBody(Object obj, String role, String node, Object detail, InternalMessage im) {
        SOAP12FaultInfo faultInfo;
        if (obj instanceof SOAP12FaultInfo) {
            faultInfo = (SOAP12FaultInfo)obj;
        } else if (obj instanceof ServerRtException) {
            Throwable cause = ((ServerRtException)obj).getCause();
            Throwable th = (cause == null) ? (ServerRtException)obj : cause;
            faultInfo = createSOAP12FaultInfo(th, role, node, detail);

        } else if (obj instanceof SOAPFaultException) {
            SOAPFaultException e = (SOAPFaultException)obj;
            faultInfo = new SOAP12FaultInfo(e.getFault());
        } else if (obj instanceof SOAPVersionMismatchException) {
            String faultString = "SOAP envelope version mismatch";
            FaultCode code = new FaultCode(FaultCodeEnum.VersionMismatch, (FaultSubcode) null);
            FaultReason reason = new FaultReason(new FaultReasonText(faultString, Locale.getDefault()));
            faultInfo = new SOAP12FaultInfo(code, reason, null, null, null);
        } else if (obj instanceof Exception) {
            faultInfo = createSOAP12FaultInfo((Exception)obj, role, node, detail);
        } else {
            String faultString = "Unknown fault type:"+obj.getClass();
            FaultCode code = new FaultCode(FaultCodeEnum.Receiver, (FaultSubcode) null);
            FaultReason reason = new FaultReason(new FaultReasonText(faultString, Locale.getDefault()));
            faultInfo = new SOAP12FaultInfo(code, reason, null, null, null);
        }
        if (im == null) {
            im = new InternalMessage();
        }
        BodyBlock bodyBlock = im.getBody();
        if (bodyBlock == null) {
            bodyBlock = new BodyBlock(faultInfo);
            im.setBody(bodyBlock);
        } else {
            bodyBlock.setValue(faultInfo);
        }
        return im;
    }
    
    // currently adds not understood header blocks if there are any
    public static void addHeaders(InternalMessage message, MessageInfo mi) {
        Set<HeaderBlock> notUnderstoodHeaders =
            MessageInfoUtil.getNotUnderstoodHeaders(mi);
        if (notUnderstoodHeaders != null) {
            for (HeaderBlock block : notUnderstoodHeaders) {
                message.addHeader(block);
            }
        }
    }

    private static SOAP12FaultInfo createSOAP12FaultInfo(Throwable e, String role, String node, Object detail) {
        SOAPFaultException soapFaultException = null;
        FaultCode code = null;
        FaultReason reason = null;
        String faultRole = null;
        String faultNode = null;
        Throwable cause = e.getCause();
        if (e instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException)e;
        } else if (cause != null && cause instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException)e.getCause();
        }
        if (soapFaultException != null) {
            SOAPFault soapFault =  soapFaultException.getFault();
            code = new FaultCode(FaultCodeEnum.get(soapFault.getFaultCodeAsQName()), (FaultSubcode) null);
            reason = new FaultReason(new FaultReasonText(soapFault.getFaultString(),
                    soapFault.getFaultStringLocale()));
            faultRole = soapFault.getFaultRole();
            if(faultRole == null)
                faultRole = role;
            faultNode = soapFault.getFaultNode();
            if(faultNode == null)
                faultNode = node;
        }

        if (code == null || ((code != null) && code.getValue() == null)) {
            code = new FaultCode(FaultCodeEnum.Receiver, (FaultSubcode) null);
        }

        if(reason == null){
            String faultString = e.getMessage();
            if (faultString == null) {
                faultString = e.toString();
            }

            reason = new FaultReason(new FaultReasonText(faultString, Locale.getDefault()));
        }

        if ((detail == null) && (soapFaultException != null)) {
            detail = soapFaultException.getFault().getDetail();
        }

        return new SOAP12FaultInfo(code, reason, faultRole, faultNode, detail);
    }

    //TODO: createSOAP12HeaderFault()


    /**
     * @param obj
     * @param actor
     * @param detailBlock
     * @param internalMsg
     * @return the <code>InteralMessage</code> for a HeaderFault
     */
    public static InternalMessage createHeaderFault(Object obj, String actor, JAXBBridgeInfo detailBlock, InternalMessage internalMsg){
        //its headerfault so, create body fault with no detail. detail object goes as a header block
        internalMsg = createFaultInBody(obj, actor, null, internalMsg);
        HeaderBlock hdrBlock = new HeaderBlock(detailBlock);
        internalMsg.addHeader(hdrBlock);
        return internalMsg;
    }

    /**
     *
     * @param e
     * @param actor
     * @param detail
     */
    private static SOAPFaultInfo createSOAPFaultInfo(Throwable e, String actor,
            Object detail) {
//        e.printStackTrace();
        SOAPFaultException soapFaultException = null;
        QName faultCode = null;
        String faultString = null;
        String faultActor = null;
        Throwable cause = e.getCause();
        if (e instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException)e;
        } else if (cause != null && cause instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException)e.getCause();
        }
        if (soapFaultException != null) {
            faultCode = soapFaultException.getFault().getFaultCodeAsQName();
            faultString = soapFaultException.getFault().getFaultString();
            faultActor = soapFaultException.getFault().getFaultActor();
        }
        
        if (faultCode == null) {
            faultCode = SOAPConstants.FAULT_CODE_SERVER;
        }
        
        if (faultString == null) {
            faultString = e.getMessage();
            if (faultString == null) {
                faultString = e.toString(); 
            }
        }

        if (faultActor == null) {
            faultActor = actor;   
        }

        if (detail == null && soapFaultException != null) {
            detail = soapFaultException.getFault().getDetail();
        }

        return new SOAPFaultInfo(faultString, faultCode, faultActor, detail, javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING);
    }

}
