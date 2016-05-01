#!/bin/bash


# /etc/init/ws-main.conf:

# description   "angara-main service"

# start on runlevel [2]
# stop on runlevel [!2]

# respawn
# respawn limit 0 10

# script
#   sleep 1
#   cd /www/main
#   exec /bin/su anga -c "./run.sh"
# end script

exec java -jar main.jar var/prod.edn >> /var/log/angara/main-out.log 2>&1

#.
