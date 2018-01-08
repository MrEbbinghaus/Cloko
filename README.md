# CloKo - Clojure Konquest! #

[![GitHub version](https://badge.fury.io/gh/MrOerni%2FCloko.svg)](https://badge.fury.io/gh/MrOerni%2FCloko)
[![Build Status](https://travis-ci.com/MrOerni/Cloko.svg?token=Rnk3s5asaiAz4eG6YMXs&branch=master)](https://travis-ci.com/MrOerni/Cloko)
[![Code Climate](https://codeclimate.com/github/MrOerni/Cloko/badges/gpa.svg)](https://codeclimate.com/github/MrOerni/Cloko)

### What is this repository for? ###
* A Konquest clone written in ClojureScript!
* It is a project work for the course _functional programming_ from the Heinrich-Heine-University in the winter term 2016/17
* Version: `1.2.0`

### How do I set up? ###
* Make sure you installed leinigen.
* Set up the development enviroment with `lein figwheel`.
* Visit `http://0.0.0.0:3449` to use connect the repl or play with the UI.
* To directly call the core functions, call `(in-ns 'cloko.core)` first.

#### Compile sass ###
* Install sass
* Run `sass --watch resources/public/css/styles.sass:resources/public/css/styles.css`

#### How t run tests ####
* Requirements: `phantomjs`
* Simply run: `lein test`
* If you want to use live reloading tests run the development enviroment with: `lein figwheel devcard-test dev` and visit `http://0.0.0.0:3449/tests`

### Deployment instructions ###
* Run `lein cljsbuild once min` to compile to a minified js file.
* Point your webserver to serve files from `resources/public`


### Who do I talk to? ###
* Björn Ebbinghaus ([bjoern@ebbinghaus.me](mailto:bjoern@ebbinghaus.me))
