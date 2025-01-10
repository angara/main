#!/bin/bash

# export SID="16206ced09f.xxxxxxxxxxxx"

TID=101015
API="http://localhost:8001/forum/api"
#API="https://angara.net/forum/api"

# curl -b sid=$SID ${API}/topic/watch?tid=$TID
# curl -b sid=$SID -X POST -d watch=1 ${API}/topic/watch?tid=$TID
# curl -b sid=$SID ${API}/topic/watch?tid=$TID

http ${API}/topic/watch Cookie:sid=${SID} tid==${TID}
http ${API}/topic/watch Cookie:sid=${SID} tid==${TID} watch=1
http ${API}/topic/watch Cookie:sid=${SID} tid==${TID}
http ${API}/topic/watch Cookie:sid=${SID} tid==${TID} watch=0
http ${API}/topic/watch Cookie:sid=${SID} tid==${TID}
