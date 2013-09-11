## Start with Development ##

### Prerequisites ###

- node >= 0.8 [website](http://nodejs.org)
- npm in your $PATH

You'll also need [grunt](http://gruntjs.com/) and [bower](http://bower.io/).
These would be installed by `npm install`, but it's probably better,
if you install them globally and have them in your $PATH.

    npm install -g grunt bower

Also, [yeoman](http://yeoman.io/) might be of service.

Every command block assumes, that the current working directory is /yo/.
Also, the commands are written for bash or a similar shell, you'll need
to adjust them if you work on other systems, e.g. Windows.


### Download Assets ###

    npm install
    bower install

Some components need an additional build step:

    cd app/components/angular-ui-bootstrap
    npm install && grunt build

as well as

    cd app/components/angular-ui-utils
    npm install && grunt build

You'all also need jsPlumb, which isn't available through bower ([issue](https://github.com/sporritt/jsPlumb/issues/53))
So, you have to download the github archive:

    cd app/components
    wget -qO- https://github.com/sporritt/jsPlumb/archive/1.5.1.tar.gz | tar xvz -
    mv jsPlumb-1.5.1 jsPlumb


### Start Development Server ###

    grunt server

A browser should open at localhost:9999


### Run the tests ###

the test runner for the unit tests is [karma](http://karma-runner.github.io/).
As with bower/grunt, you can have it installed globally or locally through `npm install`
Either way, start a single unit test run:

    karma start --single-run

This should output something like

> INFO [karma]: Karma v0.10.2 server started at http://localhost:9876/
> INFO [launcher]: Starting browser Chrome
> INFO [Chrome 29.0.1547 (Mac OS X 10.8.4)]: Connected on socket 4aWTmAM_7jePLA3clBgw
> Chrome 29.0.1547 (Mac OS X 10.8.4): Executed 31 of 31 SUCCESS (0.87 secs / 0.14 secs)

Alternatively, you can use grunt to do the same:

    grunt karma:unit

Karma can be run in a server mode, where tests are re-run on every file change:

    grunt karma:continuous
    # or
    karma start --auto-watch


### Run the linter ###

Linting is done by [jshint](http://jshint.com/). It can be run via grunt:

    grunt jshint

A good idea is to incorporate jshint within your favorite IDE: [jshint plugins](http://jshint.com/install/#plugins)
For example, the IDEA plugin highlights jshint issues directly as syntax errors in the IDE

