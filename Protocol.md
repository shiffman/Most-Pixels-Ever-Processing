# Most Pixels Ever 2.0 Protocol


## Client


| Message       | Example       |  Description    |
| ------------- |:-------------:|  :-------------:| 
| S,#           | S,0           |  Synchronous Client connecting, ID # | 
| A,#,boolean   | A,0,true      |  ASychronous Client connecting, ID #, messages back yes or no? |
| D,#           | D,231         |  Client done rendering |
| T,String      | T,temp=42     |  Data message sent to all client |
| T,String|#,#  | T,temp=42|0,1 |  Data message, which clients to send to |

## Server

| Message           | Example                 |  Description           |
| ----------------- | ----------------------- |  --------------------  | 
| G,#               | G,231                   |  Go and draw frame 231 | 
| G,#|String|String | G,231|message0|message1 |  Go and draw frame 231 with these messages | 
| R,0               | R,0                     |  Reset to frame 0 |

 
