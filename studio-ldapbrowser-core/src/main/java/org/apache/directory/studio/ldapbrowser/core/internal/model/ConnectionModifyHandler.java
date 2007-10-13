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

package org.apache.directory.studio.ldapbrowser.core.internal.model;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.directory.studio.connection.core.StudioProgressMonitor;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreConstants;
import org.apache.directory.studio.ldapbrowser.core.BrowserCoreMessages;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.model.DN;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IReferralHandler;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.URL;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.LdifEnumeration;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.container.LdifChangeAddRecord;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.container.LdifChangeDeleteRecord;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.container.LdifChangeModDnRecord;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.container.LdifChangeModifyRecord;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.container.LdifContainer;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.container.LdifContentRecord;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.container.LdifModSpec;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.container.LdifRecord;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifAttrValLine;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifChangeTypeLine;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifCommentLine;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifDeloldrdnLine;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifDnLine;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifModSpecSepLine;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifNewrdnLine;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifNewsuperiorLine;
import org.apache.directory.studio.ldapbrowser.core.model.ldif.lines.LdifSepLine;
import org.apache.directory.studio.ldapbrowser.core.utils.ModelConverter;


class ConnectionModifyHandler
{

    private BrowserConnection browserConnection;

    private ModificationLogger modificationLogger;


    ConnectionModifyHandler( BrowserConnection connection )
    {
        this.browserConnection = connection;
        this.modificationLogger = new ModificationLogger( connection );
    }


    void connectionClosed()
    {
    }


    void create( IValue[] valuesToCreate, StudioProgressMonitor monitor )
    {
        for ( int i = 0; !monitor.isCanceled() && i < valuesToCreate.length; i++ )
        {

            LdifChangeModifyRecord cmr = new LdifChangeModifyRecord( LdifDnLine.create( valuesToCreate[i]
                .getAttribute().getEntry().getDn().toString() ) );
            ModelConverter.addControls( cmr, valuesToCreate[i].getAttribute().getEntry() );
            cmr.setChangeType( LdifChangeTypeLine.createModify() );

            LdifModSpec modSpec = LdifModSpec.createAdd( valuesToCreate[i].getAttribute().getDescription() );
            if ( valuesToCreate[i].isString() )
            {
                modSpec.addAttrVal( LdifAttrValLine.create( valuesToCreate[i].getAttribute().getDescription(),
                    valuesToCreate[i].getStringValue() ) );
            }
            else
            {
                modSpec.addAttrVal( LdifAttrValLine.create( valuesToCreate[i].getAttribute().getDescription(),
                    valuesToCreate[i].getBinaryValue() ) );
            }
            modSpec.finish( LdifModSpecSepLine.create() );
            cmr.addModSpec( modSpec );
            cmr.finish( LdifSepLine.create() );

            try
            {
                this.applyModificationAndLog( cmr, monitor );
            }
            catch ( ConnectionException e )
            {
                monitor.reportError( e );
            }
        }
    }


    void modify( IValue oldValue, IValue newValue, StudioProgressMonitor monitor )
    {
        try
        {
            LdifChangeModifyRecord cmr = new LdifChangeModifyRecord( LdifDnLine.create( oldValue.getAttribute()
                .getEntry().getDn().toString() ) );
            ModelConverter.addControls( cmr, oldValue.getAttribute().getEntry() );
            cmr.setChangeType( LdifChangeTypeLine.createModify() );

            if ( oldValue.getAttribute().getValueSize() == 1 )
            {
                LdifModSpec modSpec = LdifModSpec.createReplace( oldValue.getAttribute().getDescription() );
                if ( newValue.isString() )
                {
                    modSpec.addAttrVal( LdifAttrValLine.create( oldValue.getAttribute().getDescription(), newValue
                        .getStringValue() ) );
                }
                else
                {
                    modSpec.addAttrVal( LdifAttrValLine.create( oldValue.getAttribute().getDescription(), newValue
                        .getBinaryValue() ) );
                }
                modSpec.finish( LdifModSpecSepLine.create() );
                cmr.addModSpec( modSpec );
                cmr.finish( LdifSepLine.create() );
            }
            else
            {
                LdifModSpec modSpec1 = LdifModSpec.createAdd( oldValue.getAttribute().getDescription() );
                if ( newValue.isString() )
                {
                    modSpec1.addAttrVal( LdifAttrValLine.create( oldValue.getAttribute().getDescription(), newValue
                        .getStringValue() ) );
                }
                else
                {
                    modSpec1.addAttrVal( LdifAttrValLine.create( oldValue.getAttribute().getDescription(), newValue
                        .getBinaryValue() ) );
                }
                modSpec1.finish( LdifModSpecSepLine.create() );
                cmr.addModSpec( modSpec1 );

                LdifModSpec modSpec2 = LdifModSpec.createDelete( oldValue.getAttribute().getDescription() );
                if ( oldValue.isString() )
                {
                    modSpec2.addAttrVal( LdifAttrValLine.create( oldValue.getAttribute().getDescription(), oldValue
                        .getStringValue() ) );
                }
                else
                {
                    modSpec2.addAttrVal( LdifAttrValLine.create( oldValue.getAttribute().getDescription(), oldValue
                        .getBinaryValue() ) );
                }
                modSpec2.finish( LdifModSpecSepLine.create() );
                cmr.addModSpec( modSpec2 );
                cmr.finish( LdifSepLine.create() );
            }

            this.applyModificationAndLog( cmr, monitor );

        }
        catch ( ConnectionException e )
        {
            monitor.reportError( e );
        }
    }


    void delete( IValue[] valuesToDelete, StudioProgressMonitor monitor )
    {
        try
        {
            for ( int i = 0; !monitor.isCanceled() && i < valuesToDelete.length; i++ )
            {
                LdifChangeModifyRecord cmr = new LdifChangeModifyRecord( LdifDnLine.create( valuesToDelete[i]
                    .getAttribute().getEntry().getDn().toString() ) );
                ModelConverter.addControls( cmr, valuesToDelete[i].getAttribute().getEntry() );
                cmr.setChangeType( LdifChangeTypeLine.createModify() );

                LdifModSpec modSpec = LdifModSpec.createDelete( valuesToDelete[i].getAttribute().getDescription() );
                if ( valuesToDelete[i].isString() )
                {
                    modSpec.addAttrVal( LdifAttrValLine.create( valuesToDelete[i].getAttribute().getDescription(),
                        valuesToDelete[i].getStringValue() ) );
                }
                else
                {
                    modSpec.addAttrVal( LdifAttrValLine.create( valuesToDelete[i].getAttribute().getDescription(),
                        valuesToDelete[i].getBinaryValue() ) );
                }
                modSpec.finish( LdifModSpecSepLine.create() );
                cmr.addModSpec( modSpec );
                cmr.finish( LdifSepLine.create() );

                this.applyModificationAndLog( cmr, monitor );
            }
        }
        catch ( ConnectionException e )
        {
            monitor.reportError( e );
        }
    }


    void delete( IAttribute[] attriubtesToDelete, StudioProgressMonitor monitor )
    {
        try
        {
            for ( int i = 0; !monitor.isCanceled() && i < attriubtesToDelete.length; i++ )
            {
                LdifChangeModifyRecord cmr = new LdifChangeModifyRecord( LdifDnLine.create( attriubtesToDelete[i]
                    .getEntry().getDn().toString() ) );
                ModelConverter.addControls( cmr, attriubtesToDelete[i].getEntry() );
                cmr.setChangeType( LdifChangeTypeLine.createModify() );

                LdifModSpec modSpec = LdifModSpec.createDelete( attriubtesToDelete[i].getDescription() );
                modSpec.finish( LdifModSpecSepLine.create() );
                cmr.addModSpec( modSpec );
                cmr.finish( LdifSepLine.create() );

                this.applyModificationAndLog( cmr, monitor );
            }
        }
        catch ( ConnectionException e )
        {
            monitor.reportError( e );
        }
    }


    void rename( IEntry entryToRename, DN newDn, boolean deleteOldRdn, StudioProgressMonitor monitor )
    {
        try
        {
            LdifChangeModDnRecord cmdr = new LdifChangeModDnRecord( LdifDnLine
                .create( entryToRename.getDn().toString() ) );
            ModelConverter.addControls( cmdr, entryToRename );
            cmdr.setChangeType( LdifChangeTypeLine.createModDn() );

            cmdr.setNewrdn( LdifNewrdnLine.create( newDn.getRdn().toString() ) );
            cmdr.setDeloldrdn( deleteOldRdn ? LdifDeloldrdnLine.create1() : LdifDeloldrdnLine.create0() );
            cmdr.finish( LdifSepLine.create() );

            this.applyModificationAndLog( cmdr, monitor );

            uncacheChildren( entryToRename );

        }
        catch ( ConnectionException e )
        {
            monitor.reportError( e );
        }
    }


    void move( IEntry entryToMove, DN newSuperior, StudioProgressMonitor monitor )
    {
        try
        {
            LdifChangeModDnRecord cmdr = new LdifChangeModDnRecord( LdifDnLine.create( entryToMove.getDn().toString() ) );
            ModelConverter.addControls( cmdr, entryToMove );
            cmdr.setChangeType( LdifChangeTypeLine.createModDn() );

            cmdr.setNewrdn( LdifNewrdnLine.create( entryToMove.getRdn().toString() ) );
            cmdr.setDeloldrdn( LdifDeloldrdnLine.create0() );
            cmdr.setNewsuperior( LdifNewsuperiorLine.create( newSuperior.toString() ) );
            cmdr.finish( LdifSepLine.create() );

            this.applyModificationAndLog( cmdr, monitor );

            uncacheChildren( entryToMove );

        }
        catch ( ConnectionException e )
        {
            monitor.reportError( e );
        }
    }


    private void uncacheChildren( IEntry entry )
    {
        IEntry[] children = entry.getChildren();
        if ( entry.getChildren() != null )
        {
            for ( int i = 0; i < children.length; i++ )
            {
                uncacheChildren( children[i] );
            }
        }
        browserConnection.uncacheEntry( entry );
    }


    void delete( IEntry entry, StudioProgressMonitor monitor )
    {
        try
        {
            LdifChangeDeleteRecord cdr = new LdifChangeDeleteRecord( LdifDnLine.create( entry.getDn().toString() ) );
            ModelConverter.addControls( cdr, entry );
            cdr.setChangeType( LdifChangeTypeLine.createDelete() );
            cdr.finish( LdifSepLine.create() );

            this.applyModificationAndLog( cdr, monitor );

            browserConnection.uncacheEntry( entry );

        }
        catch ( ConnectionException e )
        {
            monitor.reportError( e );
        }
    }


    void importLdif( LdifEnumeration enumeration, Writer logWriter, boolean continueOnError,
        StudioProgressMonitor monitor )
    {
        int importedCount = 0;
        int errorCount = 0;
        try
        {
            while ( !monitor.isCanceled() && enumeration.hasNext( monitor ) )
            {
                LdifContainer container = enumeration.next( monitor );

                if ( container instanceof LdifRecord )
                {

                    LdifRecord record = ( LdifRecord ) container;
                    try
                    {
                        this.applyModificationAndLog( record, monitor );

                        // update cache and adjust attribute/children initialization flags
                        DN dn = new DN( record.getDnLine().getValueAsString() );
                        IEntry entry = browserConnection.getEntryFromCache( dn );
                        DN parentDn = dn.getParentDn();
                        IEntry parentEntry = parentDn != null ? browserConnection.getEntryFromCache( dn.getParentDn() )
                            : null;

                        if ( record instanceof LdifChangeDeleteRecord )
                        {
                            if ( entry != null )
                            {
                                entry.setAttributesInitialized( false );
                                browserConnection.uncacheEntry( entry );
                            }
                            if ( parentEntry != null )
                            {
                                parentEntry.setChildrenInitialized( false );
                            }
                        }
                        else if ( record instanceof LdifChangeModDnRecord )
                        {
                            if ( entry != null )
                            {
                                entry.setAttributesInitialized( false );
                                browserConnection.uncacheEntry( entry );
                            }
                            if ( parentEntry != null )
                            {
                                parentEntry.setChildrenInitialized( false );
                            }
                            LdifChangeModDnRecord modDnRecord = ( LdifChangeModDnRecord ) record;
                            if ( modDnRecord.getNewsuperiorLine() != null )
                            {
                                DN newSuperiorDn = new DN( modDnRecord.getNewsuperiorLine().getValueAsString() );
                                IEntry newSuperiorEntry = browserConnection.getEntryFromCache( newSuperiorDn );
                                if ( newSuperiorEntry != null )
                                {
                                    newSuperiorEntry.setChildrenInitialized( false );
                                }
                            }
                        }
                        else if ( record instanceof LdifChangeAddRecord || record instanceof LdifContentRecord )
                        {
                            if ( parentEntry != null )
                            {
                                parentEntry.setChildrenInitialized( false );
                            }
                        }
                        else
                        {
                            if ( entry != null )
                            {
                                entry.setAttributesInitialized( false );
                            }
                        }

                        logModification( logWriter, record, monitor );

                        importedCount++;
                    }
                    catch ( Exception e )
                    {

                        logModificationError( logWriter, record, e, monitor );

                        errorCount++;

                        if ( !continueOnError )
                        {
                            monitor.reportError( e );
                            return;
                        }
                    }

                    monitor.reportProgress( BrowserCoreMessages.bind(
                        BrowserCoreMessages.ldif__imported_n_entries_m_errors, new String[]
                            { "" + importedCount, "" + errorCount } ) ); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    logWriter.write( container.toRawString() );
                }
            }

            if ( errorCount > 0 )
            {
                monitor.reportError( BrowserCoreMessages.bind( BrowserCoreMessages.ldif__n_errors_see_logfile,
                    new String[]
                        { "" + errorCount } ) ); //$NON-NLS-1$
            }
        }
        catch ( Exception e )
        {
            monitor.reportError( e );
        }
    }


    private void applyModificationAndLog( LdifRecord record, StudioProgressMonitor monitor ) throws ConnectionException
    {
        if ( record != null )
        {
            try
            {
                int referralsHandlingMethod = browserConnection.getReferralsHandlingMethod();
                // int referralsHandlingMethod =
                // IConnection.HANDLE_REFERRALS_IGNORE;
                browserConnection.connectionProvider.applyModification( record, referralsHandlingMethod, monitor );

                StringWriter writer = new StringWriter();
                this.logModification( writer, record, monitor );
                this.modificationLogger.log( writer.toString() );

            }
            catch ( ConnectionException ce )
            {
                if ( ce instanceof ReferralException )
                {
                    if ( browserConnection.getReferralsHandlingMethod() == IBrowserConnection.HANDLE_REFERRALS_FOLLOW )
                    {
                        // get referral handler
                        IReferralHandler referralHandler = BrowserCorePlugin.getDefault().getReferralHandler();
                        if ( referralHandler == null )
                        {
                            throw new ConnectionException( BrowserCoreMessages.model__no_referral_handler );
                        }

                        // for all referrals
                        ReferralException re = ( ReferralException ) ce;
                        for ( int r = 0; r < re.getReferrals().length; r++ )
                        {
                            // parse referral URL
                            String referral = re.getReferrals()[r];
                            URL referralUrl = new URL( referral );

                            // get referral connection
                            IBrowserConnection referralConnection = referralHandler.getReferralConnection( referralUrl );
                            if ( referralConnection == null )
                            {
                                // throw new
                                // ConnectionException(BrowserCoreMessages.model__no_referral_connection);
                                continue;
                            }

                            ( ( BrowserConnection ) referralConnection ).modifyHandler.applyModificationAndLog( record,
                                monitor );
                        }
                    }
                }
                else
                {
                    StringWriter writer = new StringWriter();
                    this.logModificationError( writer, record, ce, monitor );
                    this.modificationLogger.log( writer.toString() );

                    throw ce;
                }
            }
        }
    }


    ModificationLogger getModificationLogger()
    {
        return modificationLogger;
    }


    private void logModificationError( Writer logWriter, LdifRecord record, Exception e, StudioProgressMonitor monitor )
    {
        try
        {
            DateFormat df = new SimpleDateFormat( BrowserCoreConstants.DATEFORMAT );

            String errorComment = "#!ERROR " + e.getMessage(); //$NON-NLS-1$
            errorComment = errorComment.replaceAll( "\r", " " ); //$NON-NLS-1$ //$NON-NLS-2$
            errorComment = errorComment.replaceAll( "\n", " " ); //$NON-NLS-1$ //$NON-NLS-2$
            LdifCommentLine errorCommentLine = LdifCommentLine.create( errorComment );

            logWriter.write( LdifCommentLine.create( "#!RESULT ERROR" ).toFormattedString() ); //$NON-NLS-1$
            logWriter
                .write( LdifCommentLine
                    .create(
                        "#!CONNECTION ldap://" + browserConnection.getConnection().getHost() + ":" + browserConnection.getConnection().getPort() ).toFormattedString() ); //$NON-NLS-1$ //$NON-NLS-2$
            logWriter.write( LdifCommentLine.create( "#!DATE " + df.format( new Date() ) ).toFormattedString() ); //$NON-NLS-1$
            logWriter.write( errorCommentLine.toFormattedString() );
            logWriter.write( record.toFormattedString() );
        }
        catch ( IOException ioe )
        {
            monitor.reportError( BrowserCoreMessages.model__error_logging_modification, ioe );
        }
    }


    private void logModification( Writer logWriter, LdifRecord record, StudioProgressMonitor monitor )
    {
        try
        {
            DateFormat df = new SimpleDateFormat( BrowserCoreConstants.DATEFORMAT );
            logWriter.write( LdifCommentLine.create( "#!RESULT OK" ).toFormattedString() ); //$NON-NLS-1$
            logWriter
                .write( LdifCommentLine
                    .create(
                        "#!CONNECTION ldap://" + browserConnection.getConnection().getHost() + ":" + browserConnection.getConnection().getPort() ).toFormattedString() ); //$NON-NLS-1$ //$NON-NLS-2$
            logWriter.write( LdifCommentLine.create( "#!DATE " + df.format( new Date() ) ).toFormattedString() ); //$NON-NLS-1$
            logWriter.write( record.toFormattedString() );
        }
        catch ( IOException ioe )
        {
            monitor.reportError( BrowserCoreMessages.model__error_logging_modification, ioe );
        }
    }

}
