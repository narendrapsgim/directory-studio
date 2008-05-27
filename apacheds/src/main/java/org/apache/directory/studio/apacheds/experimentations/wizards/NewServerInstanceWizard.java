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
package org.apache.directory.studio.apacheds.experimentations.wizards;


import org.apache.directory.studio.apacheds.experimentations.ApacheDsPluginUtils;
import org.apache.directory.studio.apacheds.experimentations.model.ServerInstance;
import org.apache.directory.studio.apacheds.experimentations.model.ServersHandler;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


/**
 * This class implements the new server instance wizard.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class NewServerInstanceWizard extends Wizard implements INewWizard
{
    /** The wizard page */
    private NewServerInstanceWizardPage page;


    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    public void addPages()
    {
        page = new NewServerInstanceWizardPage();
        addPage( page );
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        // Creating the new server instance
        ServerInstance serverInstance = new ServerInstance( page.getServerInstanceName() );

        // Adding the new server instance to the servers handler
        ServersHandler.getDefault().addServerInstance( serverInstance );

        // Creating the complete folder structure for the new server instance
        ApacheDsPluginUtils.createNewInstanceFolder( serverInstance.getId() );

        return true;
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        setNeedsProgressMonitor( false );
    }
}
