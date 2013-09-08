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

package org.bedework.caldav.bwserver.stdupdaters;

import org.bedework.caldav.bwserver.PropertyUpdater;
import org.bedework.caldav.server.sysinterface.SysIntf.UpdateResult;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.util.misc.Util;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;

import ietf.params.xml.ns.icalendar_2.TextPropertyType;

import java.util.Set;

/**
 * @author douglm
 *
 */
public class ContactPropUpdater implements PropertyUpdater {
  public UpdateResult applyUpdate(final UpdateInfo ui) throws WebdavException {
    try {
      ChangeTableEntry cte = ui.getCte();
      BwEvent ev = ui.getEvent();

      Set<BwContact> contacts = ev.getContacts();

      BwString nm = new BwString(UpdaterUtil.getLang(ui.getProp()),
                                 ((TextPropertyType)ui.getProp()).getText());

      String altrep = UpdaterUtil.getAltrep(ui.getProp());

      if (ui.isAdd()) {
        if (!Util.isEmpty(contacts)) {
          for (BwContact cnct: contacts) {
            if (cnct.getName().equals(nm)) {
              // Already there
              return UpdateResult.getOkResult();
            }
          }
        }

        // Add it
        BwContact cnct = ui.getIcalCallback().findContact(nm);

        if (cnct == null) {
          cnct = new BwContact();
          cnct.setName(nm);
          cnct.setLink(altrep);

          ui.getIcalCallback().addContact(cnct);
        }

        ev.addContact(cnct);
        cte.addAddedValue(cnct);

        return UpdateResult.getOkResult();
      }

      if (ui.isRemove()) {
        if (Util.isEmpty(contacts)) {
          // Nothing to remove
          return UpdateResult.getOkResult();
        }

        for (BwContact cnct: contacts) {
          if (cnct.getName().equals(nm)) {
            if (ev.removeContact(cnct)) {
              cte.addRemovedValue(cnct);
            }

            return UpdateResult.getOkResult();
          }
        }

        return UpdateResult.getOkResult();
      }

      if (ui.isChange()) {
        // Change a value
        if (Util.isEmpty(contacts)) {
          // Nothing to change
          return new UpdateResult("No contact to change");
        }

        for (BwContact evcnct: contacts) {
          if (evcnct.getName().equals(nm)) {
            // Found - remove that one and add a new one.
            BwString newnm = new BwString(UpdaterUtil.getLang(ui.getUpdprop()),
                                          ((TextPropertyType)ui.getUpdprop()).getText());

            BwContact cnct = ui.getIcalCallback().findContact(newnm);

            if (cnct == null) {
              cnct = new BwContact();
              cnct.setName(newnm);
              cnct.setLink(altrep);

              ui.getIcalCallback().addContact(cnct);
            }

            if (ev.removeContact(evcnct)) {
              cte.addRemovedValue(evcnct);
            }

            ev.addContact(cnct);
            cte.addAddedValue(cnct);

            return UpdateResult.getOkResult();
          }
        }
      }

      return UpdateResult.getOkResult();
    } catch (CalFacadeException cfe) {
      throw new WebdavException(cfe);
    }
  }
}
