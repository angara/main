#!/bin/bash


# /etc/init/ws-usr.conf:

# description   "angara-usr service"

# start on runlevel [2]
# stop on runlevel [!2]

# respawn
# respawn limit 0 10

# script
#   sleep 1
#   cd /www/usr
#   exec /bin/su anga -c "./run.sh"
# end script

exec java -jar angara-usr.jar var/prod.edn >> /var/log/angara/usr-out.log 2>&1

#.
