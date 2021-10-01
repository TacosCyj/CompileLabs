FROM gcc:10
WORKDIR /app/
COPY submitTest.c ./
RUN gcc submitTest.c -o submitTest
RUN chmod +x submitTest