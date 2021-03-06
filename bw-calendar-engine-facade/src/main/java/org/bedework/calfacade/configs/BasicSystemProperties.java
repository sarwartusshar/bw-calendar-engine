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
package org.bedework.calfacade.configs;

import org.bedework.util.config.ConfInfo;
import org.bedework.util.jmx.MBeanInfo;

import java.io.Serializable;

/** Provides access to some of the basic configuration for the system. Most of
 * these values should not be changed. While their values do leak out into the
 * user interface they are intended to be independent of any language.
 *
 * <p>Display names should be used to localize the names of collections and paths.
 *
 * @author Mike Douglass
 */
@ConfInfo(elementName = "basic-properties")
public interface BasicSystemProperties extends Serializable {
  /** Before 4.0 internal paths did not end with "/". 4.0 and above they will
   */
  boolean colPathEndsWithSlash = false;

  // TODO - make all these static strings

  /** Set the public Calendar Root
   *
   * @param val    String
   */
  void setPublicCalendarRoot(String val);

  /** Get the publicCalendarRoot
   *
   * @return String   publicCalendarRoot
   */
  @MBeanInfo("public Calendar Root - do not change")
  String getPublicCalendarRoot();

  /** Set the user Calendar Root
   *
   * @param val    String
   */
  void setUserCalendarRoot(String val);

  /** Get the userCalendarRoot
   *
   * @return String   userCalendarRoot
   */
  @MBeanInfo("user Calendar Root - do not change")
  String getUserCalendarRoot();

  /** Set the user default events calendar
   *
   * @param val    String
   */
  void setUserDefaultCalendar(String val);

  /** Get the user default events calendar
   *
   * @return String   userDefaultCalendar
   */
  @MBeanInfo("user default events calendar - do not change")
  String getUserDefaultCalendar();

  /** Set the user default tasks calendar
   *
   * @param val    String
   */
  void setUserDefaultTasksCalendar(String val);

  /** Get the user default tasks calendar
   *
   * @return String
   */
  @MBeanInfo("user default tasks calendar - do not change")
  String getUserDefaultTasksCalendar();

  /** Set the user default polls calendar
   *
   * @param val    String
   */
  void setUserDefaultPollsCalendar(String val);

  /** Get the user default polls calendar
   *
   * @return String
   */
  @MBeanInfo("user default polls calendar - do not change")
  String getUserDefaultPollsCalendar();

  /** Set the default notifications collection name
   *
   * @param val
   */
  void setDefaultNotificationsName(String val);

  /** Get the default notifications name
   *
   * @return name
   */
  @MBeanInfo("name of notifications collection - do not change")
  String getDefaultNotificationsName();

  /** Set the default managed attachments collection name
   *
   * @param val
   */
  void setDefaultAttachmentsName(String val);

  /** Get the default attachments name
   *
   * @return name
   */
  @MBeanInfo("name of attachments collection - do not change")
  String getDefaultAttachmentsName();

  /** Set the defaultReferencesName
   *
   * @param val
   */
  void setDefaultReferencesName(String val);

  /** Get the defaultReferencesName
   *
   * @return flag
   */
  @MBeanInfo("name of default references collection - do not change")
  String getDefaultReferencesName();

  /** Set the user inbox name
   *
   * @param val    String
   */
  void setUserInbox(String val);

  /** Get the user inbox name
   *
   * @return String   user inbox
   */
  @MBeanInfo("user inbox name - do not change")
  String getUserInbox();

  /** Set the user outbox
   *
   * @param val    String
   */
  void setUserOutbox(String val);

  /** Get the user outbox
   *
   * @return String   user outbox
   */
  @MBeanInfo("user outbox - do not change")
  String getUserOutbox();

  /** Set the path to the root for indexes
   *
   * @param val    String index root
   */
  void setIndexRoot(String val);

  /** Get the path to the root for indexes
   *
   * @return String
   */
  @MBeanInfo("path to the root for non-solr indexes")
  String getIndexRoot();

  /** Set the global resources path
   *
   * @param val
   */
  void setGlobalResourcesPath(String val);

  /** Get the global resources path
   *
   * @return token
   */
  @MBeanInfo("The global resources path")
  String getGlobalResourcesPath();

  /** To enable mapping of calendar addresses e.g. mailto:fred@example.org
   *  on to principals we need to either do a directory lookup or have
   *  some sort of pattern map.
   *
   * <p>Setting a caladdr prefix enables pattern mapping. By default
   * calendar addresses are users
   *
   * <pre>
   * &lt;caladdrPrefixes classname="org.bedework.calfacade.configs.CalAddrPrefixes">
   *   &lt;!--
   *        This would specify that any calendar user address starting with loc_
   *        is to be treated as a location, e.g. we might get
   *            mailto:loc_vcc315@example.org
   *      &lt;location>loc_&lt;/location>
   *       -->
   *    &lt;/caladdrPrefixes>
   * </pre>
   *
   * @param val
   */
  void setCalAddrPrefixes(CalAddrPrefixes val);

  /**
   * @return CalAddrPrefixes or null
   */
  @MBeanInfo("Calendar address prefixes")
  CalAddrPrefixes getCalAddrPrefixes();

  /** This directory allows us to hide away bedework specific resources
   * such as views and categories. We can hide this directory from
   * generic caldav and other protocols.
   *
   * @param val
   */
  void setBedeworkResourceDirectory(String val);

  /**
   * @return CalAddrPrefixes or null
   */
  @MBeanInfo("Directory for bedework resources")
  String getBedeworkResourceDirectory();

  /** True to run in test mode. This will adversely affect performance
   * but avoids some test failures due to asynchronous behavior.
   *
   * <p>DO NOT run in production mode with this on</p>.
   *
   * @param val
   */
  void setTestMode(boolean val);

  /**
   * @return CalAddrPrefixes or null
   */
  @MBeanInfo("True to run in test mode. This will adversely affect performance\n" +
                     "   * but avoids some test failures due to asynchronous behavior.\n" +
                     "   *\n" +
                     "   * <p>DO NOT run in production mode with this on")
  boolean getTestMode();
}
