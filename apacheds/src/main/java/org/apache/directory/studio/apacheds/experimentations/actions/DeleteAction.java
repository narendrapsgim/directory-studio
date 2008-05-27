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
package org.apache.directory.studio.apacheds.experimentations.actions;


import org.apache.directory.studio.apacheds.experimentations.ApacheDsPluginConstants;
import org.apache.directory.studio.apacheds.experimentations.dialogs.DeleteServerDialog;
import org.apache.directory.studio.apacheds.experimentations.model.ServerInstance;
import org.apache.directory.studio.apacheds.experimentations.model.ServersHandler;
import org.apache.directory.studio.apacheds.experimentations.views.ServersView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;


/**
 * This class implements the delete action for a server instance.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class DeleteAction extends Action implements IWorkbenchWindowActionDelegate
{
    /** The associated view */
    private ServersView view;


    /**
     * Creates a new instance of DeleteAction.
     * 
     * @param view
     *      the associated view
     */
    public DeleteAction( ServersView view )
    {
        super( "Delete" );
        this.view = view;
        setToolTipText( "Delete" );
        setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
            ISharedImages.IMG_TOOL_DELETE ) );
        setId( ApacheDsPluginConstants.ACTION_DELETE );
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run()
    {
        // What we get from the TableViewer is a StructuredSelection
        StructuredSelection selection = ( StructuredSelection ) view.getViewer().getSelection();

        // Here's the real object
        ServerInstance serverInstance = ( ServerInstance ) selection.getFirstElement();

        // Asking for confirmation
        DeleteServerDialog dsd = new DeleteServerDialog( view.getSite().getShell(), serverInstance );
        if ( dsd.open() == DeleteServerDialog.OK )
        {
            // Removing the server instance
            ServersHandler.getDefault().removeServerInstance( serverInstance );
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run( IAction action )
    {
        run();
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose()
    {
        // Nothing to do
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init( IWorkbenchWindow window )
    {
        // Nothing to do
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged( IAction action, ISelection selection )
    {
        // Nothing to do
    }
}
