var gulp = require('gulp');
var myth = require('gulp-myth');
var rev = require('gulp-rev');
var del = require('del');
var sizereport = require('gulp-sizereport');
var concat = require('gulp-concat');
var tap = require('gulp-tap');
var replace = require('gulp-replace');
var path = require('path');
var exec = require('gulp-exec');
var dist = 'resources/immutable/dist';
var babelify = require('babelify');
var browserify = require('browserify');
var source = require('vinyl-source-stream');

gulp.task('clean', function (cb) {
    return del([dist + '/**/*'], cb);
});

var templateHtml = 'resources/templates/base.html';

var prepareCSS = function (cssSource, fileRegex) {
    return cssSource
        .pipe(myth({compress: true}))
        .pipe(rev())
        .pipe(gulp.dest(dist))
        .pipe(tap(function (file) {
            return gulp.src(templateHtml)
                .pipe(replace(fileRegex, path.basename(file.path)))
                .pipe(gulp.dest(path.dirname(templateHtml)));
        }));
};
gulp.task('generate-css', ['clean', 'generate-thirdparty-css'], function () {
    return prepareCSS(gulp.src('resources/css/app.css'), /app-([a-g0-9]+)\.css/g);
});
gulp.task('generate-thirdparty-css', ['clean'], function () {
    return prepareCSS(
        gulp.src(['resources/css/normalize-3.0.2.css', 'resources/css/skeleton-2.0.4.css', 'resources/css/font-awesome.min.css'])
            .pipe(concat('thirdparty.css')), /thirdparty-([a-g0-9]+)\.css/g
    );
});

gulp.task('browsify-javascript', ['generate-css'], function () {
    return browserify({
        entries: 'resources/javascript/app.js'
    })
        .transform(babelify)
        .bundle()
        .pipe(source('all.js'))
        .pipe(gulp.dest(dist));
});

gulp.task('process-javascript', ['browsify-javascript'], function () {
    return gulp.src(dist + '/all.js')
        .pipe(rev())
        .pipe(gulp.dest(dist))
        .pipe(tap(function (file) {
            var fileRegex = /all-([a-g0-9]+)\.js/g;
            return gulp.src(templateHtml)
                .pipe(replace(fileRegex, path.basename(file.path, '.map')))
                .pipe(gulp.dest(path.dirname(templateHtml)));
        }));
});

gulp.task('default', ['process-javascript'], function () {
    return gulp.src(dist + '/**/*')
        .pipe(sizereport())
        .pipe(exec('git add --all resources/immutable/dist/'));
});

gulp.task("watch", ['default'], function () {
    gulp.watch('resources/javascript/**/*.js', ['default']);
    gulp.watch('resources/css/**/*.css', ['default']);
});