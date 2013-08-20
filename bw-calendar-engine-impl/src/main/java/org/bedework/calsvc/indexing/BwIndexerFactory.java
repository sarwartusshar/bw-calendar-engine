/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.calsvc.indexing;

import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeException;

/** Create an instance of an indexer for bedework.
 *
 * @author douglm
 *
 */
public class BwIndexerFactory {
  /* No instantiation
   */
  private BwIndexerFactory() {
  }

  /** Factory method to get current indexer
   *
   * @param publick
   * @param principal - who we are searching for
   * @param writeable true if the caller can update the index
   * @param authpars - for authenticated limits
   * @param unauthpars - for public limits
   * @param syspars
   * @throws CalFacadeException
   */
  public static BwIndexer getIndexer(final boolean publick,
                                     final String principal,
                                     final boolean writeable,
                                     final AuthProperties authpars,
                                     final AuthProperties unauthpars,
                                     final SystemProperties syspars) throws CalFacadeException {
    try {
      if (publick) {
          return new BwIndexSolrImpl(true,
                                     principal,
                                     writeable,
                                     authpars, unauthpars, syspars,
                                     true,  // No admin
                                     null); // No explicit name
      }

      return new BwIndexSolrImpl(false,
                                 principal,
                                 writeable,
                                 authpars, unauthpars, syspars,
                                 true,  // No admin
                                 null); // No explicit name
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  /** Factory method allowing us to specify the system root. This should only
   * be called from the crawler which will be indexing into an alternative
   * index.
   *
   * @param principal
   * @param writeable true if the caller can update the index
   * @param authpars - for authenticated limits
   * @param unauthpars - for public limits
   * @param syspars
   * @param indexRoot
   * @return indexer
   * @throws CalFacadeException
   */
  public static BwIndexer getIndexer(final String principal,
                                     final boolean writeable,
                                     final AuthProperties authpars,
                                     final AuthProperties unauthpars,
                                     final SystemProperties syspars,
                                     final String indexRoot) throws CalFacadeException {
    try {
      return new BwIndexSolrImpl(true,
                                 principal,
                                 writeable,
                                 authpars, unauthpars, syspars,
                                 false,  // Admin
                                 indexRoot); // Explicit name
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }
}
