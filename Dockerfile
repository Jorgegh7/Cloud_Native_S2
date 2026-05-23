FROM ubuntu:latest
LABEL authors="jorge"

ENTRYPOINT ["top", "-b"]