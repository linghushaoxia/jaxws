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

package fromjava.wsa.custom_action.client;

import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Binding;
import javax.xml.ws.soap.AddressingFeature;

import junit.framework.TestCase;

/**
 * @author Rama Pulavarthi
 */
public class AddNumbersTest extends TestCase {
    public AddNumbersTest(String name) {
        super(name);
    }

    private AddNumbers createStub() throws Exception {
        return new AddNumbersService().getAddNumbersPort(new AddressingFeature());
    }

    public void testDefaultActions() throws Exception {
        int result = createStub().addNumbersNoAction(10, 10);
        assertEquals(20, result);
    }

    public void testEmptyActions() throws Exception {
        int result = createStub().addNumbersEmptyAction(10, 10);
        assertEquals(20, result);
    }

    public void testExplicitActions() throws Exception {
        AddNumbers port = createStub();
        ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.com/input");
        int result = port.addNumbers(10, 10);
        assertEquals(20, result);
    }

    public void testExplicitActions2() throws Exception {
        AddNumbers port = createStub();
        ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.com/input2");
        int result = port.addNumbers2(10, 10);
        assertEquals(20, result);
    }

    public void testDefaultOutputActionWithInputSpecified() throws Exception {
        AddNumbers port = createStub();
        ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.com/input3");
        int result = port.addNumbers3(10, 10);
        assertEquals(20, result);
    }

    public void testOneFault() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput1");
            port.addNumbersFault1(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testTwoFaults_ExplicitAddNumbers() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput2");
            port.addNumbersFault2(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testTwoFaults_ExplicitTooBigNumbers() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput2");
            port.addNumbersFault2(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testTwoFaults_OnlyAddNumbersSpecified_AddNumbers() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput3");
            port.addNumbersFault3(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testTwoFaults_OnlyAddNumbersSpecified_TooBigNumbers() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput3");
            port.addNumbersFault3(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_OnlyAddNumbersSpecified_AddNumbers() throws Exception {
        try {
            createStub().addNumbersFault4(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_OnlyAddNumbersSpecified_TooBigNumbers() throws Exception {
        try {
            createStub().addNumbersFault4(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_OnlyTooBigNumbersSpecified_AddNumbers() throws Exception {
        try {
            createStub().addNumbersFault5(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_OnlyTooBigNumbersSpecified_TooBigNumbers() throws Exception {
        try {
            createStub().addNumbersFault5(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_BothSpecified_AddNumbers() throws Exception {
        try {
            createStub().addNumbersFault6(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_BothSpecified_TooBigNumbers() throws Exception {
        try {
            createStub().addNumbersFault6(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_BothEmptyString_AddNumbers() throws Exception {
        try {
            createStub().addNumbersFault7(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_BothEmptyString_TooBigNumbers() throws Exception {
        try {
            createStub().addNumbersFault7(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }
}
