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
package org.bedework.inoutsched.processors;

import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calsvc.CalSvcDb;
import org.bedework.calsvci.CalSvcI;

/** Abstract class to support scheduling messages which avoid the inbox.
 *
 * @author Mike Douglass
 */
public abstract class SchedProcessor extends CalSvcDb {
  /**
   * @param svci
   */
  public SchedProcessor(final CalSvcI svci) {
    super(null);
    setSvc(svci);
  }

  /** Result from processing */
  public static class SchedProcResult {
    /** Result of the scheduling operations */
    public ScheduleResult sr = new ScheduleResult();

    /* * processors set this true when appropriate * /
    public boolean noInboxChange;

    /** Errors imply removal of associated inbox entry if any * /
    public boolean removeInboxEntry;

    /** Update was just attendee accepting * /
    public boolean attendeeAccepting;
    */
  }

  /**
   * @param ei - the originating event
   * @return ProcessResult
   * @throws CalFacadeException
   */
  public abstract SchedProcResult process(final EventInfo ei) throws CalFacadeException;
}
