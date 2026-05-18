FROM tomcat:9.0.30-jdk8-openjdk-slim

ADD ./target/outertuning.war /usr/local/tomcat/webapps/
ADD ./docker-entrypoint.sh /docker-entrypoint.sh

RUN cd /usr/local/tomcat/webapps/ && rm -rf ROOT.war ROOT/ && mv outertuning.war ROOT.war
RUN echo 'export CATALINA_OPTS="$CATALINA_OPTS -Xms1024m -Xmx4096m -XX:MaxPermSize=1024m -XX:+UseParallelGC"' > /usr/local/tomcat/bin/setenv.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8080

CMD ["/docker-entrypoint.sh"]