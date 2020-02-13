FROM postgres:9.6

MAINTAINER Study Hsueh <ph.study@gmail.com>


COPY src /postgresql-zetasketch/src
COPY pom.xml /postgresql-zetasketch

RUN echo "===> Adding prerequisites..."                      && \
    apt-get update -y                                        && \
    DEBIAN_FRONTEND=noninteractive                              \
        apt-get install --no-install-recommends --allow-downgrades -y -q \
                git ca-certificates g++ maven                   \
                postgresql-server-dev-9.6                       \
                libpq-dev=9.6\* libpq5=9.6\* libecpg-dev        \
                libkrb5-dev libssl-dev                          \
                openjdk-8-jdk-headless                       && \
    echo "===> Building pljava..."                           && \
    export PGXS=/usr/lib/postgresql/9.6/lib/pgxs/src/makefiles/pgxs.mk && \
    git clone https://github.com/tada/pljava.git             && \
    cd pljava                                                && \
    git checkout tags/V1_5_5                                 && \
    mvn -Pwnosign clean install                              && \
    java -jar /pljava/pljava-packaging/target/pljava-pg9.6-amd64-Linux-gpp.jar && \
    \
    \
    echo "===> Building postgresql-zetasketch..."            && \
    mvn -f /postgresql-zetasketch/pom.xml clean package      && \
    \
    \
    echo "===> Clean up..."                                  && \
    apt-get -y remove --purge --auto-remove                     \
            git ca-certificates g++ maven                       \
            postgresql-server-dev-9.6 libpq-dev libecpg-dev     \
            libkrb5-dev libssl-dev                           && \
    DEBIAN_FRONTEND=noninteractive                              \
        apt-get install --no-install-recommends           -y -q \
                openjdk-8-jdk-headless                       && \
    apt-get clean                                            && \
    rm -rf ~/.m2 /var/lib/apt/lists/* /tmp/* /var/tmp/*

ADD /docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

ENTRYPOINT ["/docker-entrypoint.sh"]
EXPOSE 5432
CMD ["postgres"]