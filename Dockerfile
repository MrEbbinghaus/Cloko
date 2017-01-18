FROM clojure:lein-2.7.1
MAINTAINER Björn Ebbinghaus <bjoern@ebbinghaus.me>
# Add sources for nodejs
RUN apt-get update -qq && \
    apt-get install -yqq rubygems nodejs && \
    yes | gem install sass && \
    npm install phantomjs-prebuilt -g && \
    mkdir ./cloko
WORKDIR /cloko
ADD . /cloko
RUN lein deps > /dev/null 2>&1