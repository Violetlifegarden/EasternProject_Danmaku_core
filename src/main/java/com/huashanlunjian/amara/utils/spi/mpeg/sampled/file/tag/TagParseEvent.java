/*
 * TagParseEvent.
 * 
 * jicyshout : http://sourceforge.net/projects/jicyshout/
 *  
 * JavaZOOM : mp3spi@javazoom.net
 * 			  http://www.javazoom.net
 * 
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package com.huashanlunjian.amara.utils.spi.mpeg.sampled.file.tag;

import java.util.EventObject;
/** Event to indicate that an MP3 tag was received through
    some means (parsed in stream, received via UDP, whatever)
    and converted into an MP3Tag object.
 */
public class TagParseEvent extends EventObject {
	protected MP3Tag tag;
	public TagParseEvent(Object source, MP3Tag tag) {
		super(source);
		this.tag = tag;
	}
	/** Get the tag that was parsed.
	 */
	public MP3Tag getTag() {
		return tag;
	}
}
