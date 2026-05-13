CORE_JAR := omnicron-core/target/omnicron-core-0.0.1-SNAPSHOT.jar
CORE_SOURCES := $(shell find omnicron-core/src/main/java omnicron-core/src/main/resources -type f 2>/dev/null)
CORE_SOURCES += omnicron-core/pom.xml pom.xml

.PHONY: install-api build-api run-api run-api-local

$(CORE_JAR): $(CORE_SOURCES)
	./mvnw -pl omnicron-core install

install-api:
	./mvnw -pl omnicron-api -am install

build-api:
	./mvnw -pl omnicron-api -am clean install

run-api: $(CORE_JAR)
	./mvnw -pl omnicron-api spring-boot:run

run-api-local: $(CORE_JAR)
	./mvnw -pl omnicron-api spring-boot:run -Dspring-boot.run.profiles=local
