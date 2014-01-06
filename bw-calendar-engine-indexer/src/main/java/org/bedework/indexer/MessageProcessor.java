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
package org.bedework.indexer;

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwEventProxy;
import org.bedework.calfacade.base.BwOwnedDbentity;
import org.bedework.calfacade.configs.IndexProperties;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.indexing.BwIndexKey;
import org.bedework.calfacade.indexing.BwIndexer;
import org.bedework.sysevents.events.CollectionChangeEvent;
import org.bedework.sysevents.events.CollectionDeletedEvent;
import org.bedework.sysevents.events.EntityDeletedEvent;
import org.bedework.sysevents.events.EntityUpdateEvent;
import org.bedework.sysevents.events.SysEvent;
import org.bedework.util.misc.Util;

import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Class to handle incoming system event messages and fire off index processes
 * <p>
 * We assume that we do not need to figure out side effects of a change. For
 * example, if a collection is moved we will also get events for each affected
 * entity so we don't need to figure out what children are affected.
 * <p>
 * This may not hold true for recurring events. We possibly need to figure out
 * how to remove all instances and overrides. This might require a search
 * capability for path + uid
 *
 * @author douglm
 */
public class MessageProcessor extends CalSys {
  private transient Logger log;

  private boolean debug;

  transient private BwIndexer publicIndexer;

  transient private BwIndexer userIndexer;

  transient private String userIndexerPrincipal;

  protected long collectionsUpdated;
  protected long collectionsDeleted;

  protected long entitiesUpdated;
  protected long entitiesDeleted;

  private int maxRetryCt = 10;

  /**
   * @param props
   * @throws CalFacadeException
   */
  public MessageProcessor(final IndexProperties props) throws CalFacadeException {
    super("MessageProcessor", props.getAccount(), null);

    debug = getLog().isDebugEnabled();
  }

  /**
   * @param msg
   * @throws CalFacadeException
   */
  public void processMessage(final SysEvent msg) throws CalFacadeException {
    for (int ct = 0; ct < maxRetryCt; ct++) {
      try {
        if (debug) {
          debugMsg("Event " + msg.getSysCode());
        }

        if (msg instanceof CollectionChangeEvent) {
          collectionsUpdated++;
          doCollectionChange((CollectionChangeEvent)msg);
          return;
        }

        if (msg instanceof EntityUpdateEvent) {
          entitiesUpdated++;
          doEntityChange((EntityUpdateEvent)msg);
          return;
        }

        if (msg instanceof EntityDeletedEvent) {
          entitiesDeleted++;
          doEntityDelete((EntityDeletedEvent)msg);
          return;
        }

        if (msg instanceof CollectionDeletedEvent) {
          collectionsDeleted++;
          doCollectionDelete((CollectionDeletedEvent)msg);
          return;
        }

        return;
      } catch (CalFacadeAccessException cfae) {
        // No point in retrying this
        warn("No access (or deleted)");
        break;
      } catch (Throwable t) {
        warn("Error indexing msg");
        if (ct == 0) {
          warn("Will retry " + maxRetryCt + " times");
        }

        error(t);
      }
    }

    warn("Failed after " + maxRetryCt + " retries");
  }

  /**
   * @return count processed
   */
  public long getCollectionsUpdated() {
    return collectionsUpdated;
  }

  /**
   * @return count processed
   */
  public long getCollectionsDeleted() {
    return collectionsDeleted;
  }

  /**
   * @return count processed
   */
  public long getEntitiesUpdated() {
    return entitiesUpdated;
  }

  /**
   * @return count processed
   */
  public long getEntitiesDeleted() {
    return entitiesDeleted;
  }

  private void doCollectionDelete(final CollectionDeletedEvent cde)
                                                                  throws CalFacadeException {
    RemovalKey rk = new RemovalKey(cde.getPublick(),
                                   cde.getOwnerHref(),
                                   cde.getHref());

    remove(rk);
  }

  private void doCollectionChange(final CollectionChangeEvent cce)
                                                   throws CalFacadeException {
    try {
      setCurrentPrincipal(null);
      getSvci();

      BwCalendar col = getCollection(cce.getColPath());

      if (col != null) {
        // Null if no access or removed.
        add(col);
      }
    } finally {
      close();
    }
  }

  private String getParentPath(final String val) {
    int pos = val.lastIndexOf("/");

    if (pos <= 0) {
      return null;
    }

    return val.substring(0, pos);
  }

  private static class RemovalKey extends BwIndexKey {
    boolean publick;

    String ownerHref;

    RemovalKey(final boolean publick,
               final String ownerHref,
               final String path) {
      super(path);

      this.publick = publick;
      this.ownerHref = ownerHref;
    }

    RemovalKey(final String itemType,
               final boolean publick,
               final String ownerHref,
               final String href,
               final String rid) {
      super(itemType, href, rid);

      this.publick = publick;
      this.ownerHref = ownerHref;
    }
  }

  private void doEntityDelete(final EntityDeletedEvent ede)
       throws CalFacadeException {
    RemovalKey rk = new RemovalKey(ede.getType(),
                                   ede.getPublick(),
                                   ede.getOwnerHref(),
                                   ede.getHref(),
                                   ede.getRecurrenceId());

    remove(rk);
  }

  private void doEntityChange(final EntityUpdateEvent ece)
       throws CalFacadeException {
    try {
      setCurrentPrincipal(ece.getOwnerHref());
      getSvci();

      Collection<EventInfo> eis = getEvent(getParentPath(ece.getHref()),
                                           ece.getUid(),
                                           ece.getRecurrenceId());

      for (EventInfo ei : eis) {
        add(ei, false);
      }
    } finally {
      close();
    }
  }

  /*
   * ====================================================================
   * Private methods
   * ====================================================================
   */

  private void add(final BwCalendar val) throws CalFacadeException {
    getIndexer(val).indexEntity(val);
  }

  private void add(final EventInfo val,
                   final boolean firstRecurrence) throws CalFacadeException {
    boolean first = true;
    if (!Util.isEmpty(val.getOverrides())) {
      for (EventInfo ei : val.getOverrides()) {
        add(ei, first);
        first = false;
      }
    }

    if (firstRecurrence && (val.getEvent().getRecurrenceId() != null)) {
      // Indexing an override. Reindex the master event
      BwEventProxy proxy = (BwEventProxy)val.getEvent();

      EventInfo ei = new EventInfo(proxy.getTarget());

      getIndexer(val).indexEntity(ei);
    }

    getIndexer(val).indexEntity(val);
  }

  private void remove(final RemovalKey val) throws CalFacadeException {
    getIndexer(val).unindexEntity(val);
  }

  @SuppressWarnings("rawtypes")
  private BwIndexer getIndexer(final Object val) throws CalFacadeException {
    boolean publick = false;

    String principal = null;

    BwOwnedDbentity ent = null;

    if (val instanceof BwOwnedDbentity) {
      ent = (BwOwnedDbentity)val;
    } else if (val instanceof EventInfo) {
      ent = ((EventInfo)val).getEvent();
    } else if (val instanceof RemovalKey) {
      RemovalKey rk = (RemovalKey)val;
      publick = rk.publick;
      principal = rk.ownerHref;
    } else {
      throw new CalFacadeException("org.bedework.index.unexpected.class");
    }

    if (ent != null) {
      if (ent.getPublick() == null) {
        debugMsg("This is wrong");
      }
      publick = ent.getPublick();
      principal = ent.getOwnerHref();
    }

    return getIndexer(publick, principal);
  }

  private BwIndexer getIndexer(final boolean publick,
                               final String principal) throws CalFacadeException {
    try {
      if (publick) {
        if (publicIndexer == null) {
          publicIndexer = getSvci().getIndexer(true);
        }
        return publicIndexer;
      }

      if ((userIndexerPrincipal != null) &&
              (!userIndexerPrincipal.equals(principal))) {
        userIndexer = null;
      }

      if (userIndexer == null) {
        userIndexer = getSvci().getIndexer(principal);
      }

      return userIndexer;
    } catch (Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  protected Logger getLog() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  @Override
  protected void debugMsg(final String msg) {
    getLog().debug(msg);
  }

  @Override
  protected void info(final String msg) {
    getLog().info(msg);
  }

  protected void warn(final String msg) {
    getLog().warn(msg);
  }
}
