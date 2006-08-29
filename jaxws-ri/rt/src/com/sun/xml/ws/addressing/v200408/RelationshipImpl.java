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

package com.sun.xml.ws.addressing.v200408;

import javax.xml.bind.annotation.XmlAttribute;

import com.sun.xml.ws.addressing.model.Relationship;
import static com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants.WSA_NAMESPACE_NAME;


/**
 * @author Arun Gupta
 */
public class RelationshipImpl extends Relationship {
    public RelationshipImpl() {
    }

    public RelationshipImpl(String id) {
        super(id);
    }

    public RelationshipImpl(String id, String type) {
        super(id, type);
    }

    @Override
    @XmlAttribute(name="RelationshipType", namespace= WSA_NAMESPACE_NAME)
    public String getType() {
        return super.getType();
    }

    @Override
    public void setType(String type) {
        super.setType(type);
    }
}
