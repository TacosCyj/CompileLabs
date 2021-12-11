FROM openjdk:16
COPY . /myapp/
WORKDIR /myapp/
RUN javac -cp src/ src/Analysis.java -d dst/