/*
 * Copyright (c) 2002-2012, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.fdw.modules.wizardpdfproducer.service;

import fr.paris.lutece.plugins.directory.business.Directory;
import fr.paris.lutece.plugins.directory.business.EntryFilter;
import fr.paris.lutece.plugins.directory.business.EntryHome;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.business.producerconfig.ConfigProducer;
import fr.paris.lutece.plugins.directory.modules.pdfproducer.service.ConfigProducerService;
import fr.paris.lutece.plugins.fdw.modules.wizard.business.DuplicationContext;
import fr.paris.lutece.plugins.fdw.modules.wizard.exception.DuplicationException;
import fr.paris.lutece.plugins.fdw.modules.wizard.service.DuplicationService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;

import java.util.ArrayList;
import java.util.List;


/**
 * Duplication service
 *
 */
public class PdfProducerConfigDuplicationService extends DuplicationService
{
    private static final String PLUGIN_NAME = "fdw-wizardpdfproducer";
    private static final String CONFIG_TYPE = "PDF";
    private ConfigProducerService _configProducerService;

    @Override
    public void doDuplicate( DuplicationContext context )
        throws DuplicationException
    {
        if ( ( context != null ) && context.isDirectoryDuplication(  ) )
        {
            Plugin plugin = PluginService.getPlugin( PLUGIN_NAME );
            Directory directoryToCopy = context.getDirectoryToCopy(  );
            Directory copyOfDirectory = context.getDirectoryCopy(  );

            if ( ( directoryToCopy != null ) && ( copyOfDirectory != null ) )
            {
                try
                {
                    List<ConfigProducer> listConfigsToCopy = _configProducerService.loadListProducerConfig( plugin,
                            directoryToCopy.getIdDirectory(  ), CONFIG_TYPE );

                    for ( ConfigProducer configProducerToCopy : listConfigsToCopy )
                    {
                        // copy of config
                        ConfigProducer configProducerCopy = configProducerToCopy;
                        configProducerCopy.setIdDirectory( copyOfDirectory.getIdDirectory(  ) );

                        // list of entries to copy
                        EntryFilter entryFilter = new EntryFilter(  );
                        entryFilter.setIdDirectory( directoryToCopy.getIdDirectory(  ) );

                        List<IEntry> listEntryToCopy = EntryHome.getEntryList( entryFilter, plugin );

                        // list of copied entries
                        entryFilter = new EntryFilter(  );
                        entryFilter.setIdDirectory( copyOfDirectory.getIdDirectory(  ) );

                        List<IEntry> listEntryCopy = EntryHome.getEntryList( entryFilter, plugin );

                        // list of id entry to copy
                        List<Integer> listConfigIdEntryToCopy = _configProducerService.loadListConfigEntry( plugin,
                                configProducerToCopy.getIdProducerConfig(  ) );

                        // list of copied id entry
                        List<Integer> listConfigIdEntryCopy = new ArrayList<Integer>(  );

                        // copy of entries list
                        // we use the position of entries to find matching entries between the original directory and the copy
                        int nSize = listEntryToCopy.size(  );

                        for ( int i = 0; i < nSize; i++ )
                        {
                            IEntry entry = listEntryToCopy.get( i );

                            if ( listConfigIdEntryToCopy.contains( entry.getIdEntry(  ) ) )
                            {
                                IEntry correspondingEntry = listEntryCopy.get( i );
                                listConfigIdEntryCopy.add( correspondingEntry.getIdEntry(  ) );
                            }
                        }

                        // perform copy
                        _configProducerService.addNewConfig( plugin, configProducerCopy, listConfigIdEntryCopy );
                    }
                }
                catch ( Exception e )
                {
                    // rollback - delete already copied config producers
                    List<ConfigProducer> listConfigsToDelete = _configProducerService.loadListProducerConfig( plugin,
                            copyOfDirectory.getIdDirectory(  ), CONFIG_TYPE );

                    for ( ConfigProducer configProducerToDelete : listConfigsToDelete )
                    {
                        _configProducerService.deleteProducerConfig( plugin,
                            configProducerToDelete.getIdProducerConfig(  ) );
                    }

                    throw new DuplicationException( e );
                }
            }
        }
    }

    /**
     * set the config producer service
     * @param configProducerService the config producer service to set
     */
    public void setconfigProducerService( ConfigProducerService configProducerService )
    {
        this._configProducerService = configProducerService;
    }
}
