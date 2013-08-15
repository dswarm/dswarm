## Start with Development ##

### Prerequisites ###

- node >= 0.8 [website](http://nodejs.org)
- npm in your $PATH

You'll also need [grunt](http://gruntjs.com/) and [bower](http://bower.io/).
These would be installed by `npm install`, but it's probably better,
if you install them globally and have them in your $PATH.

    npm install -g grunt bower

Also, [yeoman](http://yeoman.io/) might be of service.


### Download Assets ###

    npm install
    bower install

Angular UI for Bootstrap needs a post install build, though:

    cd app/components/angular-ui-bootstrap
    npm install && grunt build


### Start Development Server ###

    grunt server

A browser should open at localhost:9999

