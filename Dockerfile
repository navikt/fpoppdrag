FROM ghcr.io/navikt/fp-baseimages/chainguard:jre-27
LABEL org.opencontainers.image.source=https://github.com/navikt/fpoppdrag

# Config
COPY target/classes/logback*.xml ./conf/

# Application Container (Jetty)
COPY target/lib/*.jar ./lib/
COPY target/app.jar .
