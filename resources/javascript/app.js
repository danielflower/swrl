import '../../bower_components/es6-promise/promise.min.js';
import '../../bower_components/fetch/fetch.js';
import setupChromeExtension from "./chrome-extension";
import editor from "./editor";
import setupSwirlEdit from "./edit-swirl";
import respondForm from "./respond-form";
import commentForm from "./comment-form";
import menu from "./menu";

$(document).ready(function () {
    editor.init($);
    setupSwirlEdit();
    setupChromeExtension();
    respondForm.init($);
    commentForm.init($);
    menu.init($);
});
