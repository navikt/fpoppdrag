FROM navikt/java:11

RUN mkdir /app/lib
RUN mkdir /app/webapp
RUN mkdir /app/conf

# AppDynamics config
#COPY appdynamics.sh /init-scripts/
#COPY appdynamics/ /app/klient/appdynamics/

# Config
COPY web/webapp/target/classes/logback.xml /app/conf/
COPY web/webapp/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Container (Jetty)
COPY web/webapp/target/app.jar /app/
COPY web/webapp/target/lib/*.jar /app/lib/

# Application Start Command
COPY run-java.sh /
RUN chmod +x /run-java.sh