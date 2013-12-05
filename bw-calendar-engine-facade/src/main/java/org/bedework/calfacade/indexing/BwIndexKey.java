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
package org.bedework.calfacade.indexing;

import org.bedework.util.indexing.Index;
import org.bedework.util.indexing.IndexException;

import java.io.CharArrayWriter;

/**
 * @author Mike Douglass douglm @ rpi.edu
 *
 */
public class BwIndexKey extends Index.Key {
  private String key1; // calendar:path
  private String key2; // event:guid
  private String key3; // event:recurrenceid

  /** An event key is stored as a concatenated set of Strings,
   * calendar:path + guid + (recurrenceid | null).
   *
   * <p>We set it here and use the decode methods to split it up.
   */
  private char[] encoded;

  private String itemType;

  /** Constructor
   *
   */
  public BwIndexKey() {
  }

  /** Constructor for a collection key
   *
   * @param path
   */
  public BwIndexKey(final String path) {
    itemType = BwIndexer.docTypeCollection;
    key1 = path;
  }

  /** Constructor for an event key
   *
   * @param itemType
   * @param path
   * @param uid
   * @param rid
   */
  public BwIndexKey(final String itemType,
                    final String path, final String uid, final String rid) {
    this.itemType = itemType;
    key1 = path;
    key2 = uid;
    key3 = rid;
  }

  /** Constructor
   *
   * @param score
   */
  public BwIndexKey(final float score) {
    this.score = score;
  }

  /** Set the item type with a value defined in BwIndexDefs
   *
   * @param val
   */
  public void setItemType(final String val) {
    itemType = val;
  }

  /**
   * @return String item type as defined in BwIndexDefs
   */
  public String getItemType() {
    return itemType;
  }

  /** Set the score
   *
   * @param val
   */
  public void setScore(final float val) {
    score = val;
  }

  /** Set the key for a calendar (the path) or category (the uid)
   *
   * @param key1
   */
  public void setKey1(final String key1) {
    this.key1 = key1;
  }

  /** Set the key for an event (encode ca+guid+recurid)
   *
   * @param key
   * @throws IndexException
   */
  public void setEventKey(final String key) throws IndexException {
    encoded = key.toCharArray();
    pos = 0;

    key1 = getKeyString();
    key2 = getKeyString();
    key3 = getKeyString();
  }

  /**
   * @return String key value.
   * @throws IndexException
   */
  public String getKey() throws IndexException {
    if (itemType.equals(BwIndexer.docTypeCollection)) {
      return key1;  // Path
    }

    if (itemType.equals(BwIndexer.docTypeCategory)) {
      return makeCategoryKey(key1);
    }

    return makeEventKey(key1, key2, key3);
  }

  public String makeCategoryKey(final String key1) {
    // Key is just the uid
    return key1;
  }

  public String makeContactKey(final String key1) {
    // Key is just the uid
    return key1;
  }

  public String makeLocationKey(final String key1) {
    // Key is just the uid
    return key1;
  }

  /** Encode an event key
   *
   * @param key1
   * @param key2
   * @param key3
   * @return Strign encoded key
   * @throws IndexException
   */
  public String makeEventKey(final String key1, final String key2,
                             final String key3) throws IndexException {
    startEncoding();
    encodeString(key1);
    encodeString(key2);
    encodeString(key3);

    return getEncodedKey();
  }

  /** Will return either a BwCalendar or a Collection of EventInfo.
   *
   */
  @Override
  public Object getRecord() throws IndexException {
    throw new RuntimeException("org.bedework.wrong.method");
  }

  /* ====================================================================
   *                 Key decoding methods
   * ==================================================================== */

  /* Current position in the key */
  private int pos;

  /* When encoding a key we build it here.
   */
  private CharArrayWriter caw;

  /** Get next char from encoded value. Return < 0 for no more
   *
   * @return char value
   */
  private char getChar() {
    if ((encoded == null) || (pos == encoded.length)) {
      return (char)-1;
    }

    char c = encoded[pos];
    pos++;

    return c;
  }

  /** Back off one char
   *
   * @throws IndexException
   */
  private void back() throws IndexException {
    back(1);
  }

  /** Back off n chars
   *
   * @param n   int number of chars
   * @throws IndexException
   */
  private void back(final int n) throws IndexException {
    if ((pos - n) < 0) {
      throw new IndexException("org.bedework.index.badKeyRewind");
    }

    pos -= n;
  }

  /** Return the value of a blank terminated length. On success current pos
   * has been incremented.
   *
   * @return int length
   * @throws IndexException
   */
  private int getLength() throws IndexException {
    int res = 0;

    for (;;) {
      char c = getChar();
      if (c == ' ') {
        break;
      }

      if (c < 0) {
        throw new IndexException("org.bedework.index.badKeyLength");
      }

      if ((c < '0') || (c > '9')) {
        throw new IndexException("org.bedework.index.badkeychar");
      }

      res = (res * 10) + Character.digit(c, 10);
    }

    return res;
  }

  /** Get a String from the encoded acl at the current position.
   *
   * @return String decoded String value
   * @throws IndexException
   */
  private String getKeyString() throws IndexException {
    if (getChar() == 'N') {
      return null;
    }
    back();
    int len = getLength();

    if ((encoded.length - pos) < len) {
      throw new IndexException("org.bedework.index.badKeyLength");
    }

    String s = new String(encoded, pos, len);
    pos += len;

    return s;
  }

  /* * Skip a String from the encoded acl at the current position.
   *
   * @throws IndexException
   * /
  public void skipString() throws IndexException {
    if (getChar() == 'N') {
      return;
    }

    back();
    int len = getLength();
    pos += len;
  }*/

  /* ====================================================================
   *                 Encoding methods
   * ==================================================================== */

  /** Get ready to encode
   *
   */
  private void startEncoding() {
    caw = new CharArrayWriter();
  }

  /** Encode a blank terminated 0 prefixed length.
   *
   * @param len
   * @throws IndexException
   */
  private void encodeLength(final int len) throws IndexException {
    try {
      String slen = String.valueOf(len);
      caw.write('0');
      caw.write(slen, 0, slen.length());
      caw.write(' ');
    } catch (Throwable t) {
      throw new IndexException(t);
    }
  }

  /** Encode a String with length prefix. String is encoded as <ul>
   * <li>One byte 'N' for null string or</li>
   * <li>length {@link #encodeLength(int)} followed by</li>
   * <li>String value.</li>
   * </ul>
   *
   * @param val
   * @throws IndexException
   */
  public void encodeString(final String val) throws IndexException {
    try {
      if (val == null) {
        caw.write('N'); // flag null
      } else {
        encodeLength(val.length());
        caw.write(val, 0, val.length());
      }
    } catch (IndexException ie) {
      throw ie;
    } catch (Throwable t) {
      throw new IndexException(t);
    }
  }

  /** Add a character
   *
   * @param c char
   * @throws IndexException
   */
  public void addChar(final char c) throws IndexException {
    try {
      caw.write(c);
    } catch (Throwable t) {
      throw new IndexException(t);
    }
  }

  /** Get the current encoed value
   *
   * @return char[] encoded value
   */
  public String getEncodedKey() {
    return new String(caw.toCharArray());
  }
}