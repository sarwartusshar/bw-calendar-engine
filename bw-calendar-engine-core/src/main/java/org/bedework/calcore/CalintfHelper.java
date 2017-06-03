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
package org.bedework.calcore;

import org.bedework.access.Acl;
import org.bedework.access.Acl.CurrentAccess;
import org.bedework.access.PrivilegeDefs;
import org.bedework.calcore.hibernate.DAOBase;
import org.bedework.calcore.hibernate.Filters;
import org.bedework.calcorei.CalintfDefs;
import org.bedework.calcorei.CoreEventInfo;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventAnnotation;
import org.bedework.calfacade.BwEventProxy;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwStats;
import org.bedework.calfacade.base.AlarmsEntity;
import org.bedework.calfacade.base.AttachmentsEntity;
import org.bedework.calfacade.base.AttendeesEntity;
import org.bedework.calfacade.base.BwOwnedDbentity;
import org.bedework.calfacade.base.BwShareableContainedDbentity;
import org.bedework.calfacade.base.CategorisedEntity;
import org.bedework.calfacade.base.CommentedEntity;
import org.bedework.calfacade.base.ContactedEntity;
import org.bedework.calfacade.base.DescriptionEntity;
import org.bedework.calfacade.base.PropertiesEntity;
import org.bedework.calfacade.base.RecurrenceEntity;
import org.bedework.calfacade.base.ResourcedEntity;
import org.bedework.calfacade.base.SummaryEntity;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.configs.BasicSystemProperties;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.exc.CalFacadeAccessException;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.indexing.BwIndexer;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.PrincipalInfo;
import org.bedework.calfacade.util.AccessChecker;
import org.bedework.calfacade.util.AccessUtilI;
import org.bedework.calfacade.util.NotificationsInfo;
import org.bedework.calfacade.wrappers.CalendarWrapper;
import org.bedework.calsvci.CalSvcFactoryDefault;
import org.bedework.sysevents.events.SysEvent;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.misc.Util;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

/** Class used as basis for a number of helper classes.
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public abstract class CalintfHelper
        implements CalintfDefs, PrivilegeDefs, Serializable {
  /**
   */
  public static interface Callback extends Serializable {
    void registerDao(DAOBase dao);
    
    /**
     * @return BasicSystemProperties object
     * @throws CalFacadeException on error
     */
    BasicSystemProperties getSyspars() throws CalFacadeException;

    /**
     * @return PrincipalInfo object
     * @throws CalFacadeException on error
     */
    PrincipalInfo getPrincipalInfo() throws CalFacadeException;

    /** Only valid during a transaction.
     *
     * @return a timestamp from the db
     * @throws CalFacadeException on error
     */
    Timestamp getCurrentTimestamp() throws CalFacadeException;

    /**
     * @return BwStats
     * @throws CalFacadeException on error
     */
    BwStats getStats() throws CalFacadeException;

    /** Used to fetch a calendar from the cache - assumes any access
     *
     * @param path
     * @return BwCalendar
     * @throws CalFacadeException on error
     */
    BwCalendar getCollection(String path) throws CalFacadeException;

    /** Used to fetch a category from the cache - assumes any access
     *
     * @param uid
     * @return BwCategory
     * @throws CalFacadeException on error
     */
    BwCategory getCategory(String uid) throws CalFacadeException;

    /** Used to fetch a calendar from the cache
     *
     * @param path of collection
     * @param desiredAccess minimum
     * @param alwaysReturn false will throw exception on no access
     * @return BwCalendar
     * @throws CalFacadeException on error
     */
    BwCalendar getCollection(String path,
                             int desiredAccess,
                             boolean alwaysReturn) throws CalFacadeException;

    /** Called to notify container that an event occurred. This method should
     * queue up notifications until after transaction commit as consumers
     * should only receive notifications when the actual data has been written.
     *
     * @param ev the system event
     * @throws CalFacadeException on error
     */
    void postNotification(SysEvent ev) throws CalFacadeException;

    /**
     * @return true if restoring
     */
    boolean getForRestore();

    /**
     * @param entity may influence choice of indexer
     * @return BwIndexer
     * @throws CalFacadeException on error
     */
    BwIndexer getIndexer(BwOwnedDbentity entity) throws CalFacadeException;
  }

  protected AccessChecker ac;

  protected boolean debug;

  protected boolean collectTimeStats;

  protected Callback cb;

  protected int currentMode = guestMode;

  private transient Logger log;

  protected boolean sessionless;

  public SystemProperties sysprops;

  public AuthProperties authprops;

  /** Initialize
   *
   * @param cb
   * @param ac
   * @param currentMode
   * @param sessionless
   */
  public void init(final Callback cb,
                   final AccessChecker ac,
                   final int currentMode,
                   final boolean sessionless) {
    this.cb = cb;
    this.ac = ac;
    this.currentMode = currentMode;
    this.sessionless = sessionless;
    debug = getLogger().isDebugEnabled();
    collectTimeStats = Logger.getLogger("org.bedework.collectTimeStats").isDebugEnabled();
  }

  public abstract void throwException(final CalFacadeException cfe)
          throws CalFacadeException;

  public void throwException(final String err)
          throws CalFacadeException {
    throwException(new CalFacadeException(err));
  }

  public void throwException(final String err,
                             final String extra)
          throws CalFacadeException {
    throwException(new CalFacadeException(err, extra));
  }

  /** Used to fetch a calendar from the cache
   *
   * @param path
   * @param desiredAccess
   * @param alwaysReturn
   * @return BwCalendar
   * @throws org.bedework.calfacade.exc.CalFacadeException
   */
  protected BwCalendar getCollection(String path,
                                     int desiredAccess,
                                     boolean alwaysReturn) throws CalFacadeException {
    return cb.getCollection(path, desiredAccess,
                            alwaysReturn);
  }

  protected BasicSystemProperties getSyspars() throws CalFacadeException {
    return cb.getSyspars();
  }
  protected BwPrincipal getAuthenticatedPrincipal() throws CalFacadeException {
    if (cb == null) {
      return null;
    }

    return cb.getPrincipalInfo().getAuthPrincipal();
  }

  protected BwPrincipal getPrincipal() throws CalFacadeException {
    if (cb == null) {
      return null;
    }

    return cb.getPrincipalInfo().getPrincipal();
  }

  protected String authenticatedPrincipal() throws CalFacadeException {
    BwPrincipal p = getAuthenticatedPrincipal();

    if (p == null) {
      return null;
    }

    return p.getPrincipalRef();
  }

  public String currentPrincipal() throws CalFacadeException {
    if (cb == null) {
      return null;
    }

    if (getPrincipal() == null) {
      return null;
    }

    return getPrincipal().getPrincipalRef();
  }

  /** Only valid during a transaction.
   *
   * @return a timestamp from the db
   * @throws CalFacadeException
   */
  public Timestamp getCurrentTimestamp() throws CalFacadeException {
    return cb.getCurrentTimestamp();
  }

  protected BwCalendar getCollection(final String path) throws CalFacadeException {
    return cb.getCollection(path);
  }

  protected boolean getForRestore() {
    return cb.getForRestore();
  }

  protected BwIndexer getIndexer(final BwOwnedDbentity entity) throws CalFacadeException {
    return cb.getIndexer(entity);
  }

  protected AccessChecker getAccessChecker() throws CalFacadeException {
    return ac;
  }

  /*
  protected ESQueryFilter getFilters() throws CalFacadeException {
    return ((BwIndexEsImpl)getIndexer()).getFilters();
  }*/

  protected void indexEntity(final EventInfo ei) throws CalFacadeException {
    if (ei.getEvent().getRecurrenceId() != null) {
      // Cannot index single instance
      warn("Tried to index a recurrence instance");
      return;
    }

    if (!getForRestore()) {
      getIndexer(ei.getEvent()).indexEntity(ei);
    }
  }

  protected void unindexEntity(final EventInfo ei) throws CalFacadeException {
    final BwEvent ev = ei.getEvent();

    if (ev.getRecurrenceId() != null) {
      // Cannot index single instance
      warn("Tried to unindex a recurrence instance");
      return;
    }

    getIndexer(ev).unindexEntity(ev.getHref());
  }

  protected void indexEntity(final BwCalendar col) throws CalFacadeException {
    if (!getForRestore()) {
      getIndexer(col).indexEntity(col);
    }
  }

  protected void unindexEntity(final BwCalendar col) throws CalFacadeException {
    getIndexer(col).unindexEntity(col.getPath());
  }

  /** Called to notify container that an event occurred. This method should
   * queue up notifications until after transaction commit as consumers
   * should only receive notifications when the actual data has been written.
   *
   * @param ev
   * @throws CalFacadeException
   */
  public void postNotification(final SysEvent ev) throws CalFacadeException {
    cb.postNotification(ev);
  }

  protected CalendarWrapper wrap(final BwCalendar val) {
    if (val == null) {
      return null;
    }

    if (val instanceof CalendarWrapper) {
      // CALWRAPPER get this from getEvents with an internal temp calendar
      return (CalendarWrapper)val;
    }
    return new CalendarWrapper(val, ac.getAccessUtil());
  }

  protected BwCalendar unwrap(final BwCalendar val) throws CalFacadeException {
    if (val == null) {
      return null;
    }

    if (!(val instanceof CalendarWrapper)) {
      // We get these at the moment - getEvents at svci level
      return val;
      // CALWRAPPER throw new CalFacadeException("org.bedework.not.wrapped");
    }

    return ((CalendarWrapper)val).fetchEntity();
  }

  protected BwCalendar getEntityCollection(final String path,
                                           final int nonSchedAccess,
                                           final boolean scheduling,
                                           final boolean alwaysReturn) throws CalFacadeException {
    final int desiredAccess;

    if (!scheduling) {
      desiredAccess = nonSchedAccess;
    } else {
      desiredAccess = privAny;
    }

    final BwCalendar cal = getCollection(path, desiredAccess,
                                         alwaysReturn | scheduling);
    if (cal == null) {
      return null;
    }

    if (!cal.getCalendarCollection()) {
      throwException(new CalFacadeAccessException());
    }

    if (!scheduling) {
      return cal;
    }

    CurrentAccess ca;
    final AccessUtilI access = ac.getAccessUtil();

    if ((cal.getCalType() == BwCalendar.calTypeInbox) ||
        (cal.getCalType() == BwCalendar.calTypePendingInbox)) {
      ca = access.checkAccess(cal, privScheduleDeliver, true);
      if (!ca.getAccessAllowed()) {
        // try old style
        ca = access.checkAccess(cal, privScheduleRequest, alwaysReturn);
      }
    } else if (cal.getCalType() == BwCalendar.calTypeOutbox) {
      ca = access.checkAccess(cal, privScheduleSend, true);
      if (!ca.getAccessAllowed()) {
        // try old style
        ca = access.checkAccess(cal, privScheduleReply, alwaysReturn);
      }
    } else {
      throw new CalFacadeAccessException();
    }

    if (!ca.getAccessAllowed()) {
      return null;
    }

    return cal;
  }

  protected void tombstoneEntity(final BwShareableContainedDbentity val) {
    if (val instanceof AlarmsEntity) {
      clearCollection(((AlarmsEntity)val).getAlarms());
    }

    if (val instanceof AttachmentsEntity) {
      clearCollection(((AttachmentsEntity)val).getAttachments());
    }

    if (val instanceof AttendeesEntity) {
      clearCollection(((AttendeesEntity)val).getAttendees());
    }

    if (val instanceof CategorisedEntity) {
      clearCollection(((CategorisedEntity)val).getCategoryUids());
    }

    if (val instanceof CommentedEntity) {
      clearCollection(((CommentedEntity)val).getComments());
    }

    if (val instanceof ContactedEntity) {
      clearCollection(((ContactedEntity)val).getContacts());
    }

    if (val instanceof DescriptionEntity) {
      clearCollection(((DescriptionEntity)val).getDescriptions());
    }

    if (val instanceof RecurrenceEntity) {
      RecurrenceEntity re = (RecurrenceEntity)val;

      re.setRecurring(false);
      clearCollection(re.getExdates());
      clearCollection(re.getExrules());
      clearCollection(re.getRdates());
      clearCollection(re.getRrules());
    }

    if (val instanceof ResourcedEntity) {
      clearCollection(((ResourcedEntity)val).getResources());
    }

    if (val instanceof SummaryEntity) {
      clearCollection(((SummaryEntity)val).getSummaries());
    }

    if (val instanceof PropertiesEntity) {
      clearCollection(((PropertiesEntity)val).getProperties());
    }
  }

  protected void clearCollection(final Collection val) {
    if (val == null) {
      return;
    }

    val.clear();
  }

  /* Post processing of event. Return null or throw exception for no access
   */
  protected CoreEventInfo postGetEvent(final BwEvent ev,
                                       final int desiredAccess,
                                       final boolean nullForNoAccess,
                                       final Filters f) throws CalFacadeException {
    if (ev == null) {
      return null;
    }

    final CurrentAccess ca = ac.checkAccess(ev, 
                                            desiredAccess, 
                                            nullForNoAccess);

    if (!ca.getAccessAllowed()) {
      return null;
    }

    return postGetEvent(ev, f, ca);
  }

  /* Post processing of event access has been checked
   */
  protected CoreEventInfo postGetEvent(final BwEvent ev,
                                       final Filters f,
                                       final Acl.CurrentAccess ca) throws CalFacadeException {
    /* XXX-ALARM
    if (currentMode == userMode) {
      ev.setAlarms(getAlarms(ev, user));
    }
    */

    BwEvent event;

    if (ev instanceof BwEventAnnotation) {
      event = new BwEventProxy((BwEventAnnotation)ev);

      if ((f != null) && !f.postFilter(ev, currentPrincipal())) {
        return null;
      }
    } else {
      event = ev;
    }

    CoreEventInfo cei = new CoreEventInfo(event, ca);

    return cei;
  }

  protected AuthProperties getAuthprops() throws CalFacadeException {
    if (authprops == null) {
      authprops = new CalSvcFactoryDefault().getSystemConfig().getAuthProperties(currentMode == guestMode);
    }

    return authprops;
  }

  protected SystemProperties getSysprops() throws CalFacadeException {
    if (sysprops == null) {
      sysprops = new CalSvcFactoryDefault().getSystemConfig().getSystemProperties();
    }

    return sysprops;
  }

  protected void stat(final String name,
                      final Long startTime) throws CalFacadeException {
    if (!collectTimeStats) {
      return;
    }

    postNotification(SysEvent.makeStatsEvent(name,
                                             System.currentTimeMillis() - startTime));
  }

  protected void notifyDelete(final boolean reallyDelete,
                              final BwEvent val,
                              final boolean shared) throws CalFacadeException {
    SysEvent.SysCode code;

    if (reallyDelete) {
      code = SysEvent.SysCode.ENTITY_DELETED;
    } else {
      code = SysEvent.SysCode.ENTITY_TOMBSTONED;
    };

    String note = getChanges(code, val);
    if (note == null) {
      return;
    }

    postNotification(
            SysEvent.makeEntityDeletedEvent(code,
                                            authenticatedPrincipal(),
                                            val.getOwnerHref(),
                                            val.getHref(),
                                            shared,
                                            val.getPublick(),
                                            true, // indexed,
                                            IcalDefs.fromEntityType(
                                                    val.getEntityType()),
                                            val.getRecurrenceId(),
                                            note,
                                            null)); // XXX Emit multiple targted?
  }

  protected void notify(final SysEvent.SysCode code,
                        final BwEvent val,
                        final boolean shared) throws CalFacadeException {
    final String note = getChanges(code, val);
    if (note == null) {
      return;
    }

    final boolean indexed = true;

    postNotification(
            SysEvent.makeEntityUpdateEvent(code,
                                           authenticatedPrincipal(),
                                           val.getOwnerHref(),
                                           val.getHref(),
                                           shared,
                                           indexed,
                                           val.getRecurrenceId(),
                                           note,
                                           null)); // XXX Emit multiple targeted?
  }

  protected void notifyMove(final SysEvent.SysCode code,
                            final String oldHref,
                            final boolean oldShared,
                            final BwEvent val,
                            final boolean shared) throws CalFacadeException {
    postNotification(
            SysEvent.makeEntityMovedEvent(code,
                                          currentPrincipal(),
                                          val.getOwnerHref(),
                                          val.getHref(),
                                          shared,
                                          false,
                                          oldHref,
                                          oldShared));
  }

  protected void notifyInstanceChange(final SysEvent.SysCode code,
                                      final BwEvent val,
                                      final boolean shared,
                                      final String recurrenceId) throws CalFacadeException {
    final String note = getChanges(code, val);
    if (note == null) {
      return;
    }

    /* We flag these as indexed. They get handled by the update for
         the master
       */
    if (code.equals(SysEvent.SysCode.ENTITY_DELETED) ||
            code.equals(SysEvent.SysCode.ENTITY_TOMBSTONED)) {
      postNotification(
              SysEvent.makeEntityDeletedEvent(code,
                                              authenticatedPrincipal(),
                                              val.getOwnerHref(),
                                              val.getHref(),
                                              shared,
                                              val.getPublick(),
                                              true, // Indexed
                                              IcalDefs.fromEntityType(
                                                      val.getEntityType()),
                                              recurrenceId,
                                              note,
                                              null)); // XXX Emit multiple targted?
    } else {
      postNotification(
              SysEvent.makeEntityUpdateEvent(code,
                                             authenticatedPrincipal(),
                                             val.getOwnerHref(),
                                             val.getHref(),
                                             shared,
                                             true, // Indexed
                                             val.getRecurrenceId(),
                                             note,  // changes
                                             null)); // XXX Emit multiple targted?
    }
  }

  protected String getChanges(final SysEvent.SysCode code,
                              final BwEvent val) {
    try {
      if (code.equals(SysEvent.SysCode.ENTITY_DELETED) ||
              code.equals(SysEvent.SysCode.ENTITY_TOMBSTONED)) {
        return NotificationsInfo.deleted(authenticatedPrincipal(),
                                         val);
      }

      if (code.equals(SysEvent.SysCode.ENTITY_UPDATED)) {
        return NotificationsInfo.updated(authenticatedPrincipal(), val);
      }

      if (code.equals(SysEvent.SysCode.ENTITY_ADDED)) {
        return NotificationsInfo.added(authenticatedPrincipal(), val);
      }

      return null;
    } catch (Throwable t) {
      error(t);
      return null;
    }
  }

  protected void restoreCategories(final CategorisedEntity ce) throws CalFacadeException {
    final Set<String> uids = ce.getCategoryUids();
    if (Util.isEmpty(uids)) {
      return;
    }

    for (final String uid: uids) {
      final BwCategory cat = cb.getCategory(uid);

      if (cat == null) {
        throw new CalFacadeException("Attempting to store null for cat uid "
                                             + uid);
      }

      ce.addCategory(cat);
    }
  }

  /** Get a logger for messages
   *
   * @return Logger
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void debugMsg(final String msg) {
    getLogger().debug(msg);
  }

  protected void warn(final String msg) {
    getLogger().warn(msg);
  }

  protected void trace(final String msg) {
    getLogger().debug(msg);
  }

  protected void error(final Throwable t) {
    getLogger().error(t.getLocalizedMessage(), t);
  }
}
