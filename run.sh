#!/bin/bash

export CONFIG_EDN="../conf/angara.edn"
exec java -jar angara-main.jar 2>&1 >> ../log/main.log

#.
