/**
 * Copyright (C) 2013, 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.cache.ehcache;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.bonitasoft.engine.cache.CacheConfigurations;
import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * Platform version of the EhCacheCacheService
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class PlatformEhCacheCacheService extends CommonEhCacheCacheService implements PlatformCacheService {

    private final URL configFile;

    public PlatformEhCacheCacheService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor,
            final CacheConfigurations cacheConfigurations, final URL configFile) {
        super(logger, sessionAccessor, cacheConfigurations);
        this.configFile = configFile;
    }

    public PlatformEhCacheCacheService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor,
            final CacheConfigurations cacheConfigurations) {
        this(logger, sessionAccessor, cacheConfigurations, null);
    }

    @Override
    protected String getKeyFromCacheName(final String cacheName) {
        return "P_" + cacheName;
    }

    @Override
    public List<String> getCachesNames() {
        final String[] cacheNames = cacheManager.getCacheNames();
        final ArrayList<String> cacheNamesList = new ArrayList<String>(cacheNames.length);
        String prefix;
        prefix = "P_";
        for (final String cacheName : cacheNames) {
            if (cacheName.startsWith(prefix)) {
                cacheNamesList.add(getCacheNameFromKey(cacheName));
            }
        }
        return cacheNamesList;
    }

    private String getCacheNameFromKey(final String cacheNameKey) {
        return cacheNameKey.substring(cacheNameKey.indexOf('_') + 1);
    }

    @Override
    public synchronized void start() {
        cacheManager = configFile != null ? new CacheManager(configFile) : new CacheManager();
    }

    @Override
    public synchronized void stop() {
        if (cacheManager != null) {
            cacheManager.shutdown();
            cacheManager = null;
        }
    }

    @Override
    public void pause() {
        try {
            clearAll();
        } catch (final SCacheException sce) {
            throw new SBonitaRuntimeException(sce);
        }
    }

    @Override
    public void resume() {
        // nothing to do
    }

}
