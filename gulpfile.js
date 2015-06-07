var gulp = require('gulp');
var myth = require('gulp-myth');
var rev = require('gulp-rev');
var del = require('del');
var sizereport = require('gulp-sizereport');

var dist = 'resources/immutable/dist';

gulp.task('clean', function (cb) {
    del([dist + '/**/*'], cb);
});

gulp.task('generate-css', function () {
    return gulp.src('resources/css/app.css')
        .pipe(myth({compress: true}))
        .pipe(rev())
        .pipe(gulp.dest(dist));
});

gulp.task('size-report', function () {
    return gulp.src(dist + '/*')
        .pipe(sizereport());
});

gulp.task('default', ['clean', 'generate-css', 'size-report']);