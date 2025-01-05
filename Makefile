# # #

PROD_HOST	=	angara
PROD_PATH	=	/app/main

# # #

.EXPORT_ALL_VARIABLES:
.PHONY: clean css jar uberjar build

SHELL = bash
	
UBER_JAR  = ${TARGET}/angara-main.jar


dev:
	set -a && CONFIG_EDN=../conf/dev.edn && source ../conf/dev.env && clojure -M:dev:nrepl


build: clean
	@clj -T:build uberjar


deploy:
	chmod g+r ${UBER_JAR}
	scp ${UBER_JAR} ${PROD_HOST}:${PROD_PATH}


clean:
	clj -T:build clean


outdated:
	@(clojure -Sdeps '{:deps {antq/antq {:mvn/version "RELEASE"}}}' -T antq.core/-main || exit 0)

#.
