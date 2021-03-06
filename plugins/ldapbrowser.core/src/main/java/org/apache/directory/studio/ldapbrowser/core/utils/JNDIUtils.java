/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */

package org.apache.directory.studio.ldapbrowser.core.utils;


import org.apache.directory.api.ldap.model.exception.LdapOperationException;


/**
 * Utility class for JNDI specific stuff.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class JNDIUtils
{
    /**
     * Gets the LDAP status code from the exception.
     * 
     * @param exception the exception
     * 
     * @return the LDAP status code, -1 if none
     */
    public static int getLdapStatusCode( Exception exception )
    {
        int ldapStatusCode = -1;

        if ( exception instanceof LdapOperationException )
        {
            LdapOperationException loe = ( LdapOperationException ) exception;
            loe.getResultCode().getValue();
        }

        // get LDAP status code
        // [LDAP: error code 21 - telephoneNumber: value #0 invalid per syntax]
        String message = exception.getMessage();
        if ( message != null && message.startsWith( "[LDAP: error code " ) ) { //$NON-NLS-1$
            int begin = "[LDAP: error code ".length(); //$NON-NLS-1$
            int end = begin + 2;
            try
            {
                ldapStatusCode = Integer.parseInt( message.substring( begin, end ).trim() );
            }
            catch ( NumberFormatException nfe )
            {
            }
        }

        return ldapStatusCode;
    }

}
