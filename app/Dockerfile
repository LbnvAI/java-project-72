FROM gradle:8.10.0-jdk21

WORKDIR /app

COPY /app .

RUN gradle installDist

CMD ./build/install/app/bin/app

ENV PORT=7070

ENV JAVA_OPTS "-Xmx512M -Xms512M"