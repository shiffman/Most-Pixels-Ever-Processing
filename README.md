# Most Pixels Ever

Most Pixels Ever is an open-source framework for spanning graphics applications across multiple screens.

![Screenshot](http://farm3.static.flickr.com/2199/2124879919_6a8e447903_m.jpg)  ![Screenshot](http://farm3.static.flickr.com/2201/2125653100_1954bd6189_m.jpg)  ![Screenshot](http://farm3.static.flickr.com/2190/2124878313_c302b6aac7_m.jpg)

# Getting Started:

Check out the tutorials on the wiki!

[https://github.com/shiffman/Most-Pixels-Ever/wiki](https://github.com/shiffman/Most-Pixels-Ever/wiki)

# Supported environments

* [Processing]()
* [openFrameworks](https://github.com/obviousjim/ofxMostPixelsEver)
* [Cinder](https://github.com/wdlindmeier/Most-Pixels-Ever-Cinder)

# Most Pixels Ever 2.0 Protocol

## Client


| Message           | Example                 |  Description           |
| ----------------- | ----------------------- |  --------------------  | 
| S&#124;#               | S&#124;0                     |  Synchronous Client connecting &#124; ID # | 
| A&#124;#&#124;boolean  | A&#124;0&#124;true           |  ASychronous Client connecting &#124; ID # &#124; messages back yes or no? |
| D&#124;#&#124;#        | D&#124;0&#124;231            |  Client done rendering, ID, frame number |
| T&#124;String          | T&#124;rain,82               |  Data message sent to all client |
| T&#124;String&#124;#,#      | T&#124;rain,82&#124;0,1           |  Data message &#124; which clients to send to |
| P                 | P                       |  Toggle pause state |

## Server

| Message           | Example                 |  Description           |
| ----------------- | ----------------------- |  --------------------  | 
| G&#124;#               | G&#124;231                   |  Go and draw frame 231 | 
| G&#124;#&#124;#,String&#124;#,String | G&#124;231&#124;rain,0,82&#124;3,snow,42   |  Go and draw frame 231 with these messages, messages are preceded by client ID that sent them | 
| R                 | R                       |  Reset to frame 0 |
| P                 | P                       |  Toggle pause state |

 


