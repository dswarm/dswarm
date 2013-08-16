module.exports = function(config) {
    config.set({
        // Karma configuration

        // base path, that will be used to resolve files and exclude
        basePath : 'app/',

        // list of files / patterns to load in the browser
        files : [
          'components/angular/angular.js',
          'components/angular-mocks/angular-mocks.js',
          'components/angular-ui-bootstrap/misc/test-lib/angular-mocks.js',
          'components/angular-resource/angular-resource.js',
          'components/angular-cookies/angular-cookies.js',
          'components/angular-sanitize/angular-sanitize.js',
          'components/angular-ui-router/release/angular-ui-router.js',
          'scripts/*.js',
          'scripts/**/*.js',

          // templates
          'views/directives/**/*.html',

          '../test/mock/**/*.js',
          '../test/spec/**/*.js'
        ],

        // list of files to exclude
        exclude : [],

        // test results reporter to use
        // possible values: dots || progress || growl
        //reporters : ['progress'],

        // web server port
        //port : 8080,

        // cli runner port
        //runnerPort : 9100,
        
        // enable / disable colors in the output (reporters and logs)
        colors : true,

        // level of logging
        // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
        //logLevel : LOG_DEBUG,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch : true,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers : ['Chrome'],

        // If browser does not capture in given timeout [ms], kill it
        captureTimeout : 5000,

        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun : false,

        junitReporter : {
            outputFile: 'test_out/unit.xml',
            suite: 'unit'
        },

        frameworks : ["jasmine"],

        preprocessors : {
            '**/*.html': ['ng-html2js']
        }

    })
};