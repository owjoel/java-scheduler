CORE_INSTALL_STAMP := omnicron-core/target/.core-installed
CORE_SOURCES := $(shell find omnicron-core/src/main/java omnicron-core/src/main/resources -type f 2>/dev/null)
CORE_SOURCES += omnicron-core/pom.xml pom.xml

.PHONY: install-api build-api run-api run-api-local run-worker-local

$(CORE_INSTALL_STAMP): $(CORE_SOURCES)
	./mvnw -pl omnicron-core install
	touch $(CORE_INSTALL_STAMP)

install-api:
	./mvnw -pl omnicron-api -am install

build-api:
	./mvnw -pl omnicron-api -am clean install

run-api: $(CORE_INSTALL_STAMP)
	./mvnw -pl omnicron-api spring-boot:run

run-api-local: $(CORE_INSTALL_STAMP)
	./mvnw -pl omnicron-api spring-boot:run -Dspring-boot.run.profiles=local

run-worker-local: $(CORE_INSTALL_STAMP)
	./mvnw -pl omnicron-worker spring-boot:run -Dspring-boot.run.profiles=local
