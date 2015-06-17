import '../../bower_components/es6-promise/promise.min.js';
import '../../bower_components/fetch/fetch.js';
import setupChromeExtension from './chrome-extension';
import editor from './editor';
import setupSwirlEdit from './edit-swirl';
import responseForm from './response-form';
import commentForm from './comment-form';
import menu from './menu';
import timeAgo from './time-ago';

$(document).ready(function () {
    editor.init($);
    setupSwirlEdit();
    setupChromeExtension();
    responseForm.init($);
    commentForm.init($);
    menu.init($);
    timeAgo.init($);
});
