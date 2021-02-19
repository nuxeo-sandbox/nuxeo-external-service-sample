FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine

RUN apk update \
    && apk add --no-cache bash \
    && mkdir /opt/extService

COPY bin /opt/extService/
COPY conf /opt/extService/

ENV CLI /opt/extService
ENV PATH="/opt/extService:${PATH}"

ENTRYPOINT [ "bin/bash","-c" ]
CMD [ "sleep infinity"]
