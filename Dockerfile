FROM navikt/java:11-appdynamics
ENV APPD_ENABLED=true
ENV APP_NAME=fpoppdrag
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=appdynamics.adeo.no
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
ENV TZ=Europe/Oslo

# lag en gruppe og en sysembruker (-r) uten passord, uten hjemme-katalog, uten shell.
RUN groupadd -r applikasjon && useradd -r -s /bin/false -g applikasjon applikasjon


RUN mkdir /app/lib
RUN mkdir /app/webapp
RUN mkdir /app/conf

# Config
COPY web/webapp/target/classes/logback.xml /app/conf/
COPY web/webapp/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Container (Jetty)
COPY web/webapp/target/app.jar /app/
COPY web/webapp/target/lib/*.jar /app/lib/

# Application Start Command
COPY run-java.sh /
RUN chmod +x /run-java.sh

# Export vault properties
COPY export-vault.sh /init-scripts/export-vault.sh

USER applikasjon
