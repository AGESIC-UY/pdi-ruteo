PROFILE=$1
if [[ -z "$PROFILE" ]]; then
        PROFILE=$SPRING_PROFILES_ACTIVE
fi

JAVA_OPTS="-Xmx1024m -Xms1024m"
java $JAVA_OPTS -jar -Dspring.profiles.active=$PROFILE /opt/pdi/router-service.jar

