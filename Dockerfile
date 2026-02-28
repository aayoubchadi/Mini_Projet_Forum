FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy source
COPY src/main/java  src/main/java
COPY src/main/webapp src/main/webapp

# Download all required JARs into WEB-INF/lib
ADD https://jdbc.postgresql.org/download/postgresql-42.7.3.jar src/main/webapp/WEB-INF/lib/postgresql-42.7.3.jar
ADD https://repo1.maven.org/maven2/jakarta/mail/jakarta.mail-api/2.0.1/jakarta.mail-api-2.0.1.jar src/main/webapp/WEB-INF/lib/jakarta.mail-api-2.0.1.jar
ADD https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar src/main/webapp/WEB-INF/lib/jakarta.mail-2.0.1.jar
ADD https://repo1.maven.org/maven2/jakarta/activation/jakarta.activation-api/2.0.1/jakarta.activation-api-2.0.1.jar src/main/webapp/WEB-INF/lib/jakarta.activation-api-2.0.1.jar
ADD https://repo1.maven.org/maven2/com/sun/activation/jakarta.activation/2.0.1/jakarta.activation-2.0.1.jar src/main/webapp/WEB-INF/lib/jakarta.activation-2.0.1.jar
ADD https://repo1.maven.org/maven2/org/glassfish/web/jakarta.servlet.jsp.jstl/3.0.1/jakarta.servlet.jsp.jstl-3.0.1.jar src/main/webapp/WEB-INF/lib/jakarta.servlet.jsp.jstl-3.0.1.jar
ADD https://repo1.maven.org/maven2/jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api/3.0.2/jakarta.servlet.jsp.jstl-api-3.0.2.jar src/main/webapp/WEB-INF/lib/jakarta.servlet.jsp.jstl-api-3.0.2.jar

# Compile Java sources against Tomcat + the JARs in WEB-INF/lib
RUN mkdir -p build/classes \
    && TOMCAT_VER=11.0.4 \
    && curl -fsSL "https://archive.apache.org/dist/tomcat/tomcat-11/v${TOMCAT_VER}/bin/apache-tomcat-${TOMCAT_VER}.tar.gz" \
       | tar xz -C /opt \
    && TOMCAT_HOME=/opt/apache-tomcat-${TOMCAT_VER} \
    && CP=$(find ${TOMCAT_HOME}/lib src/main/webapp/WEB-INF/lib -name '*.jar' | tr '\n' ':') \
    && find src/main/java -name '*.java' > sources.txt \
    && javac -cp "$CP" -d build/classes @sources.txt

# Build WAR
RUN mkdir -p build/war/WEB-INF/classes build/war/WEB-INF/lib \
    && cp -r src/main/webapp/* build/war/ \
    && cp -r build/classes/* build/war/WEB-INF/classes/ \
    && cd build/war && jar cf /app/forum.war .

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre

ENV TOMCAT_VER=11.0.4
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* \
    && curl -fsSL "https://archive.apache.org/dist/tomcat/tomcat-11/v${TOMCAT_VER}/bin/apache-tomcat-${TOMCAT_VER}.tar.gz" \
       | tar xz -C /opt \
    && rm -rf /opt/apache-tomcat-${TOMCAT_VER}/webapps/*

ENV CATALINA_HOME=/opt/apache-tomcat-${TOMCAT_VER}
ENV PATH="${CATALINA_HOME}/bin:${PATH}"

# Deploy WAR as ROOT so app is at /
COPY --from=build /app/forum.war ${CATALINA_HOME}/webapps/ROOT.war

# Platform provides PORT env var; default 7860 for Hugging Face Spaces
ENV PORT=7860

EXPOSE ${PORT}
CMD bash -c "sed -i \"s/port=\\\"8080\\\"/port=\\\"$PORT\\\"/\" ${CATALINA_HOME}/conf/server.xml && catalina.sh run"
