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

gulp.task('clean', function (cb) {
    del([dist + '/**/*'], cb);
});


var prepareCSS = function (cssSource, fileRegex) {
    return cssSource
        .pipe(myth({compress: true}))
        .pipe(rev())
        .pipe(gulp.dest(dist))
        .pipe(tap(function (file) {
            var templateHtml = 'resources/templates/base.html';
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

gulp.task('size-report-and-add', ['generate-css', 'generate-thirdparty-css'], function () {
    return gulp.src(dist + '/*')
        .pipe(sizereport())
        .pipe(exec('git add --all resources/immutable/dist/'));
});

gulp.task('default', ['size-report-and-add']);