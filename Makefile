# # #

GROUP_ID 	= angara
ARTEFACT  = main
MAIN      = web.main

# # #

.EXPORT_ALL_VARIABLES:
.PHONY: clean inc-major inc-minor inc-patch shapshot release
#
SHELL = bash
#
VERSION = $(shell cat VERSION)

ifndef TIMESTAMP
  TIMESTAMP = $(shell date -Isec)
endif

ifndef COMMIT
  COMMIT = $(shell git rev-parse HEAD)
endif
	
#
RESOURCES = ./resources
TARGET 		= ./target
CLASSES 	= ${TARGET}/classes
JAR_FILE  = ${TARGET}/${GROUP_ID}-${ARTEFACT}-${VERSION}.jar
UBER_JAR  = ${TARGET}/${GROUP_ID}-${ARTEFACT}.jar
BUILD_EDN = ${RESOURCES}/build.edn
APPNAME   = ${ARTEFACT}
#
REPO_ID   		= anga
SNAPSHOTS_URL = https://???/repository/maven-snapshots/
RELEASES_URL  = https://???/repository/maven-releases/ 

config: VERSION
	@echo "{">${BUILD_EDN}
	@echo ":appname \"${APPNAME}\"">>${BUILD_EDN}
	@echo ":version \"${VERSION}\"">>${BUILD_EDN}
	@echo ":commit \"${COMMIT}\"">>${BUILD_EDN}
	@echo ":timestamp \"${TIMESTAMP}\"">>${BUILD_EDN}
	@echo "}">>${BUILD_EDN}

pom: VERSION deps.edn
	@cat tools/pom-template.xml | envsubst > pom.xml
	@clojure -Spom

compile:
	mkdir -p ${CLASSES}
	clojure -e "(set! *compile-path* \"${CLASSES}\") (compile '${MAIN})"

jar: pom config compile
	clojure -A:depstar -m hf.depstar.jar ${JAR_FILE}

uberjar: clean pom config compile
	clojure -A:depstar:uberjar -m hf.depstar.uberjar ${UBER_JAR} --main ${MAIN}

# snapshot: export VERSION := ${VERSION}-SNAPSHOT
# snapshot: uberjar
# 	@mvn deploy:deploy-file 		\
# 		-DpomFile=pom.xml					\
# 		-Dfile=${UBER_JAR} 				\
# 		-DrepositoryId=${REPO_ID}	\
# 		-Durl=${SNAPSHOTS_URL}

# release: uberjar
# 	@mvn deploy:deploy-file 		\
# 		-DpomFile=pom.xml					\
# 		-Dfile=${UBER_JAR} 				\
# 		-DrepositoryId=${REPO_ID}	\
# 		-Durl=${RELEASES_URL}

inc-major:
	@(VERS=`awk -F'.' '{print $$1+1 "." 0 "." 0}' VERSION` && echo $${VERS} > VERSION)
	@cat VERSION

inc-minor:
	@(VERS=`awk -F'.' '{print $$1 "." $$2+1 "." 0}' VERSION` && echo $${VERS} > VERSION)
	@cat VERSION

inc-patch:
	@(VERS=`awk -F'.' '{print $$1 "." $$2 "." $$3+1}' VERSION` && echo $${VERS} > VERSION)
	@cat VERSION

clean:
	rm -rf ${TARGET}

#.
