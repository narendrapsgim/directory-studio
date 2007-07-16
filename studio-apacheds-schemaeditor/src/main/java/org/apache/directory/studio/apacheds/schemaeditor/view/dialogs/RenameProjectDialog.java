package org.apache.directory.studio.apacheds.schemaeditor.view.dialogs;


import org.apache.directory.studio.apacheds.schemaeditor.Activator;
import org.apache.directory.studio.apacheds.schemaeditor.controller.ProjectsHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


public class RenameProjectDialog extends Dialog
{
    /** The original name*/
    private String originalName;

    /** The new name */
    private String newName;

    /** The ProjectsHandler */
    private ProjectsHandler projectsHandler;

    // UI Fields
    private Text newNameText;
    private Composite errorComposite;
    private Image errorImage;
    private Label errorLabel;
    private Button okButton;


    /**
     * Creates a new instance of RenameProjectDialog.
     *
     * @param originalName
     *      the original name of the project
     */
    public RenameProjectDialog( String originalName )
    {
        super( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
        this.originalName = originalName;
        projectsHandler = Activator.getDefault().getProjectsHandler();

    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( "Rename Schema Project" );
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NONE );
        composite.setLayout( new GridLayout( 2, false ) );
        composite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

        // New Name
        Label newNameLabel = new Label( composite, SWT.NONE );
        newNameLabel.setText( "New name:" );
        newNameText = new Text( composite, SWT.BORDER );
        newNameText.setLayoutData( new GridData( GridData.FILL, SWT.NONE, true, false ) );
        newNameText.setText( originalName );
        newNameText.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                newName = newNameText.getText();

                if ( !newName.equals( originalName ) )
                {
                    if ( projectsHandler.isProjectNameAlreadyTaken( newName ) )
                    {
                        okButton.setEnabled( false );
                        errorComposite.setVisible( true );
                        return;
                    }
                }

                // Default
                okButton.setEnabled( true );
                errorComposite.setVisible( false );
            }
        } );

        // Error Composite
        errorComposite = new Composite( composite, SWT.NONE );
        errorComposite.setLayout( new GridLayout( 2, false ) );
        errorComposite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true, 2, 1 ) );
        errorComposite.setVisible( false );

        // Error Image
        errorImage = PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK );
        Label label = new Label( errorComposite, SWT.NONE );
        label.setImage( errorImage );
        label.setSize( 16, 16 );

        // Error Label
        errorLabel = new Label( errorComposite, SWT.NONE );
        errorLabel.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
        errorLabel.setText( "A project with the same name already exists." );

        newNameText.setFocus();

        return composite;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
        okButton = createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
    }


    /**
     * Returns the new name.
     *
     * @return
     *      the new name
     */
    public String getNewName()
    {
        return newName;
    }
}
