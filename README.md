# Swirl

...a.k.a. swrl. This is a social network site specifically for recommending movies/albums/books/websites/games
to people you know. See it here: [https://www.swrl.co/](https://www.swrl.co/)

# Development / running locally

## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

If using Cursive Clojure in IntelliJ, add a new run configuration of type `REPL - Local REPL` and choose to use leinengen.

### DB setup

Preferred option is to use Vagrant. Just install Virtual Box and Vagrant and then run `vagrant up`

Alternatively, install Postgres manually . Run init_db pointing to a folder where the data will be stored. E.g. in windows:

    D:\apps\pgsql\9.4.1-3\bin> initdb --encoding="UTF8" D:\apps\pgsql\data

Now you need to create a user and database for the project. 
First start the postgres server with `"pg_ctl" -D "D:\apps\pgsql\data" -l logfile start`
and then run `psql postgres` to open the SQL console: 

    CREATE USER dev WITH PASSWORD 'password';
    CREATE DATABASE yswrl WITH OWNER=dev ENCODING='UTF8' CONNECTION LIMIT=-1;

If that fails due to incompatible encodings, [follow these instructions](http://stackoverflow.com/a/26915078/131578)

To run postgres:

    "pg_ctl" -D "D:\apps\pgsql\data" -l logfile start

#### DB connection info

You should be able to connect from intellij or another SQL client using:

    host=localhost
    port=5432
    database=yswrl
    username=dev
    password=password

## Running

The database gets upgraded every time you run the web server.

To start a web server for the application, run:

    lein ring server

To run the scheduled jobs:

    lein run-jobs

## Client side development

You will need:
* nodejs and npm
* gulp: `npm install -g gulp`
* bower: `npm install -g bower`

To update dependencies:
`npm install`
`bower install`

To generate the CSS and JavaScript artifacts run `gulp` but during development you can auto generate files by running:

`gulp watch`

## Testing

While you can run `lein midje` from the command line, it takes many seconds to
start up and is too slow for TDD. Instead, start an interactive lein session
and just run tests from from:

    $ lein midje :autotest
    
OR if you like working in the REPL and would also like to enjoy auto-loading of namespaces you are working on as well as seeing up-to-date test runs run these commands in the REPL:

    (use 'midje.repl)
    (autotest)

Notes:
1. Ignore the errors midje throws when launching
2. Autotest works by reloading only the current namespace after the first run (so it's quick). This means it may hide failures from other namespaces, so is recommended to run a full `lein midje` before pushing.
3. Midje runs all previously written clojure.test tests, so worry not.

## Deploying

Run DB migrations remotely from your dev machine to Heroku:

    set "JDBC_DATABASE_URL=jdbc:postgresql://ec2-54-243-149-147.compute-1.amazonaws.com:5432/d9b1u6iuu53rj9?user=_____________________&password=______________________&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"
    lein ragtime migrate

Then deploy the app:

Pre-reqs:
    install the heroku cli (https://toolbelt.heroku.com/ or `brew install heroku` on mac)
    Ensure your SSH key is added to your heroku account
    Add heroku as a remote git repo: `git remote add heroku git@heroku.com:youshouldwatchreadlisten.git`
    
Deploy:

    heroku login
    git push heroku master
    
## Data

Just run the tests a few times to generate fake data.

## Updating the Chrome extension

1. Uninstall the Swirl extension if you have it already.
2. Go to extensions, enable developer mode, and add the chrome folder as an extension.
3. Once debugged, increment the version in `manifest.js`, zip the folder and upload to the Chrome store.
