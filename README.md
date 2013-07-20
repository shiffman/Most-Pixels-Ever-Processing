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
| S,#               | S,0                     |  Synchronous Client connecting, ID # | 
| A,#,boolean       | A,0,true                |  ASychronous Client connecting, ID #, messages back yes or no? |
| D,#               | D,231                   |  Client done rendering |
| T,String          | T,rain,82               |  Data message sent to all client |
| T,String&#124;#,#      | T,rain,82&#124;0,1           |  Data message, which clients to send to |
| P                 | P                       |  Toggle pause state |

## Server

| Message           | Example                 |  Description           |
| ----------------- | ----------------------- |  --------------------  | 
| G,#               | G,231                   |  Go and draw frame 231 | 
| G,#&#124;#,String&#124;#,String | G,231&#124;rain,0,82&#124;3,snow,42   |  Go and draw frame 231 with these messages, messages are preceded by client ID that sent them | 
| R                 | R                       |  Reset to frame 0 |
| P                 | P                       |  Toggle pause state |

 


