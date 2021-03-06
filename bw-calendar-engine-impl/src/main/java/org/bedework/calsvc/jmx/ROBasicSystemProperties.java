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
package org.bedework.calsvc.jmx;

import org.bedework.calfacade.configs.BasicSystemProperties;
import org.bedework.calfacade.configs.CalAddrPrefixes;

/**
 * @author douglm
 *
 */
public final class ROBasicSystemProperties implements BasicSystemProperties {
  /* Wrapper to make System properties read only. */

  private BasicSystemProperties cfg;

  private BasicSystemProperties getConfig() {
    return cfg;
  }

  /**
   * @param cfg the rw config
   */
  ROBasicSystemProperties(final BasicSystemProperties cfg) {
    this.cfg = cfg;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public void setPublicCalendarRoot(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setPublicCalendarRoot(val);
  }

  @Override
  public String getPublicCalendarRoot() {
    return getConfig().getPublicCalendarRoot();
  }

  @Override
  public void setUserCalendarRoot(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getUserCalendarRoot() {
    return getConfig().getUserCalendarRoot();
  }

  @Override
  public void setUserDefaultCalendar(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getUserDefaultCalendar() {
    return getConfig().getUserDefaultCalendar();
  }

  @Override
  public void setUserDefaultTasksCalendar(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getUserDefaultTasksCalendar() {
    return getConfig().getUserDefaultTasksCalendar();
  }

  @Override
  public void setUserDefaultPollsCalendar(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getUserDefaultPollsCalendar() {
    return getConfig().getUserDefaultPollsCalendar();
  }

  @Override
  public void setDefaultNotificationsName(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setDefaultNotificationsName(val);
  }

  @Override
  public String getDefaultNotificationsName() {
    return getConfig().getDefaultNotificationsName();
  }

  @Override
  public void setDefaultAttachmentsName(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setDefaultNotificationsName(val);
  }

  @Override
  public String getDefaultAttachmentsName() {
    return getConfig().getDefaultAttachmentsName();
  }

  @Override
  public void setDefaultReferencesName(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setDefaultReferencesName(val);
  }

  @Override
  public String getDefaultReferencesName() {
    return getConfig().getDefaultReferencesName();
  }

  @Override
  public void setUserInbox(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setUserInbox(val);
  }

  @Override
  public String getUserInbox() {
    return getConfig().getUserInbox();
  }

  @Override
  public void setUserOutbox(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setUserOutbox(val);
  }

  @Override
  public String getUserOutbox() {
    return getConfig().getUserOutbox();
  }

  @Override
  public void setIndexRoot(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setIndexRoot(val);
  }

  @Override
  public String getIndexRoot() {
    return getConfig().getIndexRoot();
  }

  @Override
  public void setGlobalResourcesPath(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setGlobalResourcesPath(val);
  }

  @Override
  public String getGlobalResourcesPath() {
    return getConfig().getGlobalResourcesPath();
  }

  @Override
  public void setCalAddrPrefixes(final CalAddrPrefixes val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public CalAddrPrefixes getCalAddrPrefixes() {
    return getConfig().getCalAddrPrefixes();
  }

  @Override
  public void setBedeworkResourceDirectory(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getBedeworkResourceDirectory() {
    return getConfig().getBedeworkResourceDirectory();
  }

  @Override
  public void setTestMode(final boolean val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public boolean getTestMode() {
    return getConfig().getTestMode();
  }

  @Override
  public String toString() {
    return getConfig().toString();
  }
}
