# you-should-read-watch-listen

FIXME

## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

If using Cursive Clojure in IntelliJ, add a new run configuration of type `REPL - Local REPL` and choose to use leinengen.

Install Postgres. Run init_db pointing to a folder where the data will be stored. E.g. in windows:

    D:\apps\pgsql\9.4.1-3\bin> initdb --encoding="UTF8" D:\apps\pgsql\data

Now you need to create a user and database for the project. 
First start the postgres server with `"pg_ctl" -D "D:\apps\pgsql\data" -l logfile start`
and then run `psql postgres` to open the SQL console: 

    CREATE USER dev WITH PASSWORD 'password';
    CREATE DATABASE yswrl WITH OWNER=dev ENCODING='UTF8' CONNECTION LIMIT=-1;

If that fails due to incompatible encodings, [follow these instructions](http://stackoverflow.com/a/26915078/131578)

You should be able to connect from intellij or another GUI using:

    host=localhost
    port=5432
    database=yswrl
    username=dev
    password=password

## Running

To run postgres:

    "pg_ctl" -D "D:\apps\pgsql\data" -l logfile start

To update the database:

    lein ragtime migrate

Note: you can use IntelliJ as a GUI for postgres.

To start a web server for the application, run:

    lein ring server

## Testing

While you can run `lein test` from the command line, it takes many seconds to
start up and is too slow for TDD. Instead, start an interactive lein session
and just run tests from from:

    $ lein test-refresh
    

## Deploying

Run DB migrations remotely from your dev machine to Heroku:

    set "JDBC_DATABASE_URL=jdbc:postgresql://ec2-107-20-159-103.compute-1.amazonaws.com:5432/dekbme81p64129?user=_________________&password=___________________&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"
    lein ragtime migrate

Then deploy the app:

    git push heroku master
    
## Data
In fake_data.clj there are methods to create 10 users and 30 swirls.  This assumes you have a blank database currently (due to assuming the user IDs are from 0).  This can easily be improved and shall be in later iterations.
