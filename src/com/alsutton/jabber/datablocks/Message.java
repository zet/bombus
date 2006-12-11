/*
  Copyright (c) 2000, Al Sutton (al@alsutton.com)
  All rights reserved.
  Redistribution and use in source and binary forms, with or without modification, are permitted
  provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice, this list of conditions
  and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright notice, this list of
  conditions and the following disclaimer in the documentation and/or other materials provided with
  the distribution.

  Neither the name of Al Sutton nor the names of its contributors may be used to endorse or promote
  products derived from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.alsutton.jabber.datablocks;
import com.alsutton.jabber.*;
import java.util.*;

/**
 * Title:        Message.java
 * Description:  The class representing a Jabber message object
 */

public class Message extends JabberDataBlock
{
  /**
   * Constructor. Prepares the message destination and body
   *
   * @param to The destination of the message
   * @param message The message text
   */

  public Message( String to, String message , String subject, boolean groupchat)
  {
    super();

    setAttribute( "to", to );
    if( message != null )
      setBodyText( message );
    if (subject!=null) 
        setSubject(subject);
    setTypeAttribute((groupchat)?"groupchat":"chat");
  }

  /**
   * Constructor. Prepares the message destination
   *
   * @param to The destination of the message
   */

  public Message( String to )
  {
      super();
    setAttribute( "to", to );
  }

  /**
   * Default Constructor. Alls for construction of an empty message template.
   */

  public Message()
  {
    this( null );
  }

  /**
   * Constructor for incomming messages
   *
   * @param _parent The parent of this datablock
   * @param _attributes The list of element attributes
   */

  public Message( JabberDataBlock _parent, Hashtable _attributes )
  {
    super( _parent, _attributes );
  }

  /**
   * Method to set the body text. Creates a block with body as it's tag name
   * and inserts the text into it.
   *
   * @param bodyText The string to go in the message body
   */

  public void setBodyText( String text )
  {
    addChild( "body", text );
  }

  /**
   * Method to set the body text written in HTML. Creates a block with html as
   * it's tag name in the xhtml name space and inserts the html into it.
   *
   * @param html The html to go in the message body
   */

  /*
  public void setHTMLBodyText( String html )
  {
    JabberDataBlock body = new JabberDataBlock( "html", null, null );
    body.setNameSpace( "http://www.w3.org/1999/xhtml" );
    body.addText( html );
    addChild( body );
  }
   */

  /**
   * Method to set the message thread. Creates a block with thread as it's tag
   * name and inserts the thread name into it.
   *
   * @param threadName The string to go in the thread block
   */

  /*public void setThread( String text )
  {
    JabberDataBlock thread = new JabberDataBlock( "thread", null, null );
    thread.addText( text );
    addChild( thread );
  }*/

  /**
   * Method to set the subject text. Creates a subject block and inserts the text into it.
   *
   * @param text The string to go in the message subject
   */

  public void setSubject( String text ) { addChild( "subject", text ); }


  /**
   * Method to get the message subject
   *
   * @return A string representing the message subject
   */

  public String getSubject() {  return getChildBlockText( "subject" );  }

  /**
   * Method to get the message body
   *
   * @return The message body as a string
   */

  public String getBody() { 
      String body=getChildBlockText( "body" ); 
      
      JabberDataBlock error=getChildBlock("error");
      if (error==null) return body;
      return body+"Error\n"+error.toString();
  }
  
  
  public String getOOB() {
      JabberDataBlock oobData=findNamespace("jabber:x:oob");
      StringBuffer oob=new StringBuffer();
      try {
          oob.append("\n");
          oob.append(oobData.getChildBlockText("desc"));
          if (oob.length()>1) oob.append(" ");
          oob.append("( ");
          oob.append(oobData.getChildBlockText("url"));
          oob.append(" )");
      } catch (Exception ex) { return null; }
  
      return oob.toString();
  }

  public String getTimeStamp(){
      JabberDataBlock stamp=findNamespace("jabber:x:delay");
      return (stamp!=null)? stamp.getAttribute("stamp") :null;
  }
  /**
   * Construct a reply message
   *
   * @return A message object destined for the sender of this message with no subject or body
   */

   /*
  public Message constructReply()
  {
    if( attributes == null )
      return null;

    String to = (String) attributes.get( "from" );
    if( to == null )
      return null;

    Message reply = new Message( to );

    String from = (String) attributes.get( "to" );
    if( from != null )
      reply.setAttribute( "from", from );

    String messageType = getAttribute( "type" );
    reply.setAttribute( "type", messageType );

    String thread = getTextForChildBlock( "thread" );
    if( thread != null && thread.length() > 0 )
    {
      setThread( thread );
    }

    String id = getAttribute( "id" );
    if( id != null && id.length() > 0 )
    {
      setAttribute( "id", id );
    }

    return reply;
  }
    */

  /**
   * Get the tag start marker
   *
   * @return The block start tag
   */

  public String getTagName()
  {
    return "message";
  }

  /**
     * Method to get the message from field
     * @return <B>from</B> field as a string
     */
    public String getFrom() {
	//try {
	//    // jep-0146
	//    JabberDataBlock fwd=findNamespace("jabber:x:forward");
	//    JabberDataBlock from=fwd.getChildBlock("from");
	//    return from.getAttribute("jid");
	//} catch (Exception ex) { /* normal case if not forwarded message */ };
	
	try {
	    // jep-0033 extended stanza addressing from psi
	    JabberDataBlock addresses=getChildBlock("addresses");
	    for (Enumeration e=addresses.getChildBlocks().elements(); e.hasMoreElements(); ) {
		JabberDataBlock adr=(JabberDataBlock) e.nextElement();
		if (adr.getTypeAttribute().equals("ofrom")) return adr.getAttribute("jid");
	    }
	} catch (Exception e) { /* normal case if not forwarded message */ };
	
        return (String) attributes.get( "from" );
    }
}
