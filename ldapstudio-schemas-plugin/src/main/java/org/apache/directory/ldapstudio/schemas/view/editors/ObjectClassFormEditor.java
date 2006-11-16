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

package org.apache.directory.ldapstudio.schemas.view.editors;


import org.apache.directory.ldapstudio.schemas.controller.Application;
import org.apache.directory.ldapstudio.schemas.model.ObjectClass;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;


/**
 * This class is the ObjectClass Editor main class
 */
public class ObjectClassFormEditor extends FormEditor
{
    private static Logger logger = Logger.getLogger( ObjectClassFormEditor.class );
    public static final String ID = Application.PLUGIN_ID + ".view.objectclassformeditor"; //$NON-NLS-1$
    private ObjectClassFormEditorOverviewPage overview;
    private ObjectClassFormEditorSourceCodePage sourceCode;
    private boolean dirty = false;
    private ObjectClass objectClass;


    /**
     * Default constructor
     */
    public ObjectClassFormEditor()
    {
        super();
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException
    {
        setSite( site );
        setInput( input );
        setPartName( input.getName() );

        objectClass = ( ( ObjectClassFormEditorInput ) getEditorInput() ).getObjectClass();
        objectClass.setEditor( this );
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormEditor#dispose()
     */
    @Override
    public void dispose()
    {
        objectClass.removeEditor( this );
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    @Override
    protected void addPages()
    {
        try
        {
            overview = new ObjectClassFormEditorOverviewPage( this,
                "overview", Messages.getString( "ObjectClassFormEditor.Overview" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            addPage( overview );
            sourceCode = new ObjectClassFormEditorSourceCodePage( this,
                "sourceCode", Messages.getString( "ObjectClassFormEditor.Source_Code" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            addPage( sourceCode );
        }
        catch ( PartInitException e )
        {
            logger.debug( "error when adding pages" ); //$NON-NLS-1$
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave( IProgressMonitor monitor )
    {
        // Save is delegate to the page (that holds the object class itself)
        overview.doSave( monitor );
        // We reload the name in case of it has changed
        setPartName( getEditorInput().getName() );
        sourceCode.refresh();
        if ( !monitor.isCanceled() )
        {
            setDirty( false );
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs()
    {
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed()
    {
        return false;
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormEditor#isDirty()
     */
    @Override
    public boolean isDirty()
    {
        return this.dirty;
    }


    /**
     * Sets the dirty state of the editor
     * 
     * @param dirty
     *            the dirty state
     */
    public void setDirty( boolean dirty )
    {
        this.dirty = dirty;
        editorDirtyStateChanged();
    }
}
