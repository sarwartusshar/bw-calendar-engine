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
package org.bedework.sysevents.listeners;

import org.bedework.sysevents.NotificationException;
import org.bedework.sysevents.events.SysEvent;
import org.bedework.util.logging.BwLogger;

/** This is the implementation of a notifications action class which logs
 * system events.
 *
 * @author Mike Douglass       douglm - rpi.edu
 */
public class SysEventLoggerAction extends SysEventActionClass {
  /**
   */
  public SysEventLoggerAction() {
  }

  /* (non-Javadoc)
   * @see org.bedework.sysevents.listeners.SysEventListener.SysEventActionClass#action(org.bedework.sysevents.events.SysEvent)
   */
  public void action(SysEvent ev) throws NotificationException {
    trace(ev.toString());
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
